package com.eclipsebank.backend.service;

import java.math.BigDecimal;
import java.time.YearMonth;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eclipsebank.backend.dto.ProventoFiiResposta;
import com.eclipsebank.backend.enums.StatusAplicacaoInvestimento;
import com.eclipsebank.backend.enums.TipoInvestimento;
import com.eclipsebank.backend.enums.TipoMovimentacao;
import com.eclipsebank.backend.exception.ConflitoException;
import com.eclipsebank.backend.exception.RecursoNaoEncontradoException;
import com.eclipsebank.backend.models.AplicacaoInvestimento;
import com.eclipsebank.backend.models.Conta;
import com.eclipsebank.backend.models.Movimentacao;
import com.eclipsebank.backend.models.PagamentoProventoFii;
import com.eclipsebank.backend.repository.AplicacaoInvestimentoRepository;
import com.eclipsebank.backend.repository.MovimentacaoRepository;
import com.eclipsebank.backend.repository.PagamentoProventoFiiRepository;

@Service
public class ProventoFiiService {

    private final AplicacaoInvestimentoRepository aplicacaoRepository;
    private final PagamentoProventoFiiRepository pagamentoRepository;
    private final MovimentacaoRepository movimentacaoRepository;
    private final CalculadoraProventoFii calculadoraProventoFii;

    public ProventoFiiService(
        AplicacaoInvestimentoRepository aplicacaoRepository,
        PagamentoProventoFiiRepository pagamentoRepository,
        MovimentacaoRepository movimentacaoRepository,
        CalculadoraProventoFii calculadoraProventoFii
    ) {
        this.aplicacaoRepository = aplicacaoRepository;
        this.pagamentoRepository = pagamentoRepository;
        this.movimentacaoRepository = movimentacaoRepository;
        this.calculadoraProventoFii = calculadoraProventoFii;
    }

    @Transactional
    public ProventoFiiResposta pagar(Long contaId, Long aplicacaoId) {
        AplicacaoInvestimento aplicacao = aplicacaoRepository
            .findByIdAndContaId(aplicacaoId, contaId)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Aplicação não encontrada."));

        validarAplicacao(aplicacao);

        String competencia = YearMonth.now().toString();
        if (pagamentoRepository.existsByAplicacaoIdAndCompetencia(aplicacaoId, competencia)) {
            throw new ConflitoException("O provento desta aplicação já foi pago neste mês.");
        }

        BigDecimal valor = calculadoraProventoFii.calcular(
            aplicacao.getQuantidadeCotas(),
            aplicacao.getProduto().getProventoMensalPorCota()
        );

        Conta conta = aplicacao.getConta();
        conta.creditar(valor);

        PagamentoProventoFii pagamento = new PagamentoProventoFii();
        pagamento.registrar(aplicacao, competencia, valor);
        pagamentoRepository.save(pagamento);

        Movimentacao movimentacao = new Movimentacao();
        movimentacao.setConta(conta);
        movimentacao.setTipo(TipoMovimentacao.PROVENTO_FII);
        movimentacao.setValor(valor);
        movimentacao.setSaldoResultante(conta.getSaldo());
        movimentacao.setDescricao(
            "Provento de " + aplicacao.getProduto().getNome() + " - " + competencia
        );
        movimentacaoRepository.save(movimentacao);

        return ProventoFiiResposta.from(pagamento);
    }

    private void validarAplicacao(AplicacaoInvestimento aplicacao) {
        if (aplicacao.getStatus() != StatusAplicacaoInvestimento.ATIVA) {
            throw new ConflitoException("A aplicação não está ativa.");
        }
        if (aplicacao.getProduto().getTipo() != TipoInvestimento.FUNDO_IMOBILIARIO) {
            throw new ConflitoException("Esta aplicação não é um fundo imobiliário.");
        }
        if (aplicacao.getQuantidadeCotas() == null
            || aplicacao.getProduto().getProventoMensalPorCota() == null) {
            throw new ConflitoException(
                "A aplicação não possui dados suficientes para calcular o provento."
            );
        }
    }
}
