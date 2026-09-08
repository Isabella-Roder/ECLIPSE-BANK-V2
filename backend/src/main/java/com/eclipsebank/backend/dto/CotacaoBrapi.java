package com.eclipsebank.backend.dto;

import java.math.BigDecimal;
import java.util.List;

public record CotacaoBrapi(
    List<Resultado> results
) {
    public record Resultado(
        String symbol,
        BigDecimal regularMarketPrice,
        String regularMarketTime
    ) {
    }
}
