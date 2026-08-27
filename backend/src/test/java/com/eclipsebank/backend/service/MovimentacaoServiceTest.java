package com.eclipsebank.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.eclipsebank.backend.dto.MovimentacaoResposta;
import com.eclipsebank.backend.dto.PixRequisicao;
import com.eclipsebank.backend.dto.TransferenciaRequisicao;
import com.eclipsebank.backend.enums.TipoMovimentacao;
import com.eclipsebank.backend.exception.RecursoNaoEncontradoException;
import com.eclipsebank.backend.exception.SaldoInsuficienteException;
import com.eclipsebank.backend.models.Conta;
import com.eclipsebank.backend.models.Movimentacao;
import com.eclipsebank.backend.repository.ContaRepository;
import com.eclipsebank.backend.repository.MovimentacaoRepository;

@ExtendWith(MockitoExtension.class)
class MovimentacaoServiceTest {

    @Mock
    private ContaRepository contaRepository;

    @Mock
    private MovimentacaoRepository movimentacaoRepository;

    @Mock
    private Conta contaOrigem;

    @Mock
    private Conta contaDestino;

    @Mock
    private AuditoriaService auditoriaService;

    private MovimentacaoService movimentacaoService;

    @BeforeEach
    void preparar() {
        movimentacaoService = new MovimentacaoService(
            contaRepository,
            movimentacaoRepository,
            auditoriaService
        );
    }

    @Test
    void deveTransferirEntreContasERegistrarAsDuasMovimentacoes() {
        TransferenciaRequisicao dados = new TransferenciaRequisicao(
            "0001",
            "12345678",
            new BigDecimal("50.00"),
            "Transferência de teste"
        );

        prepararContasDiferentes();
        when(contaRepository.findByAgenciaAndNumero("0001", "12345678"))
            .thenReturn(Optional.of(contaDestino));
        when(contaOrigem.getSaldo()).thenReturn(new BigDecimal("150.00"));
        when(contaDestino.getSaldo()).thenReturn(new BigDecimal("50.00"));
        when(movimentacaoRepository.save(any(Movimentacao.class)))
            .thenAnswer(invocacao -> invocacao.getArgument(0));

        MovimentacaoResposta resposta = movimentacaoService.transferir(1L, dados);

        verify(contaOrigem).debitar(new BigDecimal("50.00"));
        verify(contaDestino).creditar(new BigDecimal("50.00"));

        ArgumentCaptor<Movimentacao> captor =
            ArgumentCaptor.forClass(Movimentacao.class);

        verify(movimentacaoRepository, times(2)).save(captor.capture());

        assertEquals(
            TipoMovimentacao.TRANSFERENCIA_ENVIADA,
            captor.getAllValues().get(0).getTipo()
        );
        assertEquals(
            TipoMovimentacao.TRANSFERENCIA_RECEBIDA,
            captor.getAllValues().get(1).getTipo()
        );
        assertEquals(TipoMovimentacao.TRANSFERENCIA_ENVIADA, resposta.tipo());
    }

    @Test
    void deveRecusarTransferenciaParaMesmaConta() {
        TransferenciaRequisicao dados = new TransferenciaRequisicao(
            "0001",
            "12345678",
            new BigDecimal("50.00"),
            "Transferência de teste"
        );

        when(contaOrigem.getId()).thenReturn(1L);
        when(contaRepository.findById(1L)).thenReturn(Optional.of(contaOrigem));
        when(contaRepository.findByAgenciaAndNumero("0001", "12345678"))
            .thenReturn(Optional.of(contaOrigem));

        assertThrows(
            IllegalArgumentException.class,
            () -> movimentacaoService.transferir(1L, dados)
        );

        verify(contaOrigem, never()).debitar(any(BigDecimal.class));
        verify(movimentacaoRepository, never()).save(any(Movimentacao.class));
    }

    @Test
    void deveFazerPixERegistrarAsDuasMovimentacoes() {
        PixRequisicao dados = new PixRequisicao(
            "destino@eclipsebank.local",
            new BigDecimal("25.00"),
            "Pix de teste"
        );

        prepararContasDiferentes();
        when(contaRepository.findByUsuarioEmailIgnoreCase("destino@eclipsebank.local"))
            .thenReturn(Optional.of(contaDestino));
        when(contaOrigem.getSaldo()).thenReturn(new BigDecimal("75.00"));
        when(contaDestino.getSaldo()).thenReturn(new BigDecimal("25.00"));
        when(movimentacaoRepository.save(any(Movimentacao.class)))
            .thenAnswer(invocacao -> invocacao.getArgument(0));

        MovimentacaoResposta resposta = movimentacaoService.fazerPix(1L, dados);

        verify(contaOrigem).debitar(new BigDecimal("25.00"));
        verify(contaDestino).creditar(new BigDecimal("25.00"));

        ArgumentCaptor<Movimentacao> captor =
            ArgumentCaptor.forClass(Movimentacao.class);

        verify(movimentacaoRepository, times(2)).save(captor.capture());

        assertEquals(
            TipoMovimentacao.PIX_ENVIADO,
            captor.getAllValues().get(0).getTipo()
        );
        assertEquals(
            TipoMovimentacao.PIX_RECEBIDO,
            captor.getAllValues().get(1).getTipo()
        );
        assertEquals(TipoMovimentacao.PIX_ENVIADO, resposta.tipo());
    }

    @Test
    void deveRecusarPixParaPropriaConta() {
        PixRequisicao dados = new PixRequisicao(
            "origem@eclipsebank.local",
            new BigDecimal("25.00"),
            "Pix de teste"
        );

        when(contaOrigem.getId()).thenReturn(1L);
        when(contaRepository.findById(1L)).thenReturn(Optional.of(contaOrigem));
        when(contaRepository.findByUsuarioEmailIgnoreCase("origem@eclipsebank.local"))
            .thenReturn(Optional.of(contaOrigem));

        assertThrows(
            IllegalArgumentException.class,
            () -> movimentacaoService.fazerPix(1L, dados)
        );

        verify(contaOrigem, never()).debitar(any(BigDecimal.class));
        verify(movimentacaoRepository, never()).save(any(Movimentacao.class));
    }

    @Test
    void deveRecusarPixQuandoChaveNaoExistir() {
        PixRequisicao dados = new PixRequisicao(
            "inexistente@eclipsebank.local",
            new BigDecimal("25.00"),
            null
        );

        when(contaRepository.findById(1L)).thenReturn(Optional.of(contaOrigem));
        when(contaRepository.findByUsuarioEmailIgnoreCase("inexistente@eclipsebank.local"))
            .thenReturn(Optional.empty());

        assertThrows(
            RecursoNaoEncontradoException.class,
            () -> movimentacaoService.fazerPix(1L, dados)
        );

        verify(contaOrigem, never()).debitar(any(BigDecimal.class));
        verify(movimentacaoRepository, never()).save(any(Movimentacao.class));
    }

    @Test
    void deveRecusarTransferenciaQuandoSaldoForInsuficiente() {
        TransferenciaRequisicao dados = new TransferenciaRequisicao(
            "0001",
            "12345678",
            new BigDecimal("500.00"),
            null
        );

        prepararContasDiferentes();
        when(contaRepository.findByAgenciaAndNumero("0001", "12345678"))
            .thenReturn(Optional.of(contaDestino));
        doThrow(new SaldoInsuficienteException())
            .when(contaOrigem).debitar(new BigDecimal("500.00"));

        assertThrows(
            SaldoInsuficienteException.class,
            () -> movimentacaoService.transferir(1L, dados)
        );

        verify(contaDestino, never()).creditar(any(BigDecimal.class));
        verify(movimentacaoRepository, never()).save(any(Movimentacao.class));
    }

    private void prepararContasDiferentes() {
        when(contaOrigem.getId()).thenReturn(1L);
        when(contaDestino.getId()).thenReturn(2L);
        when(contaRepository.findById(1L)).thenReturn(Optional.of(contaOrigem));
    }
}
