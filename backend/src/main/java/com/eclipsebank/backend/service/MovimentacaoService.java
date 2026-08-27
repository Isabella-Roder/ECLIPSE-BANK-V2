package com.eclipsebank.backend.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eclipsebank.backend.dto.MovimentacaoResposta;
import com.eclipsebank.backend.dto.OperacaoValor;
import com.eclipsebank.backend.dto.PixRequisicao;
import com.eclipsebank.backend.dto.TransferenciaRequisicao;
import com.eclipsebank.backend.enums.AcaoAuditoria;
import com.eclipsebank.backend.enums.TipoMovimentacao;
import com.eclipsebank.backend.exception.RecursoNaoEncontradoException;
import com.eclipsebank.backend.models.Conta;
import com.eclipsebank.backend.models.Movimentacao;
import com.eclipsebank.backend.repository.ContaRepository;
import com.eclipsebank.backend.repository.MovimentacaoRepository;

@Service
public class MovimentacaoService {

    private final ContaRepository contaRepository;
    private final MovimentacaoRepository movimentacaoRepository;
    private final AuditoriaService auditoriaService;

    public MovimentacaoService(ContaRepository contaRepository, MovimentacaoRepository movimentacaoRepository, AuditoriaService auditoriaService) {
        this.contaRepository = contaRepository;
        this.movimentacaoRepository = movimentacaoRepository;
        this.auditoriaService = auditoriaService;
    }

    private Conta buscarConta(Long contaId) {
        return contaRepository.findById(contaId)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Conta não encontrada com o ID: " + contaId));
    }

    private Conta buscarContaDestino(String agencia, String numero) {
        return contaRepository.findByAgenciaAndNumero(agencia, numero)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Conta de destino não encontrada"));
    }

    private Conta buscarContaPorChavePix(String chave) {
        return contaRepository.findByUsuarioEmailIgnoreCase(chave.trim())
            .orElseThrow(() -> new RecursoNaoEncontradoException("Chave pix não encontrada"));
    }

    private MovimentacaoResposta registrar(Conta conta, TipoMovimentacao tipo, BigDecimal valor, String descricao) {
        Movimentacao movimentacao = new Movimentacao();

        movimentacao.setConta(conta);
        movimentacao.setTipo(tipo);
        movimentacao.setValor(valor);
        movimentacao.setSaldoResultante(conta.getSaldo());
        movimentacao.setDescricao(descricao);

        long quantidadeAnterior = movimentacaoRepository.countByContaIdAndTipo(conta.getId(), tipo);

        if (quantidadeAnterior >= 3) {
            BigDecimal media = movimentacaoRepository.calcularValorMedioPorContaETipo(conta.getId(), tipo);

            if (media != null && valor.compareTo(media.multiply(new BigDecimal("5"))) > 0) {
                Long usuarioId = contaRepository.buscarUsuarioIdPelaContaId(conta.getId()).orElse(null);
                auditoriaService.registrar(usuarioId, AcaoAuditoria.MOVIMENTACAO_SUSPEITA, 
                    String.format("%s de %s muito acima da média (R$ %s)", tipo, valor, media));
            }
        }

        Movimentacao movimentacaoSalva = movimentacaoRepository.save(movimentacao);

        return MovimentacaoResposta.from(movimentacaoSalva);
    }

    @Transactional
    public MovimentacaoResposta depositar(Long contaId, OperacaoValor dados) {
        Conta conta = buscarConta(contaId);

        conta.creditar(dados.valor());

        return registrar(
            conta,
            TipoMovimentacao.DEPOSITO,
            dados.valor(),
            dados.descricao()
        );
    }

    @Transactional
    public MovimentacaoResposta sacar(Long contaId, OperacaoValor dados) {
        Conta conta = buscarConta(contaId);

        conta.debitar(dados.valor());

        return registrar(
            conta,
            TipoMovimentacao.SAQUE,
            dados.valor(),
            dados.descricao()
        );
    }

    @Transactional
    public MovimentacaoResposta transferir(Long contaOrigemId, TransferenciaRequisicao dados) {
        Conta contaOrigem = buscarConta(contaOrigemId);

        Conta contaDestino = buscarContaDestino(dados.agenciaDestino(), dados.numeroDestino());

        if (contaOrigem.getId().equals(contaDestino.getId())) {
            throw new IllegalArgumentException("Conta de destino e origem devem ser diferentes");
        }

        contaOrigem.debitar(dados.valor());
        contaDestino.creditar(dados.valor());

        MovimentacaoResposta movimentacaoEnviada = registrar(contaOrigem, TipoMovimentacao.TRANSFERENCIA_ENVIADA, dados.valor(), dados.descricao());

        registrar(contaDestino, TipoMovimentacao.TRANSFERENCIA_RECEBIDA, dados.valor(), dados.descricao());

        return movimentacaoEnviada;
    }

    @Transactional
    public MovimentacaoResposta fazerPix(Long contaOrigemId, PixRequisicao dados) {
        Conta contaOrigem = buscarConta(contaOrigemId);

        Conta contaDestino = buscarContaPorChavePix(dados.chave());

        if (contaOrigem.getId().equals(contaDestino.getId())) {
            throw new IllegalArgumentException("A chave pix não pode ser a mesma da conta de origem");
        }

        contaOrigem.debitar(dados.valor());
        contaDestino.creditar(dados.valor());

        MovimentacaoResposta movimentacaoEnviada = registrar(contaOrigem, TipoMovimentacao.PIX_ENVIADO, dados.valor(), dados.descricao());

        registrar(contaDestino, TipoMovimentacao.PIX_RECEBIDO, dados.valor(), dados.descricao());

        return movimentacaoEnviada;
    }

    @Transactional(readOnly = true)
    public List<MovimentacaoResposta> listarExtrato(Long contaId) {
        buscarConta(contaId);

        return movimentacaoRepository.findByContaIdOrderByCriadaEmDesc(contaId)
            .stream().map(MovimentacaoResposta::from).toList();
    }

    @Transactional(readOnly = true)
    public MovimentacaoResposta buscarPorCodigo(String codigo) {
        Movimentacao movimentacao = movimentacaoRepository.findByCodigo(codigo)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Movimentação não encontrada"));

        return MovimentacaoResposta.from(movimentacao);
    }
}
