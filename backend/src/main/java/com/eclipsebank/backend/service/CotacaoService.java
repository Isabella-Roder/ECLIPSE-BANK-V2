package com.eclipsebank.backend.service;

import java.util.Set;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.eclipsebank.backend.dto.CotacaoBrapi;
import com.eclipsebank.backend.dto.CotacaoResposta;
import com.eclipsebank.backend.enums.TipoInvestimento;
import com.eclipsebank.backend.exception.ConflitoException;
import com.eclipsebank.backend.exception.RecursoNaoEncontradoException;
import com.eclipsebank.backend.exception.ServicoExternoIndisponivelException;
import com.eclipsebank.backend.models.ProdutoInvestimento;
import com.eclipsebank.backend.repository.ProdutoInvestimentoRepository;

@Service 
public class CotacaoService {
    
    private static final Set<TipoInvestimento> TIPOS_COM_COTACAO = Set.of(
        TipoInvestimento.ACAO,
        TipoInvestimento.ETF,
        TipoInvestimento.FUNDO_IMOBILIARIO
    );

    private final RestClient brapiRestClient;
    private final ProdutoInvestimentoRepository produtoInvestimentoRepository;

    public CotacaoService(
        @Qualifier("brapiRestClient") RestClient brapiRestClient,
        ProdutoInvestimentoRepository produtoInvestimentoRepository
    ) {
        this.brapiRestClient = brapiRestClient;
        this.produtoInvestimentoRepository = produtoInvestimentoRepository;
    }

    public CotacaoResposta buscarCotacao(String codigo) {
        ProdutoInvestimento produto = produtoInvestimentoRepository.findByCodigoIgnoreCase(codigo)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Produto não encontrado com o Codigo: " + codigo));

        if (!TIPOS_COM_COTACAO.contains(produto.getTipo())) {
            throw new ConflitoException("Este produto não possui cotação em tempo real.");
        }

        try {
            CotacaoBrapi resposta = brapiRestClient.get()
                .uri("/quote/{codigo}", codigo)
                .retrieve()
                .body(new ParameterizedTypeReference<CotacaoBrapi>() {});

            if (resposta == null || resposta.results() == null || resposta.results().isEmpty()) {
                throw new ServicoExternoIndisponivelException("Cotação não encontrada para " + codigo);
            }

            CotacaoBrapi.Resultado resultado = resposta.results().get(0);

            return new CotacaoResposta(
                resultado.symbol(),
                resultado.regularMarketPrice(),
                resultado.regularMarketTime()
            );
        } catch (ServicoExternoIndisponivelException excecao) {
            throw excecao;
        } catch (Exception excecao) {
            throw new ServicoExternoIndisponivelException("Não foi possivel consultar a cotação de " + codigo);
        }
    }
}
