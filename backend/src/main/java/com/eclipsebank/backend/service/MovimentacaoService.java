package com.eclipsebank.backend.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eclipsebank.backend.dto.MovimentacaoResposta;
import com.eclipsebank.backend.dto.OperacaoValor;
import com.eclipsebank.backend.dto.TransferenciaRequisicao;
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

    public MovimentacaoService(ContaRepository contaRepository, MovimentacaoRepository movimentacaoRepository) {
        this.contaRepository = contaRepository;
        this.movimentacaoRepository = movimentacaoRepository;
    }

    private Conta buscarConta(Long contaId) {
        return contaRepository.findById(contaId)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Conta não encontrada com o ID: " + contaId));
    }

    private Conta buscarContaDestino(String agencia, String numero) {
        return contaRepository.findByAgenciaAndNumero(agencia, numero)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Conta de destino não encontrada"));
    }

    private MovimentacaoResposta registrar(Conta conta, TipoMovimentacao tipo, BigDecimal valor, String descricao) {
        Movimentacao movimentacao = new Movimentacao();

        movimentacao.setConta(conta);
        movimentacao.setTipo(tipo);
        movimentacao.setValor(valor);
        movimentacao.setSaldoResultante(conta.getSaldo());
        movimentacao.setDescricao(descricao);
        
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
