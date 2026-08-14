package com.eclipsebank.backend.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.eclipsebank.backend.exception.CredenciaisInvalidasException;

@Component
public class UsuarioAutenticado {
    
    public Long obterId(Authentication autenticacao) {
        if (
            autenticacao == null ||
            !autenticacao.isAuthenticated() ||
            !(autenticacao.getPrincipal() instanceof Long usuarioId)
        ) {
            throw new CredenciaisInvalidasException();
        }

        return usuarioId;
    }

    public void validarAcessoAoUsuario(Authentication autenticacao, Long usuarioIdSolicitado) {
        Long usuarioIdAutenticado = obterId(autenticacao);

        boolean administrador = autenticacao.getAuthorities().stream()
            .anyMatch(autoridade ->
                autoridade.getAuthority().equals("ROLE_ADMIN")
            );

        if (!administrador && !usuarioIdAutenticado.equals(usuarioIdSolicitado)) {
            throw new AccessDeniedException("Você não possui acesso a este usuário");
        }
    }
}
