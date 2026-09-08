package com.eclipsebank.backend.service;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.eclipsebank.backend.dto.CambioResposta;
import com.eclipsebank.backend.dto.CotacaoCambio;
import com.eclipsebank.backend.exception.ServicoExternoIndisponivelException;

@Service 
public class CambioService {
    
    private final RestClient awesomeapiRestClient;

    public CambioService(@Qualifier("awesomeapiRestClient") RestClient awesomeapiRestClient) {
        this.awesomeapiRestClient = awesomeapiRestClient;
    }

    public CambioResposta buscarCotacao(String moeda) {
        String par = moeda.toUpperCase() + "-BRL";
        String chave = moeda.toUpperCase() + "BRL";

        try {
            Map<String, CotacaoCambio> resposta = awesomeapiRestClient.get()
                .uri("/{par}", par)
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, CotacaoCambio>>() {});

            if (resposta == null || !resposta.containsKey(chave)) {
                throw new ServicoExternoIndisponivelException("Câmbio não encontrado para " + moeda);
            }

            CotacaoCambio cotacao = resposta.get(chave);

            return new CambioResposta(
                moeda.toUpperCase(),
                new BigDecimal(cotacao.bid()),
                cotacao.timestamp()
            );
        } catch (ServicoExternoIndisponivelException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new ServicoExternoIndisponivelException("Não foi possivel consultar o câmbio de " + moeda);
        }
    }
}
