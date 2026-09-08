package com.eclipsebank.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record IndicadorMercadoResposta(
    String nome,
    BigDecimal valor,
    LocalDate dataReferencia
) {
    
}
