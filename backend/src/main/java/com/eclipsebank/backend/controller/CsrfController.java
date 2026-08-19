package com.eclipsebank.backend.controller;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

@RestController
public class CsrfController {

    private record CsrfTokenResposta(String token) {
    }
    
    @GetMapping("/api/csrf")
    public CsrfTokenResposta obterToken(HttpServletRequest requisicao) {
        CsrfToken csrfToken = (CsrfToken) requisicao.getAttribute("_csrf");

        return new CsrfTokenResposta(csrfToken.getToken());
    }
}
