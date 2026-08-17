package com.eclipsebank.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MetaFinanceiraCadastro(
    @NotBlank(message = "O nome é obrigatório")
    @Size(
        max = 80,
        message = "O nome deve conter no máximo 80 caracteres"
    )
    String nome,

    @NotNull(message = "O valor alvo é  obrigatório")
    @DecimalMin(
        value = "0.01",
        message = "O valor alvo deve ser maior que zero"
    )
    @Digits(
        integer = 17,
        fraction = 2,
        message = "O valor alvo deve conter no maximo duas casas decimais"
    )
    BigDecimal valorAlvo,

    @NotNull(message = "O prazo é obrigatório")
    @FutureOrPresent
    LocalDate prazo

) {
    
}
