package com.eclipsebank.backend.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record EmprestimoSolicitacao(
    @NotNull(message = "Valor solicitado é obrigatório")
    @DecimalMin(
        value = "0.01",
        message = "O Valor solicitado deve ser maior que zero"
    )
    @Digits(
        integer = 17,
        fraction = 2,
        message = "O valor solicitado deve conter no máximo duas casas decimais"
    )
    BigDecimal valorSolicitado,

    @NotNull(message = "Taxa de juros é obrigatório")
    @DecimalMin(
        value = "0.01",
        message = "Taxa de juros deve ser maior que zero"
    )
    @Digits(
        integer = 17,
        fraction = 2,
        message = "Taxa de juros deve conter no máximo duas casas decimais"
    )
    BigDecimal taxaJuros,

    @NotNull(message = "Quantidade de parcelas é obrigatório")
    @Min(1)
    Integer quantidadeParcelas
) {
    
}
