package com.eclipsebank.backend.dto;

import java.math.BigDecimal;

import com.eclipsebank.backend.enums.TipoInvestimento;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProdutoInvestimentoCadastro(
    @NotBlank(message = "O nome é obrigatório")
    @Size(
        max = 80,
        message = "O nome deve conter no máximo 80 caracteres"
    )
    String nome,

    @NotBlank(message = "O código é obrigatório")
    @Size(
        max = 10,
        message = "O codigo deve conter no máximo 10 caracteres"
    )
    String codigo,

    @NotNull(message = "O tipo é obrigatório")
    TipoInvestimento tipo,

    @NotNull(message = "O valor minimo é obrigatório")
    @DecimalMin(
        value = "0.01",
        message = "O valor minimo deve ser maior que zero"
    )
    @Digits(
        integer = 17,
        fraction = 2,
        message = "O valor minimo deve possuir no máximo duas casas decimas"
    )
    BigDecimal valorMinimo,

    @NotNull(message = "A rentabilidade é obrigatória")
    @DecimalMin(
        value = "0.00",
        message = "A rentabilidade não pode ser negativa"
    )
    @Digits(
        integer = 5,
        fraction = 2,
        message = "A rentabilidade deve possuir no máximo duas casas decimais"
    )
    BigDecimal rentabilidadeAnualEstimada,

    @DecimalMin(
        value = "0.01",
        message = "O valor da cota deve ser maior que zero"
    )
    @Digits(
        integer = 17,
        fraction = 2,
        message = "O valor da cota deve possuir no máximo duas casas decimais"
    )
    BigDecimal precoCota,

    @DecimalMin(
        value = "0.01",
        message = "O valor mensal por cota deve ser maior que zero"
    )
    @Digits(
        integer = 17,
        fraction = 2,
        message = "O valor mensal por cota deve possuir no maximo duas casas decimais"
    )
    BigDecimal proventoMensalPorCota
    
) {
    
}
