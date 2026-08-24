package com.eclipsebank.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EmpresaCadastro(
    @NotBlank(message = "O CNPJ é obrgatório.")
    String cnpj,

    @NotBlank(message = "Razão social é obrigatório.")
    @Size(
        max = 80,
        message = "Razão social deve conter no máximo 80 caracteres"
    )
    String razaoSocial,

    @Size(
        max = 80,
        message = "Nome fantasia deve conter no máximo 80 caracteres"
    )
    String nomeFantasia
) {
    
}
