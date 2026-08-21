package com.eclipsebank.backend.dto;

import java.math.BigDecimal;

import com.eclipsebank.backend.enums.CategoriaCompra;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CompraCadastro(
    @NotNull(message = "O valor é obrigatório")
    @DecimalMin(
        value = "0.01",
        message = "O valor deve ser maior que zero"
    )
    @Digits(
        integer = 17,
        fraction = 2,
        message = "O valor deve conter no máximo duas casas decimais"
    )
    BigDecimal valor,

    @Size(
        max = 180,
        message = "A descrição deve conter no máximo 180 caracteres"
    )
    String descricao,

    @NotNull(message = "A categoria é obrigatória")
    CategoriaCompra categoria,

    @Size(
        max = 30,
        message = "A categoria personalizada deve conter no máximo 30 caracteres"
    )
    String categoriaPersonalizada
) {
    
}
