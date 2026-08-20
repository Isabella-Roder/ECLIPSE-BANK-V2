package com.eclipsebank.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.eclipsebank.backend.enums.StatusFatura;
import com.eclipsebank.backend.models.Fatura;

public record FaturaResposta(
    Long id,
    Long cartaoId,
    String mesReferencia,
    BigDecimal valorTotal,
    StatusFatura status,
    LocalDate dataVencimento,
    LocalDateTime criadaEm,
    LocalDateTime atualizadaEm
) {
    public static FaturaResposta from(Fatura fatura) {
        return new FaturaResposta(
            fatura.getId(),
            fatura.getCartao().getId(),
            fatura.getMesReferencia(),
            fatura.getValorTotal(),
            fatura.getStatus(),
            fatura.getDataVencimento(),
            fatura.getCriadaEm(),
            fatura.getAtualizadaEm()
        );
    }
}
