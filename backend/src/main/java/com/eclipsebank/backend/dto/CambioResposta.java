package com.eclipsebank.backend.dto;

import java.math.BigDecimal;

public record CambioResposta(
    String moeda,
    BigDecimal valorEmReais,
    String horarioReferencia
) {
    
}
