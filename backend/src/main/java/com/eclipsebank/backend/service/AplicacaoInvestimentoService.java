package com.eclipsebank.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.eclipsebank.backend.dto.AplicacaoInvestimentoRequisicao;
import com.eclipsebank.backend.dto.AplicacaoInvestimentoResposta;
import com.eclipsebank.backend.dto.PosicaoConsolidadaResposta;
import com.eclipsebank.backend.dto.ResgateInvestimentoRequisicao;
import com.eclipsebank.backend.enums.StatusAplicacaoInvestimento;
import com.eclipsebank.backend.enums.TipoInvestimento;
import com.eclipsebank.backend.enums.TipoMovimentacao;
import com.eclipsebank.backend.exception.ConflitoException;
import com.eclipsebank.backend.exception.RecursoNaoEncontradoException;
import com.eclipsebank.backend.models.AplicacaoInvestimento;
import com.eclipsebank.backend.models.Conta;
import com.eclipsebank.backend.models.Movimentacao;
import com.eclipsebank.backend.models.ProdutoInvestimento;
import com.eclipsebank.backend.repository.AplicacaoInvestimentoRepository;
import com.eclipsebank.backend.repository.ContaRepository;
import com.eclipsebank.backend.repository.MovimentacaoRepository;
import com.eclipsebank.backend.repository.ProdutoInvestimentoRepository;

@Service
public class AplicacaoInvestimentoService {
    
    private final AplicacaoInvestimentoRepository aplicacaoInvestimentoRepository;
    private final ContaRepository contaRepository;
    private final ProdutoInvestimentoRepository produtoInvestimentoRepository;
    private final MovimentacaoRepository movimentacaoRepository;
    private final CalculadoraRentabilidade calculadoraRentabilidade;

    public AplicacaoInvestimentoService(
        AplicacaoInvestimentoRepository aplicacaoInvestimentoRepository,
        ContaRepository contaRepository,
        ProdutoInvestimentoRepository produtoInvestimentoRepository,
        MovimentacaoRepository movimentacaoRepository,
        CalculadoraRentabilidade calculadoraRentabilidade
    ) {
        this.aplicacaoInvestimentoRepository = aplicacaoInvestimentoRepository;
        this.contaRepository = contaRepository;
        this.produtoInvestimentoRepository = produtoInvestimentoRepository;
        this.movimentacaoRepository = movimentacaoRepository;
        this.calculadoraRentabilidade = calculadoraRentabilidade;
    }

    private Conta buscarConta(Long contaId) {
        return contaRepository.findById(contaId)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Conta não encontrada com o ID: " + contaId));
    }

    private ProdutoInvestimento buscarProduto(Long produtoId) {
        return produtoInvestimentoRepository.findById(produtoId)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Produto não encontrado com o ID: " + produtoId));
    }

    private void validarProdutoDisponivel(ProdutoInvestimento produto) {
        if (!produto.getAtivo()) {
            throw new ConflitoException("O produto de investimento não está disponível.");
        }
    }

    private void validarValorMinimo(ProdutoInvestimento produto, BigDecimal valor) {
        if (valor.compareTo(produto.getValorMinimo()) < 0) {
            throw new ConflitoException("O valor mínimo para este produto é R$ " + produto.getValorMinimo());
        }
    }

    private AplicacaoInvestimento buscarAplicacaoDaConta(Long aplicacaoId, Long contaId) {
        return aplicacaoInvestimentoRepository.findByIdAndContaId(aplicacaoId, contaId)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Aplicação de investimentos não encontrada para esta conta."));
    }

    private long quantosDiasAplicacaoDinheiro(AplicacaoInvestimento dados) {
        LocalDateTime dataInical = dados.getAtualizadaEm();
        
        LocalDateTime dataAtual = LocalDateTime.now();

        return ChronoUnit.DAYS.between(dataInical, dataAtual);
    }

    private BigDecimal calcularSaldoAtualEstimado(AplicacaoInvestimento aplicacao) {
        long dias = quantosDiasAplicacaoDinheiro(aplicacao);

        BigDecimal calculando = calculadoraRentabilidade.calcularSaldoAtual(aplicacao.getSaldoInvestido(), aplicacao.getProduto().getRentabilidadeAnualEstimada(), dias);

        return calculando;
    }

    private PosicaoConsolidadaResposta consolidar(List<AplicacaoInvestimento> aplicacoes) {
        ProdutoInvestimento produto = aplicacoes.get(0).getProduto();

        BigDecimal valorTotalAplicado = aplicacoes.stream()
            .map(AplicacaoInvestimento::getValorAplicado)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal valorTotalAtual = aplicacoes.stream()
            .map(this::calcularSaldoAtualEstimado)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal quantidadeTotalCotas = aplicacoes.stream()
            .map(AplicacaoInvestimento::getQuantidadeCotas)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal precoMedio = quantidadeTotalCotas.compareTo(BigDecimal.ZERO) > 0
            ? valorTotalAplicado.divide(quantidadeTotalCotas, 6, RoundingMode.HALF_UP)
            : null;

        return new PosicaoConsolidadaResposta(
            produto.getId(),
            produto.getNome(),
            produto.getCodigo(),
            produto.getTipo(),
            quantidadeTotalCotas,
            valorTotalAplicado,
            valorTotalAtual,
            precoMedio
        );
    }

    @Transactional
    public AplicacaoInvestimentoResposta aplicar(Long contaId, AplicacaoInvestimentoRequisicao dados) {
        Conta conta = buscarConta(contaId);

        ProdutoInvestimento produto = buscarProduto(dados.produtoId());

        validarProdutoDisponivel(produto);

        validarValorMinimo(produto, dados.valor());

        conta.debitar(dados.valor());

        AplicacaoInvestimento aplicacao = new AplicacaoInvestimento();
        aplicacao.setConta(conta);
        aplicacao.setProduto(produto);
        aplicacao.aplicar(dados.valor());

        if (aplicacao.getProduto().getTipo() == TipoInvestimento.FUNDO_IMOBILIARIO) {
            BigDecimal quantidadeCotas = dados.valor().divide(produto.getPrecoCota(), 6, RoundingMode.HALF_UP);

            aplicacao.definirQuantidadeCotas(quantidadeCotas);
        }

        AplicacaoInvestimento aplicacaoSalva = aplicacaoInvestimentoRepository.save(aplicacao);

        Movimentacao movimentacao = new Movimentacao();
        movimentacao.setConta(conta);
        movimentacao.setTipo(TipoMovimentacao.APLICACAO_INVESTIMENTO);
        movimentacao.setValor(dados.valor());
        movimentacao.setSaldoResultante(conta.getSaldo());
        movimentacao.setDescricao("Aplicação em " + produto.getNome());

        movimentacaoRepository.save(movimentacao);

        return AplicacaoInvestimentoResposta.from(aplicacaoSalva, aplicacaoSalva.getSaldoInvestido());
    }

    @Transactional
    public AplicacaoInvestimentoResposta resgatar(Long contaId, Long aplicacaoId, ResgateInvestimentoRequisicao dados) {
        Conta conta = buscarConta(contaId);

        AplicacaoInvestimento aplicacao = buscarAplicacaoDaConta(aplicacaoId, contaId);

        BigDecimal saldoAtual = calcularSaldoAtualEstimado(aplicacao);

        aplicacao.atualizarSaldoInvestido(saldoAtual);

        aplicacao.resgatar(dados.valor());

        conta.creditar(dados.valor());

        Movimentacao movimentacao = new Movimentacao();
        movimentacao.setConta(conta);
        movimentacao.setTipo(TipoMovimentacao.RESGATE_INVESTIMENTO);
        movimentacao.setValor(dados.valor());
        movimentacao.setSaldoResultante(conta.getSaldo());
        movimentacao.setDescricao("Resgate de " + aplicacao.getProduto().getNome());

        movimentacaoRepository.save(movimentacao);

        return AplicacaoInvestimentoResposta.from(aplicacao, aplicacao.getSaldoInvestido());
    }

    @Transactional(readOnly = true)
    public List<AplicacaoInvestimentoResposta> listarPorConta(Long contaId) {
        Conta conta = buscarConta(contaId);

        return aplicacaoInvestimentoRepository.findByContaIdOrderByAplicadaEmDesc(conta.getId())
            .stream().map(aplicacao -> AplicacaoInvestimentoResposta.from(aplicacao, calcularSaldoAtualEstimado(aplicacao))).toList();
    }

    @Transactional(readOnly = true)
    public List<PosicaoConsolidadaResposta> listarPosicaoConsolidada(Long contaId) {
        Conta conta = buscarConta(contaId);

        List<AplicacaoInvestimento> aplicacaoAtivas = aplicacaoInvestimentoRepository
            .findByContaIdOrderByAplicadaEmDesc(conta.getId())
            .stream()
            .filter(aplicacao -> aplicacao.getStatus() == StatusAplicacaoInvestimento.ATIVA)
            .toList();
        
        Map<Long, List<AplicacaoInvestimento>> porProduto = aplicacaoAtivas.stream()
            .collect(Collectors.groupingBy(aplicacao -> aplicacao.getProduto().getId()));

        return porProduto.values().stream().map(this::consolidar).toList();
    }
}
