package com.eclipsebank.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.eclipsebank.backend.models.Compra;

public record CompraResposta(
    Long id,
    Long faturaId,
    BigDecimal valor,
    String descricao,
    String categoria,
    LocalDateTime dataCompra
) {
    public static CompraResposta from(Compra compra) {
        return new CompraResposta(
            compra.getId(),
            compra.getFatura().getId(),
            compra.getValor(),
            compra.getDescricao(),
            compra.getCategoriaExibicao(),
            compra.getDataCompra()
        );
    }
}
