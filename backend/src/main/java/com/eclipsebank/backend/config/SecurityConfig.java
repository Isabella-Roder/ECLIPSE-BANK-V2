package com.eclipsebank.backend.config;

import static org.springframework.security.config.Customizer.withDefaults;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy;

import com.eclipsebank.backend.security.CsrfCookieFilter;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    SecurityFilterChain securityFilterChain(
        HttpSecurity http,
        SecurityContextRepository securityContextRepository,
        CsrfCookieFilter csrfCookieFilter
    ) throws Exception {
        http
            .cors(withDefaults())
            .securityContext(contexto -> contexto
                .securityContextRepository(securityContextRepository)
            )
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
            )
            .addFilterAfter(csrfCookieFilter, BasicAuthenticationFilter.class)
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable())
            .exceptionHandling(excecoes -> excecoes
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
            ).headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin())
                .httpStrictTransportSecurity(hsts -> hsts
                    .includeSubDomains(true)
                    .maxAgeInSeconds(31536000)
                )
                .referrerPolicy(refferer -> refferer
                    .policy(ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
                )
                .contentSecurityPolicy(csp -> csp
                    .policyDirectives("default-src 'self'")
                )
            ).logout(logout -> logout
                .logoutUrl("/api/usuarios/logout")
                .logoutSuccessHandler((requisicao, resposta, autenticacao) ->
                    resposta.setStatus(HttpStatus.NO_CONTENT.value())
                ).permitAll()
            )
            .authorizeHttpRequests(autorizacao -> autorizacao
                .requestMatchers(
                    HttpMethod.POST,
                    "/api/usuarios",
                    "/api/usuarios/login"
                ).permitAll()
                .requestMatchers(HttpMethod.GET,
                    "/api/csrf"
                ).permitAll()
                .requestMatchers(
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html"
                ).permitAll()
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/investimentos/produtos").hasRole("ADMIN")
                .anyRequest().authenticated()
            );

        return http.build();
    }
}
