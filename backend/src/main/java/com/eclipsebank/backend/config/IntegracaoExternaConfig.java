package com.eclipsebank.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration 
public class IntegracaoExternaConfig {
    
    @Bean 
    RestClient bancoCentralClient() {
        return RestClient.builder()
            .baseUrl("https://api.bcb.gov.br/dados/serie")
            .build();
    }

    @Bean 
    RestClient brapiRestClient() {
        return RestClient.builder()
            .baseUrl("https://brapi.dev/api")
            .build();
    }

    @Bean 
    RestClient awesomeapiRestClient() {
        return RestClient.builder()
            .baseUrl("https://economia.awesomeapi.com.br/json/last")
            .build();
    }
}
