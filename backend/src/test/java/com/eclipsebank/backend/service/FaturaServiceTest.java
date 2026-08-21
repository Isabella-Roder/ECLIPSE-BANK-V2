package com.eclipsebank.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.eclipsebank.backend.dto.FaturaResposta;
import com.eclipsebank.backend.enums.StatusFatura;
import com.eclipsebank.backend.enums.TipoCartao;
import com.eclipsebank.backend.exception.RecursoNaoEncontradoException;
import com.eclipsebank.backend.models.Cartao;
import com.eclipsebank.backend.models.Conta;
import com.eclipsebank.backend.models.Fatura;
import com.eclipsebank.backend.models.Movimentacao;
import com.eclipsebank.backend.repository.CartaoRepository;
import com.eclipsebank.backend.repository.ContaRepository;
import com.eclipsebank.backend.repository.FaturaRepository;
import com.eclipsebank.backend.repository.MovimentacaoRepository;

@ExtendWith(MockitoExtension.class)
public class FaturaServiceTest {
    
    @Mock
    private FaturaRepository faturaRepository;

    @Mock
    private CartaoRepository cartaoRepository;

    @Mock
    private ContaRepository contaRepository;

    @Mock
    private MovimentacaoRepository movimentacaoRepository;

    private FaturaService faturaService;

    @BeforeEach
    void preparar() {
        faturaService = new FaturaService(faturaRepository, cartaoRepository, contaRepository, movimentacaoRepository);
    }

    @Test
    void deveCriarFaturaNovaQuandoNaoExistirNoMes() {
        Cartao cartao = mock(Cartao.class);

        when(cartao.getTipo()).thenReturn(TipoCartao.CREDITO);

        when(cartaoRepository.findById(1L)).thenReturn(Optional.of(cartao));
        when(faturaRepository.findByCartaoIdAndMesReferencia(any(), any())).thenReturn(Optional.empty());
        when(faturaRepository.save(any(Fatura.class)))
            .thenAnswer(invocacao -> invocacao.getArgument(0));

        Fatura fatura = faturaService.obterFatuaAtualParaLancamento(1L, new BigDecimal("150.00"));

        assertEquals(new BigDecimal("150.00"), fatura.getValorTotal());
    }

    @Test
    void deveReaproveitarFaturaExistenteDoMes() {
        Cartao cartao = mock(Cartao.class);

        when(cartao.getTipo()).thenReturn(TipoCartao.CREDITO);

        Fatura faturaExistente = new Fatura();
        faturaExistente.setCartao(cartao);
        faturaExistente.setMesReferencia("2026-08");
        faturaExistente.lancarCompra(new BigDecimal("100.00"));

        when(cartaoRepository.findById(1L)).thenReturn(Optional.of(cartao));
        when(faturaRepository.findByCartaoIdAndMesReferencia(any(), any())).thenReturn(Optional.of(faturaExistente));

        Fatura fatura = faturaService.obterFatuaAtualParaLancamento(1L, new BigDecimal("50.00"));

        assertEquals(new BigDecimal("150.00"), fatura.getValorTotal());
    }

    @Test
    void deveRecusarFecharFaturaDeOutroCartao() {
        Cartao cartaoDaFatura = mock(Cartao.class);

        when(cartaoDaFatura.getId()).thenReturn(2L);

        Fatura fatura = new Fatura();
        fatura.setCartao(cartaoDaFatura);

        when(faturaRepository.findById(10L)).thenReturn(Optional.of(fatura));

        assertThrows(RecursoNaoEncontradoException.class, () -> faturaService.fechar(1L, 10L));
    }

    @Test
    void devePagarFaturaEDebitarConta() {
        Conta conta = new Conta();
        conta.creditar(new BigDecimal("500.00"));

        Cartao cartao = mock(Cartao.class);
        when(cartao.getId()).thenReturn(1L);

        Fatura fatura = new Fatura();
        fatura.setCartao(cartao);
        fatura.setMesReferencia("2026-08");
        fatura.lancarCompra(new BigDecimal("150.00"));
        fatura.fechar();

        when(contaRepository.findById(1L)).thenReturn(Optional.of(conta));
        when(faturaRepository.findById(10L)).thenReturn(Optional.of(fatura));

        FaturaResposta resposta = faturaService.pagar(1L, 1L, 10L);

        assertEquals(StatusFatura.PAGA, resposta.status());
        assertEquals(new BigDecimal("350.00"), conta.getSaldo());
        verify(movimentacaoRepository).save(any(Movimentacao.class));
    }
}
