package com.eclipsebank.backend.security;

import java.io.IOException;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CsrfCookieFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
        HttpServletRequest requisicao,
        HttpServletResponse resposta,
        FilterChain cadeia
    ) throws ServletException, IOException {
        CsrfToken csrfToken = (CsrfToken) requisicao.getAttribute("_csrf");

        if (csrfToken != null) {
            csrfToken.getToken();
        }

        cadeia.doFilter(requisicao, resposta);
    }
}
