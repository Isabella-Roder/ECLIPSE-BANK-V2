package com.eclipsebank.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.web.client.RestClient;

import com.eclipsebank.backend.dto.CambioResposta;
import com.eclipsebank.backend.dto.CotacaoCambio;
import com.eclipsebank.backend.exception.ServicoExternoIndisponivelException;

@SuppressWarnings("unchecked")
class CambioServiceTest {

    private RestClient.RequestHeadersUriSpec requisicao;
    private RestClient.ResponseSpec resposta;
    private CambioService cambioService;

    @BeforeEach
    void preparar() {
        RestClient awesomeapiRestClient = mock(RestClient.class);
        requisicao = mock(RestClient.RequestHeadersUriSpec.class);
        resposta = mock(RestClient.ResponseSpec.class);

        when(awesomeapiRestClient.get()).thenReturn((RestClient.RequestHeadersUriSpec) requisicao);
        when(requisicao.uri(anyString(), ArgumentMatchers.<Object>any())).thenReturn((RestClient.RequestHeadersSpec) requisicao);
        when(requisicao.retrieve()).thenReturn(resposta);

        cambioService = new CambioService(awesomeapiRestClient);
    }

    @Test
    void deveRetornarCambioComSucesso() {
        CotacaoCambio cotacao = new CotacaoCambio("USD", "BRL", "5.0851", "1788882193");
        when(resposta.body(any(org.springframework.core.ParameterizedTypeReference.class)))
            .thenReturn(Map.of("USDBRL", cotacao));

        CambioResposta cambio = cambioService.buscarCotacao("usd");

        assertEquals("USD", cambio.moeda());
        assertEquals(new BigDecimal("5.0851"), cambio.valorEmReais());
    }

    @Test
    void deveLancarExcecaoQuandoMoedaNaoEncontrada() {
        when(resposta.body(any(org.springframework.core.ParameterizedTypeReference.class)))
            .thenReturn(Map.of());

        assertThrows(ServicoExternoIndisponivelException.class, () -> cambioService.buscarCotacao("XXXXX"));
    }

    @Test
    void deveLancarExcecaoQuandoServicoFalha() {
        when(resposta.body(any(org.springframework.core.ParameterizedTypeReference.class)))
            .thenThrow(new RuntimeException("timeout"));

        assertThrows(ServicoExternoIndisponivelException.class, () -> cambioService.buscarCotacao("EUR"));
    }
}
