package com.eclipsebank.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.mockito.ArgumentMatchers;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;
import org.junit.jupiter.api.extension.ExtendWith;

import com.eclipsebank.backend.dto.CotacaoBrapi;
import com.eclipsebank.backend.dto.CotacaoResposta;
import com.eclipsebank.backend.enums.TipoInvestimento;
import com.eclipsebank.backend.exception.ConflitoException;
import com.eclipsebank.backend.exception.RecursoNaoEncontradoException;
import com.eclipsebank.backend.exception.ServicoExternoIndisponivelException;
import com.eclipsebank.backend.models.ProdutoInvestimento;
import com.eclipsebank.backend.repository.ProdutoInvestimentoRepository;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class CotacaoServiceTest {

    @Mock
    private ProdutoInvestimentoRepository produtoInvestimentoRepository;

    private RestClient.RequestHeadersUriSpec requisicao;
    private RestClient.ResponseSpec resposta;
    private CotacaoService cotacaoService;

    private RestClient brapiRestClient;

    @BeforeEach
    void preparar() {
        brapiRestClient = mock(RestClient.class);
        cotacaoService = new CotacaoService(brapiRestClient, produtoInvestimentoRepository);
    }

    private void prepararRestClient() {
        requisicao = mock(RestClient.RequestHeadersUriSpec.class);
        resposta = mock(RestClient.ResponseSpec.class);

        when(brapiRestClient.get()).thenReturn((RestClient.RequestHeadersUriSpec) requisicao);
        when(requisicao.uri(anyString(), ArgumentMatchers.<Object>any())).thenReturn((RestClient.RequestHeadersSpec) requisicao);
        when(requisicao.retrieve()).thenReturn(resposta);
    }

    private ProdutoInvestimento criarProduto(String codigo, TipoInvestimento tipo) {
        ProdutoInvestimento produto = new ProdutoInvestimento();
        produto.setCodigo(codigo);
        produto.setTipo(tipo);
        return produto;
    }

    @Test
    void deveLancarRecursoNaoEncontradoQuandoProdutoNaoExiste() {
        when(produtoInvestimentoRepository.findByCodigoIgnoreCase("NAOEXISTE")).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> cotacaoService.buscarCotacao("NAOEXISTE"));
    }

    @Test
    void deveLancarConflitoQuandoProdutoNaoTemCotacao() {
        when(produtoInvestimentoRepository.findByCodigoIgnoreCase("CDBELC"))
            .thenReturn(Optional.of(criarProduto("CDBELC", TipoInvestimento.RENDA_FIXA)));

        assertThrows(ConflitoException.class, () -> cotacaoService.buscarCotacao("CDBELC"));
    }

    @Test
    void deveRetornarCotacaoComSucesso() {
        prepararRestClient();

        when(produtoInvestimentoRepository.findByCodigoIgnoreCase("PETR4"))
            .thenReturn(Optional.of(criarProduto("PETR4", TipoInvestimento.ACAO)));

        CotacaoBrapi.Resultado resultado = new CotacaoBrapi.Resultado("PETR4", new BigDecimal("48.20"), "2026-09-08T15:21:30.000Z");
        when(resposta.body(any(org.springframework.core.ParameterizedTypeReference.class)))
            .thenReturn(new CotacaoBrapi(List.of(resultado)));

        CotacaoResposta cotacao = cotacaoService.buscarCotacao("PETR4");

        assertEquals("PETR4", cotacao.codigo());
        assertEquals(new BigDecimal("48.20"), cotacao.precoAtual());
    }

    @Test
    void deveLancarServicoIndisponivelQuandoBrapiFalha() {
        prepararRestClient();

        when(produtoInvestimentoRepository.findByCodigoIgnoreCase("PETR4"))
            .thenReturn(Optional.of(criarProduto("PETR4", TipoInvestimento.ACAO)));

        when(resposta.body(any(org.springframework.core.ParameterizedTypeReference.class)))
            .thenThrow(new RuntimeException("timeout"));

        assertThrows(ServicoExternoIndisponivelException.class, () -> cotacaoService.buscarCotacao("PETR4"));
    }
}
