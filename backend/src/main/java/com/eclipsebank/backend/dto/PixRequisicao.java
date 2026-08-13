package com.eclipsebank.backend.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PixRequisicao(
    @NotBlank(message = "A chave pix é obrigatória")
    String chave,

    @NotNull(message = "O valor é obrigatório")
    @DecimalMin(
        value = "0.01",
        message = "O valor deve ser maior que zero"
    )
    @Digits(
        integer = 17,
        fraction = 2,
        message = "O valor deve possuir no maximo duas casas decimais"
    )
    BigDecimal valor,

    @Size(
        max = 180,
        message = "A descricao deve possuir no máximo 180 caracteres"
    )
    String descricao
) {
    
}
