package com.eclipsebank.backend.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record MetaFinanceiraRequisicao(
    @NotNull(message = "A meta financeira é obrigatória")
    @Positive(message = "A meta financeira é obrigatória")
    Long metaId,

    @NotNull(message =  "O valor é obrigatório")
    @DecimalMin(
        value = "0.01",
        message = "O valor deve ser maior que zero"
    )
    @Digits(
        integer = 17,
        fraction = 2,
        message = "O valor deve conter no máximo duas casas decimais"
    )
    BigDecimal valor
) {
    
}
