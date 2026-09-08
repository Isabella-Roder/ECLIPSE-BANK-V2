package com.eclipsebank.backend.security;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(
        HttpServletRequest requisicao,
        HttpServletResponse resposta,
        FilterChain cadeia
    ) throws IOException, ServletException {

        if (!rotaSensivel(requisicao)) {
            cadeia.doFilter(requisicao, resposta);
            return;
        }

        String chave = requisicao.getRemoteAddr() + ":" + requisicao.getRequestURI();
        Bucket bucket = buckets.computeIfAbsent(chave, k -> criarBucket());

        if (bucket.tryConsume(1)) {
            cadeia.doFilter(requisicao, resposta);
        } else {
            resposta.setStatus(429);
            resposta.setContentType("application/json");
            resposta.getWriter().write("{\"mensagem\":\"Muitas requisições. Tente novamente em instantes.\"}");
        }
    }

    private boolean rotaSensivel(HttpServletRequest requisicao) {
        String uri = requisicao.getRequestURI();

        return uri.equals("/api/usuarios/login")
            || uri.matches("/api/contas/\\d+/pix")
            || uri.matches("/api/contas/\\d+/transferencias");
    }

    private Bucket criarBucket() {
        Bandwidth limite = Bandwidth.classic(10, Refill.intervally(10, Duration.ofMinutes(1)));
        return Bucket.builder().addLimit(limite).build();
    }
}
