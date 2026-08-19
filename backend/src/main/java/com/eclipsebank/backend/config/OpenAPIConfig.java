package com.eclipsebank.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenAPIConfig {
    
    @Bean
    OpenAPI openAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Eclipse Bank API")
                .description("API bancária educacional com contas, movimentações e investimentos simulados")
                .version("v1")
        );
    }
}
