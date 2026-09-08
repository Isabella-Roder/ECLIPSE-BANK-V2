package com.eclipsebank.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration 
public class BancoCentralConfig {
    
    @Bean 
    RestClient bancoCentralClient() {
        return RestClient.builder()
            .baseUrl("https://api.bcb.gov.br/dados/serie")
            .build();
    }
}
