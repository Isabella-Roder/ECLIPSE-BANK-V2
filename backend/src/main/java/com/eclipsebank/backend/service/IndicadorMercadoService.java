package com.eclipsebank.backend.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.eclipsebank.backend.dto.IndicadorBancoCentral;
import com.eclipsebank.backend.dto.IndicadorMercadoResposta;
import com.eclipsebank.backend.exception.ServicoExternoIndisponivelException;

@Service 
public class IndicadorMercadoService {
    
    private static final DateTimeFormatter FORMATO_DATA_BCB = DateTimeFormatter.ofPattern("dd/MM/yyyy"); 
    private static final int SERIE_SELIC = 11;
    private static final int SERIE_CDI = 12;
    private static final int SERIE_IPCA = 433;

    private final RestClient bancoCentralRestClient;

    public IndicadorMercadoService(@Qualifier("bancoCentralClient") RestClient bancoCentralRestClient) {
        this.bancoCentralRestClient = bancoCentralRestClient;
    }

    private IndicadorMercadoResposta buscarUltimoValor(int codigoSerie, String nome) {
        try {
            List<IndicadorBancoCentral> dados = bancoCentralRestClient.get()
                .uri("/bcdata.sgs.{codigo}/dados/ultimos/1?formato=json", codigoSerie)
                .retrieve()
                .body(new ParameterizedTypeReference<List<IndicadorBancoCentral>>() {});

            if (dados == null || dados.isEmpty()) {
                throw new ServicoExternoIndisponivelException("Banco Central não retornou dados para " + nome);
            }

            IndicadorBancoCentral ultimo = dados.get(dados.size() - 1);

            return new IndicadorMercadoResposta(
                nome,
                new BigDecimal(ultimo.valor().replace(",", ".")),
                LocalDate.parse(ultimo.data(), FORMATO_DATA_BCB)
            );
        } catch (Exception excecao) {
            throw new ServicoExternoIndisponivelException("Não foi possivel consultar " + nome + " no Banco Central");
        }
    }

    public IndicadorMercadoResposta buscarSelic() {
        return buscarUltimoValor(SERIE_SELIC, "Selic");
    }

    public IndicadorMercadoResposta buscarCDI() {
        return buscarUltimoValor(SERIE_CDI, "CDI");
    }

    public IndicadorMercadoResposta buscarIPCA() {
        return buscarUltimoValor(SERIE_IPCA, "IPCA");
    }
}
