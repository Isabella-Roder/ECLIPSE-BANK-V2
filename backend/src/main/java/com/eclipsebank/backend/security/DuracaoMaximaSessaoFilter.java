package com.eclipsebank.backend.security;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component 
public class DuracaoMaximaSessaoFilter extends OncePerRequestFilter{
    
    private static final Duration DURACAO_MAXIMA = Duration.ofHours(8);

    @Override 
    protected void doFilterInternal(
        HttpServletRequest requisicao,
        HttpServletResponse resposta,
        FilterChain cadeia
    ) throws IOException, ServletException {
        HttpSession sessao = requisicao.getSession(false);

        if (sessao != null) {
            Instant criadaEm = (Instant) sessao.getAttribute("criadaEm");

            if (criadaEm != null && Duration.between(criadaEm, Instant.now()).compareTo(DURACAO_MAXIMA) > 0) {
                sessao.invalidate();
                resposta.setStatus(401);
                resposta.setContentType("application/json");
                resposta.getWriter().write("{\"mensagem\":\"Sessão expirada. Faça login novamente.\"}");
                return;
            }
        }

        cadeia.doFilter(requisicao, resposta);
    }
}
