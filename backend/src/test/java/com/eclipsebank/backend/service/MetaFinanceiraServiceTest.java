package com.eclipsebank.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.eclipsebank.backend.dto.MetaFinanceiraCadastro;
import com.eclipsebank.backend.dto.MetaFinanceiraRequisicao;
import com.eclipsebank.backend.dto.MetaFinanceiraResposta;
import com.eclipsebank.backend.enums.StatusMetaFinanceira;
import com.eclipsebank.backend.models.Conta;
import com.eclipsebank.backend.models.MetaFinanceira;
import com.eclipsebank.backend.models.Movimentacao;
import com.eclipsebank.backend.repository.ContaRepository;
import com.eclipsebank.backend.repository.MetaFinanceiraRepository;
import com.eclipsebank.backend.repository.MovimentacaoRepository;

@ExtendWith(MockitoExtension.class)
public class MetaFinanceiraServiceTest {
    
    @Mock
    private MetaFinanceiraRepository metaFinanceiraRepository;

    @Mock
    private ContaRepository contaRepository;

    @Mock
    private MovimentacaoRepository movimentacaoRepository;

    private MetaFinanceiraService metaFinanceiraService;

    @BeforeEach
    void preparar() {
        metaFinanceiraService = new MetaFinanceiraService(metaFinanceiraRepository, contaRepository, movimentacaoRepository);
    }

    @Test
    void deveCriarMetaFinanceira() {
        Conta conta = new Conta();

        MetaFinanceiraCadastro dados = new MetaFinanceiraCadastro("Viagem", new BigDecimal("1000.00"), LocalDate.now().plusMonths(6));

        when(contaRepository.findById(1L)).thenReturn(Optional.of(conta));
        when(metaFinanceiraRepository.save(any(MetaFinanceira.class)))
            .thenAnswer(invocacao -> invocacao.getArgument(0));

        MetaFinanceiraResposta resposta = metaFinanceiraService.criar(1L, dados);

        assertEquals("Viagem", resposta.nome());
        assertEquals(new BigDecimal("1000.00"), resposta.valorAlvo());
    }

    @Test
    void deveAportarNaMeta() {
        Conta conta = new Conta();
        conta.creditar(new BigDecimal("500.00"));

        MetaFinanceira meta = new MetaFinanceira();
        meta.setValorAlvo(new BigDecimal("1000.00"));
        meta.setStatus(StatusMetaFinanceira.EM_ANDAMENTO);
        meta.setConta(conta);

        MetaFinanceiraRequisicao dados = new MetaFinanceiraRequisicao(1L, new BigDecimal("200.00"));

        when(metaFinanceiraRepository.findByIdAndContaId(1L, 1L)).thenReturn(Optional.of(meta));
        when(contaRepository.findById(1L)).thenReturn(Optional.of(conta));
        when(metaFinanceiraRepository.save(any(MetaFinanceira.class)))
            .thenAnswer(invocacao -> invocacao.getArgument(0));

        MetaFinanceiraResposta resposta = metaFinanceiraService.aportar(1L, 1L, dados);

        assertEquals(new BigDecimal("200.00"), resposta.valorAtual());
        assertEquals(new BigDecimal("300.00"), conta.getSaldo());
        verify(movimentacaoRepository).save(any(Movimentacao.class));
    }

    @Test
    void deveResgatarDaMeta() {
        Conta conta = new Conta();
        conta.creditar(new BigDecimal("300.00"));

        MetaFinanceira meta = new MetaFinanceira();
        meta.setValorAlvo(new BigDecimal("1000.00"));
        meta.setStatus(StatusMetaFinanceira.EM_ANDAMENTO);
        meta.setConta(conta);
        meta.aportar(new BigDecimal("500.00"));

        MetaFinanceiraRequisicao dados = new MetaFinanceiraRequisicao(1L, new BigDecimal("200.00"));

        when(metaFinanceiraRepository.findByIdAndContaId(1L, 1L)).thenReturn(Optional.of(meta));
        when(contaRepository.findById(1L)).thenReturn(Optional.of(conta));

        MetaFinanceiraResposta resposta = metaFinanceiraService.resgatar(1L, 1L, dados);

        assertEquals(new BigDecimal("300.00"), resposta.valorAtual());
        assertEquals(new BigDecimal("500.00"), conta.getSaldo());
        verify(movimentacaoRepository).save(any(Movimentacao.class));
    }

}
