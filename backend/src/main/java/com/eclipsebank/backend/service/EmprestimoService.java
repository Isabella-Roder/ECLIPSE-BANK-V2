package com.eclipsebank.backend.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eclipsebank.backend.dto.EmprestimoResposta;
import com.eclipsebank.backend.dto.EmprestimoSolicitacao;
import com.eclipsebank.backend.dto.ParcelaResposta;
import com.eclipsebank.backend.enums.StatusEmprestimo;
import com.eclipsebank.backend.enums.StatusParcela;
import com.eclipsebank.backend.enums.TipoMovimentacao;
import com.eclipsebank.backend.exception.RecursoNaoEncontradoException;
import com.eclipsebank.backend.models.Conta;
import com.eclipsebank.backend.models.Emprestimo;
import com.eclipsebank.backend.models.Movimentacao;
import com.eclipsebank.backend.models.Parcela;
import com.eclipsebank.backend.repository.ContaRepository;
import com.eclipsebank.backend.repository.EmprestimoRepository;
import com.eclipsebank.backend.repository.MovimentacaoRepository;
import com.eclipsebank.backend.repository.ParcelaRepository;

@Service
public class EmprestimoService {
    
    private final EmprestimoRepository emprestimoRepository;
    private final ContaRepository contaRepository;
    private final ParcelaRepository parcelaRepository;
    private final MovimentacaoRepository movimentacaoRepository;

    public EmprestimoService(
        EmprestimoRepository emprestimoRepository,
        ContaRepository contaRepository,
        ParcelaRepository parcelaRepository,
        MovimentacaoRepository movimentacaoRepository
    ) {
        this.emprestimoRepository = emprestimoRepository;
        this.contaRepository = contaRepository;
        this.parcelaRepository = parcelaRepository;
        this.movimentacaoRepository = movimentacaoRepository;
    }

    private BigDecimal calcularValorTotal(BigDecimal valorSolicitado, BigDecimal taxaJuros) {
        BigDecimal juros = valorSolicitado.multiply(taxaJuros).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

        return valorSolicitado.add(juros);
    }

    private BigDecimal calcularValorParcela(BigDecimal valorTotal, Integer quantidadeParcelas) {
        return valorTotal.divide(new BigDecimal(quantidadeParcelas), 2, RoundingMode.HALF_UP);
    }

    private Emprestimo buscarEmprestimoDaConta(Long emprestimoId, Long contaId) {
        Emprestimo emprestimo = emprestimoRepository.findById(emprestimoId)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Empréstimo não encontrado com ID: " + emprestimoId));

        if (!emprestimo.getConta().getId().equals(contaId)) {
            throw new RecursoNaoEncontradoException("Empréstimo não encontrado com ID: " + emprestimoId);
        }

        return emprestimo;
    }

    private Parcela buscarParcelaDoEmprestimo(Long parcelaId, Long emprestimoId) {
        Parcela parcela = parcelaRepository.findById(parcelaId)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Parcela não encontrada com ID: " + parcelaId));

        if (!parcela.getEmprestimo().getId().equals(emprestimoId)) {
            throw new RecursoNaoEncontradoException("Parcela não encontrada com ID: " + parcelaId);
        }

        return parcela;
    }

    @Transactional
    public EmprestimoResposta solicitar(Long contaId, EmprestimoSolicitacao dados) {
        Conta conta = contaRepository.findById(contaId)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Conta não encontrada com ID: " + contaId));

        BigDecimal valorTotal = calcularValorTotal(dados.valorSolicitado(), dados.taxaJuros());
        BigDecimal valorParcela = calcularValorParcela(valorTotal, dados.quantidadeParcelas());

        Emprestimo emprestimo = new Emprestimo();
        emprestimo.setConta(conta);
        emprestimo.setValorSolicitado(dados.valorSolicitado());
        emprestimo.setTaxaJuros(dados.taxaJuros());
        emprestimo.setQuantidadeParcelas(dados.quantidadeParcelas());
        emprestimo.setValorTotal(valorTotal);

        emprestimo = emprestimoRepository.save(emprestimo);

        for (int i = 1; i <= dados.quantidadeParcelas(); i++) {
            Parcela parcela = new Parcela();
            parcela.setEmprestimo(emprestimo);
            parcela.setNumero(i);
            parcela.setValor(valorParcela);
            parcela.setDataVencimento(LocalDate.now().plusMonths(i));

            parcelaRepository.save(parcela);
        }

        return EmprestimoResposta.from(emprestimo);
    }

    @Transactional
    public EmprestimoResposta aprovar(Long contaId, Long emprestimoId) {
        Emprestimo emprestimo = buscarEmprestimoDaConta(emprestimoId, contaId);

        emprestimo.aprovar();

        Conta conta = emprestimo.getConta();
        conta.creditar(emprestimo.getValorSolicitado());

        Movimentacao movimentacao = new Movimentacao();
        movimentacao.setConta(conta);
        movimentacao.setTipo(TipoMovimentacao.EMPRESTIMO_LIBERADO);
        movimentacao.setValor(emprestimo.getValorSolicitado());
        movimentacao.setSaldoResultante(conta.getSaldo());
        movimentacao.setDescricao("Liberação de empréstimo");

        movimentacaoRepository.save(movimentacao);

        return EmprestimoResposta.from(emprestimo);
    }

    @Transactional
    public ParcelaResposta pagarParcela(Long contaId, Long emprestimoId, Long parcelaId) {
        Emprestimo emprestimo = buscarEmprestimoDaConta(emprestimoId, contaId);
        Parcela parcela = buscarParcelaDoEmprestimo(parcelaId, emprestimoId);

        parcela.pagar();

        Conta conta = emprestimo.getConta();
        conta.debitar(parcela.getValor());

        Movimentacao movimentacao = new Movimentacao();
        movimentacao.setConta(conta);
        movimentacao.setTipo(TipoMovimentacao.PAGAMENTO_PARCELA_EMPRESTIMO);
        movimentacao.setValor(parcela.getValor());
        movimentacao.setSaldoResultante(conta.getSaldo());
        movimentacao.setDescricao("Pagamento da parcela " + parcela.getNumero());

        movimentacaoRepository.save(movimentacao);

        boolean todasPagas = parcelaRepository.findByEmprestimoId(emprestimoId).stream()
            .allMatch(p -> p.getStatus() == StatusParcela.PAGA);

        if (todasPagas) {
            emprestimo.setStatus(StatusEmprestimo.QUITADO);
        }

        return ParcelaResposta.from(parcela);
    }
}
