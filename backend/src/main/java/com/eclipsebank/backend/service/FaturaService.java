package com.eclipsebank.backend.service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eclipsebank.backend.dto.CompraCartao;
import com.eclipsebank.backend.dto.FaturaResposta;
import com.eclipsebank.backend.enums.TipoCartao;
import com.eclipsebank.backend.enums.TipoMovimentacao;
import com.eclipsebank.backend.exception.ConflitoException;
import com.eclipsebank.backend.exception.RecursoNaoEncontradoException;
import com.eclipsebank.backend.models.Cartao;
import com.eclipsebank.backend.models.Conta;
import com.eclipsebank.backend.models.Fatura;
import com.eclipsebank.backend.models.Movimentacao;
import com.eclipsebank.backend.repository.CartaoRepository;
import com.eclipsebank.backend.repository.ContaRepository;
import com.eclipsebank.backend.repository.FaturaRepository;
import com.eclipsebank.backend.repository.MovimentacaoRepository;

@Service
public class FaturaService {
    
    private final FaturaRepository faturaRepository;
    private final CartaoRepository cartaoRepository;
    private final ContaRepository contaRepository;
    private final MovimentacaoRepository movimentacaoRepository;

    public FaturaService(FaturaRepository faturaRepository, CartaoRepository cartaoRepository, ContaRepository contaRepository, MovimentacaoRepository movimentacaoRepository) {
        this.faturaRepository = faturaRepository;
        this.cartaoRepository = cartaoRepository;
        this.contaRepository = contaRepository;
        this.movimentacaoRepository = movimentacaoRepository;
    }

    private Fatura buscarFaturaDoCartao(Long faturaId, Long cartaoId) {
        Fatura fatura = faturaRepository.findById(faturaId)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Fatura do cartão não encontrada com ID: " + faturaId));

        if (!fatura.getCartao().getId().equals(cartaoId)) {
            throw new RecursoNaoEncontradoException("Fatura do cartão não encontrado com ID: " + faturaId);
        }

        return fatura;
        
    }

    public void validarCartaoCredito(Long cartaoId) {
        Cartao cartao = cartaoRepository.findById(cartaoId)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Cartão não encontrado com ID: " + cartaoId));

        if (cartao.getTipo() != TipoCartao.CREDITO) {
            throw new ConflitoException("O cartão não é de credito para fatura.");
        }
    }

    private Fatura buscarFaturaPorMesAtual(Long cartaoId, String mesReferencia) {
        return faturaRepository.findByCartaoIdAndMesReferencia(cartaoId, mesReferencia)
            .orElseGet(() -> {
                Cartao cartao = cartaoRepository.findById(cartaoId)
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Cartão não encontrado com ID: " + cartaoId));

                Fatura novaFatura = new Fatura();
                novaFatura.setCartao(cartao);
                novaFatura.setMesReferencia(mesReferencia);
                novaFatura.setDataVencimento(LocalDate.now().plusMonths(1).withDayOfMonth(10));

                return faturaRepository.save(novaFatura);
            });
    }

    @Transactional
    public FaturaResposta lancarCompra(Long cartaoId, CompraCartao dados) {
        validarCartaoCredito(cartaoId);

        String mesReferencia = YearMonth.now().toString();
        Fatura fatura = buscarFaturaPorMesAtual(cartaoId, mesReferencia);

        fatura.lancarCompra(dados.valor());

        return FaturaResposta.from(fatura);
    }

    @Transactional
    public FaturaResposta pagar(Long contaId, Long cartaoId, Long faturaId) {
        Conta conta = contaRepository.findById(contaId)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Conta não encontrada para com ID: " + contaId));

        Fatura fatura = buscarFaturaDoCartao(faturaId, cartaoId);

        fatura.pagar();

        conta.debitar(fatura.getValorTotal());

        Movimentacao movimentacao = new Movimentacao();
        movimentacao.setConta(conta);
        movimentacao.setTipo(TipoMovimentacao.PAGAMENTO_FATURA);
        movimentacao.setValor(fatura.getValorTotal());
        movimentacao.setSaldoResultante(conta.getSaldo());
        movimentacao.setDescricao("Pagamento de fatura " + fatura.getMesReferencia());

        movimentacaoRepository.save(movimentacao);

        return FaturaResposta.from(fatura);
    }

    @Transactional
    public FaturaResposta fechar(Long cartaoId, Long faturaId) {
        Fatura fatura = buscarFaturaDoCartao(faturaId, cartaoId);
        fatura.fechar();

        return FaturaResposta.from(fatura);
    }

    @Transactional(readOnly = true)
    public List<FaturaResposta> listarPorCartao(Long cartaoId) {
        return faturaRepository.findByCartaoId(cartaoId).stream()
            .map(FaturaResposta::from).toList();
    }
}
