package com.eclipsebank.backend.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

public record UsuarioCadastro(
    @NotBlank(message = "O nome é obrigatório.")
    @Size(
        min = 3,
        max = 100,
        message = "O nome deve ter entre 3 a 100 caracteres."
    )
    String nome,

    @Size(
        min = 3,
        max = 100,
        message = "O nome social deve ter entre 3 a 100 caracteres."
    )
    String nomeSocial,

    @NotBlank(message = "O CPF é obrigatório.")
    String cpf,

    @NotBlank(message = "O e-mail é obrigatório.")
    @Email(message = "Infome um e-mail válido.")
    String email,

    @NotBlank(message = "A senha é obrigatória.")
    @Size(
        min = 8,
        max = 72,
        message = "A senha deve ter entre 8 a 72 caracteres."
    )
    String senha,

    String telefone,

    @Past(message = "A data de nascimento deve estar no passado")
    LocalDate dataNascimento
) {
    
}
