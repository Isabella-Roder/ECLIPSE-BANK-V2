package com.eclipsebank.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.eclipsebank.backend.dto.ProventoFiiResposta;
import com.eclipsebank.backend.enums.StatusAplicacaoInvestimento;
import com.eclipsebank.backend.enums.TipoInvestimento;
import com.eclipsebank.backend.enums.TipoMovimentacao;
import com.eclipsebank.backend.exception.ConflitoException;
import com.eclipsebank.backend.models.AplicacaoInvestimento;
import com.eclipsebank.backend.models.Conta;
import com.eclipsebank.backend.models.Movimentacao;
import com.eclipsebank.backend.models.PagamentoProventoFii;
import com.eclipsebank.backend.models.ProdutoInvestimento;
import com.eclipsebank.backend.repository.AplicacaoInvestimentoRepository;
import com.eclipsebank.backend.repository.MovimentacaoRepository;
import com.eclipsebank.backend.repository.PagamentoProventoFiiRepository;

class ProventoFiiServiceTest {

    private AplicacaoInvestimentoRepository aplicacaoRepository;
    private PagamentoProventoFiiRepository pagamentoRepository;
    private MovimentacaoRepository movimentacaoRepository;
    private CalculadoraProventoFii calculadora;
    private ProventoFiiService service;

    @BeforeEach
    void preparar() {
        aplicacaoRepository = mock(AplicacaoInvestimentoRepository.class);
        pagamentoRepository = mock(PagamentoProventoFiiRepository.class);
        movimentacaoRepository = mock(MovimentacaoRepository.class);
        calculadora = mock(CalculadoraProventoFii.class);

        service = new ProventoFiiService(
            aplicacaoRepository,
            pagamentoRepository,
            movimentacaoRepository,
            calculadora
        );
    }

    @Test
    void deveCreditarProventoERegistrarMovimentacao() {
        AplicacaoInvestimento aplicacao = mock(AplicacaoInvestimento.class);
        ProdutoInvestimento produto = mock(ProdutoInvestimento.class);
        Conta conta = mock(Conta.class);

        when(aplicacaoRepository.findByIdAndContaId(5L, 2L))
            .thenReturn(Optional.of(aplicacao));
        when(aplicacao.getId()).thenReturn(5L);
        when(aplicacao.getStatus()).thenReturn(StatusAplicacaoInvestimento.ATIVA);
        when(aplicacao.getProduto()).thenReturn(produto);
        when(aplicacao.getConta()).thenReturn(conta);
        when(aplicacao.getQuantidadeCotas()).thenReturn(new BigDecimal("10.000000"));
        when(produto.getTipo()).thenReturn(TipoInvestimento.FUNDO_IMOBILIARIO);
        when(produto.getProventoMensalPorCota()).thenReturn(new BigDecimal("0.80"));
        when(produto.getNome()).thenReturn("FII Shopping");
        when(conta.getSaldo()).thenReturn(new BigDecimal("108.00"));
        when(calculadora.calcular(any(), any())).thenReturn(new BigDecimal("8.00"));
        when(pagamentoRepository.save(any(PagamentoProventoFii.class)))
            .thenAnswer(invocacao -> invocacao.getArgument(0));

        ProventoFiiResposta resposta = service.pagar(2L, 5L);

        assertEquals(new BigDecimal("8.00"), resposta.valor());
        assertEquals(YearMonth.now().toString(), resposta.competencia());
        verify(conta).creditar(new BigDecimal("8.00"));

        ArgumentCaptor<Movimentacao> captor = ArgumentCaptor.forClass(Movimentacao.class);
        verify(movimentacaoRepository).save(captor.capture());
        assertEquals(TipoMovimentacao.PROVENTO_FII, captor.getValue().getTipo());
        assertEquals(new BigDecimal("108.00"), captor.getValue().getSaldoResultante());
    }

    @Test
    void deveRecusarProventoDuplicadoNoMesmoMes() {
        AplicacaoInvestimento aplicacao = mock(AplicacaoInvestimento.class);
        ProdutoInvestimento produto = mock(ProdutoInvestimento.class);

        when(aplicacaoRepository.findByIdAndContaId(5L, 2L))
            .thenReturn(Optional.of(aplicacao));
        when(aplicacao.getStatus()).thenReturn(StatusAplicacaoInvestimento.ATIVA);
        when(aplicacao.getProduto()).thenReturn(produto);
        when(aplicacao.getQuantidadeCotas()).thenReturn(BigDecimal.TEN);
        when(produto.getTipo()).thenReturn(TipoInvestimento.FUNDO_IMOBILIARIO);
        when(produto.getProventoMensalPorCota()).thenReturn(BigDecimal.ONE);
        when(pagamentoRepository.existsByAplicacaoIdAndCompetencia(
            5L,
            YearMonth.now().toString()
        )).thenReturn(true);

        assertThrows(ConflitoException.class, () -> service.pagar(2L, 5L));
    }
}
