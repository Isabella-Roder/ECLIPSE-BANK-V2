package com.eclipsebank.backend.controller;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;

@Tag(name = "CSRF", description = "Emissão de token CSRF para o aplicativo mobile")
@RestController
public class CsrfController {

    private record CsrfTokenResposta(String token) {
    }

    @Operation(summary = "Obter token CSRF", description = "Retorna um token CSRF válido no corpo da resposta, usado pelo aplicativo mobile antes de operações que alteram dados")
    @GetMapping("/api/csrf")
    public CsrfTokenResposta obterToken(HttpServletRequest requisicao) {
        CsrfToken csrfToken = (CsrfToken) requisicao.getAttribute("_csrf");

        return new CsrfTokenResposta(csrfToken.getToken());
    }
}
