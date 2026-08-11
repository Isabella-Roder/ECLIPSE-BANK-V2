package com.eclipsebank.backend.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.eclipsebank.backend.enums.PerfilUsuario;
import com.eclipsebank.backend.models.Usuario;

public record UsuarioResposta(
    Long id,
    String nome,
    String nomeSocial,
    String email,
    String telefone,
    LocalDate dataNascimento,
    PerfilUsuario perfil,
    boolean ativo,
    LocalDateTime criadoEm
) {
    
    public static UsuarioResposta from(Usuario usuario) {
        return new UsuarioResposta(
            usuario.getId(),
            usuario.getNome(),
            usuario.getNomeSocial(),
            usuario.getEmail(),
            usuario.getTelefone(),
            usuario.getDataNascimento(),
            usuario.getPerfil(),
            usuario.getAtivo(),
            usuario.getCriadoEm()
        );
    }
}
