package com.eclipsebank.backend.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

public record UsuarioAtualizacao(
    @Size(
        min = 3,
        max = 100,
        message = "O nome deve ter entre 3 e 100 caracteres."
    )
    String nome,

    @Size(
        min = 3,
        max = 100,
        message = "O nome social deve ter entre 3 e 100 caracteres."
    )
    String nomeSocial,

    @Email(message = "Informe um e-mail válido")
    String email,

    @Size(
        max = 20,
        message = "O telefone deve ter no máximo 20 caracteres."
    )
    String telefone,

    @Past(message = "A data de nascimento deve estar no passado.")
    LocalDate dataNascimento
) {
    
}
