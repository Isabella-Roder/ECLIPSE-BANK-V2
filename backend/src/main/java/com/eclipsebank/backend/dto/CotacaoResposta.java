package com.eclipsebank.backend.dto;

import java.math.BigDecimal;

public record CotacaoResposta(
    String codigo,
    BigDecimal precoAtual,
    String horarioReferencia
) {
    
}
