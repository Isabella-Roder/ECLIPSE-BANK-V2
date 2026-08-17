package com.eclipsebank.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eclipsebank.backend.dto.MetaFinanceiraCadastro;
import com.eclipsebank.backend.dto.MetaFinanceiraRequisicao;
import com.eclipsebank.backend.dto.MetaFinanceiraResposta;
import com.eclipsebank.backend.enums.StatusMetaFinanceira;
import com.eclipsebank.backend.enums.TipoMovimentacao;
import com.eclipsebank.backend.exception.RecursoNaoEncontradoException;
import com.eclipsebank.backend.models.Conta;
import com.eclipsebank.backend.models.MetaFinanceira;
import com.eclipsebank.backend.models.Movimentacao;
import com.eclipsebank.backend.repository.ContaRepository;
import com.eclipsebank.backend.repository.MetaFinanceiraRepository;
import com.eclipsebank.backend.repository.MovimentacaoRepository;

@Service
public class MetaFinanceiraService {
    
    private final MetaFinanceiraRepository metaFinanceiraRepository;
    private final ContaRepository contaRepository;
    private final MovimentacaoRepository movimentacaoRepository;

    public MetaFinanceiraService(MetaFinanceiraRepository metaFinanceiraRepository, ContaRepository contaRepository, MovimentacaoRepository movimentacaoRepository) {
        this.metaFinanceiraRepository = metaFinanceiraRepository;
        this.contaRepository = contaRepository;
        this.movimentacaoRepository = movimentacaoRepository;
    }

    private Conta buscarConta(Long contaId) {
        return contaRepository.findById(contaId)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Conta não encontrada com o ID: " + contaId));
    }

    private MetaFinanceira buscarMetaDaConta(Long metaId, Long contaId) {
        return metaFinanceiraRepository.findByIdAndContaId(metaId, contaId)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Meta financeira não encontrada para esta conta."));
    }

    @Transactional
    public MetaFinanceiraResposta criar(Long contaId, MetaFinanceiraCadastro dados) {
        Conta conta = buscarConta(contaId);

        MetaFinanceira metaFinanceira = new MetaFinanceira();
        metaFinanceira.setNome(dados.nome());
        metaFinanceira.setValorAlvo(dados.valorAlvo());
        metaFinanceira.setPrazo(dados.prazo());
        metaFinanceira.setConta(conta);
        metaFinanceira.setStatus(StatusMetaFinanceira.EM_ANDAMENTO);

        MetaFinanceira metaFinanceiraSalva = metaFinanceiraRepository.save(metaFinanceira);

        return MetaFinanceiraResposta.from(metaFinanceiraSalva);
    }

    @Transactional
    public MetaFinanceiraResposta aportar(Long contaId, Long metaId, MetaFinanceiraRequisicao dados) {
        Conta conta = buscarConta(contaId);

        MetaFinanceira meta = buscarMetaDaConta(metaId, contaId);

        conta.debitar(dados.valor());

        meta.aportar(dados.valor());

        MetaFinanceira metaSalva = metaFinanceiraRepository.save(meta);

        Movimentacao movimentacao = new Movimentacao();
        movimentacao.setConta(conta);
        movimentacao.setTipo(TipoMovimentacao.APORTE_META_FINANCEIRA);
        movimentacao.setValor(dados.valor());
        movimentacao.setSaldoResultante(conta.getSaldo());
        movimentacao.setDescricao("Aporte em " + meta.getNome());

        movimentacaoRepository.save(movimentacao);

        return MetaFinanceiraResposta.from(metaSalva);
    }

    @Transactional
    public MetaFinanceiraResposta resgatar(Long contaId, Long metaId, MetaFinanceiraRequisicao dados) {
        Conta conta = buscarConta(contaId);

        MetaFinanceira meta = buscarMetaDaConta(metaId, contaId);

        meta.resgatar(dados.valor());

        conta.creditar(dados.valor());

        Movimentacao movimentacao = new Movimentacao();
        movimentacao.setConta(conta);
        movimentacao.setTipo(TipoMovimentacao.RESGATE_META_FINANCEIRA);
        movimentacao.setValor(dados.valor());
        movimentacao.setSaldoResultante(conta.getSaldo());
        movimentacao.setDescricao("Resgate em " + meta.getNome());

        movimentacaoRepository.save(movimentacao);

        return MetaFinanceiraResposta.from(meta);
    }

    @Transactional(readOnly = true)
    public List<MetaFinanceiraResposta> listarPorConta(Long contaId) {
        Conta conta = buscarConta(contaId);

        return metaFinanceiraRepository.findByContaIdOrderByCriadaEmDesc(contaId)
            .stream().map(MetaFinanceiraResposta::from).toList();
    }
}
