package com.eclipsebank.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.mockito.ArgumentMatchers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import com.eclipsebank.backend.dto.IndicadorBancoCentral;
import com.eclipsebank.backend.dto.IndicadorMercadoResposta;
import com.eclipsebank.backend.exception.ServicoExternoIndisponivelException;

@SuppressWarnings("unchecked")
class IndicadorMercadoServiceTest {

    private RestClient.RequestHeadersUriSpec requisicao;
    private RestClient.ResponseSpec resposta;
    private IndicadorMercadoService indicadorMercadoService;

    @BeforeEach
    void preparar() {
        RestClient restClient = mock(RestClient.class);
        requisicao = mock(RestClient.RequestHeadersUriSpec.class);
        resposta = mock(RestClient.ResponseSpec.class);

        when(restClient.get()).thenReturn((RestClient.RequestHeadersUriSpec) requisicao);
        when(requisicao.uri(anyString(), ArgumentMatchers.<Object>any())).thenReturn((RestClient.RequestHeadersSpec) requisicao);
        when(requisicao.retrieve()).thenReturn(resposta);

        indicadorMercadoService = new IndicadorMercadoService(restClient);
    }

    @Test
    void deveRetornarSelicComSucesso() {
        when(resposta.body(any(org.springframework.core.ParameterizedTypeReference.class)))
            .thenReturn(List.of(new IndicadorBancoCentral("04/09/2026", "0,05166")));

        IndicadorMercadoResposta indicador = indicadorMercadoService.buscarSelic();

        assertEquals("Selic", indicador.nome());
        assertEquals(new BigDecimal("0.05166"), indicador.valor());
        assertEquals(LocalDate.of(2026, 9, 4), indicador.dataReferencia());
    }

    @Test
    void deveLancarExcecaoQuandoBancoCentralNaoRetornaDados() {
        when(resposta.body(any(org.springframework.core.ParameterizedTypeReference.class)))
            .thenReturn(List.of());

        assertThrows(ServicoExternoIndisponivelException.class, () -> indicadorMercadoService.buscarSelic());
    }

    @Test
    void deveLancarExcecaoQuandoBancoCentralFalha() {
        when(resposta.body(any(org.springframework.core.ParameterizedTypeReference.class)))
            .thenThrow(new RuntimeException("timeout"));

        assertThrows(ServicoExternoIndisponivelException.class, () -> indicadorMercadoService.buscarCDI());
    }
}
