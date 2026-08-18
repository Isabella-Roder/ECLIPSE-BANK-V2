package com.eclipsebank.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
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

import com.eclipsebank.backend.dto.AplicacaoInvestimentoRequisicao;
import com.eclipsebank.backend.enums.TipoInvestimento;
import com.eclipsebank.backend.exception.SaldoInsuficienteException;
import com.eclipsebank.backend.models.AplicacaoInvestimento;
import com.eclipsebank.backend.models.Conta;
import com.eclipsebank.backend.models.ProdutoInvestimento;
import com.eclipsebank.backend.repository.AplicacaoInvestimentoRepository;
import com.eclipsebank.backend.repository.ContaRepository;
import com.eclipsebank.backend.repository.MovimentacaoRepository;
import com.eclipsebank.backend.repository.ProdutoInvestimentoRepository;

@ExtendWith(MockitoExtension.class)
class AplicacaoInvestimentoServiceTest {

    @Mock
    private AplicacaoInvestimentoRepository aplicacaoInvestimentoRepository;

    @Mock
    private ContaRepository contaRepository;

    @Mock
    private ProdutoInvestimentoRepository produtoInvestimentoRepository;

    @Mock
    private MovimentacaoRepository movimentacaoRepository;

    @Mock
    private CalculadoraRentabilidade calculadoraRentabilidade;

    @Mock
    private Conta conta;

    @Mock
    private ProdutoInvestimento produto;

    private AplicacaoInvestimentoService aplicacaoInvestimentoService;

    @BeforeEach
    void preparar() {
        aplicacaoInvestimentoService = new AplicacaoInvestimentoService(
            aplicacaoInvestimentoRepository,
            contaRepository,
            produtoInvestimentoRepository,
            movimentacaoRepository,
            calculadoraRentabilidade
        );
    }

    @Test
    void deveCalcularQuantidadeDeCotasAoAplicarEmFundoImobiliario() {
        AplicacaoInvestimentoRequisicao dados =
            new AplicacaoInvestimentoRequisicao(
                2L,
                new BigDecimal("1000.00")
            );

        when(contaRepository.findById(1L)).thenReturn(Optional.of(conta));
        when(produtoInvestimentoRepository.findById(2L))
            .thenReturn(Optional.of(produto));
        when(produto.getAtivo()).thenReturn(true);
        when(produto.getValorMinimo()).thenReturn(new BigDecimal("100.00"));
        when(produto.getTipo()).thenReturn(TipoInvestimento.FUNDO_IMOBILIARIO);
        when(produto.getPrecoCota()).thenReturn(new BigDecimal("100.00"));
        when(aplicacaoInvestimentoRepository.save(any(AplicacaoInvestimento.class)))
            .thenAnswer(invocacao -> invocacao.getArgument(0));

        aplicacaoInvestimentoService.aplicar(1L, dados);

        ArgumentCaptor<AplicacaoInvestimento> captor =
            ArgumentCaptor.forClass(AplicacaoInvestimento.class);

        verify(aplicacaoInvestimentoRepository).save(captor.capture());

        assertEquals(
            new BigDecimal("10.000000"),
            captor.getValue().getQuantidadeCotas()
        );
    }

    @Test
    void deveRecusarAplicacaoQuandoSaldoForInsuficiente() {
        AplicacaoInvestimentoRequisicao dados = new AplicacaoInvestimentoRequisicao(2L, new BigDecimal("1000.00"));

        when(contaRepository.findById(1L)).thenReturn(Optional.of(conta));
        when(produtoInvestimentoRepository.findById(2L)).thenReturn(Optional.of(produto));
        when(produto.getAtivo()).thenReturn(true);
        when(produto.getValorMinimo()).thenReturn(new BigDecimal("100.00"));
        doThrow(new SaldoInsuficienteException()).when(conta).debitar(dados.valor());

        assertThrows(SaldoInsuficienteException.class, () -> aplicacaoInvestimentoService.aplicar(1L, dados));

        verify(aplicacaoInvestimentoRepository, never()).save(any());
        verify(movimentacaoRepository, never()).save(any());
    }
}
