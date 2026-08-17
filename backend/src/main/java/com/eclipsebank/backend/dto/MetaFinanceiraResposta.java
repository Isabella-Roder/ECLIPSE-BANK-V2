package com.eclipsebank.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.eclipsebank.backend.enums.StatusMetaFinanceira;
import com.eclipsebank.backend.models.MetaFinanceira;

public record MetaFinanceiraResposta(
    Long id,
    Long contaId, 
    String nome,
    BigDecimal valorAlvo,
    BigDecimal valorAtual,
    LocalDate prazo,
    StatusMetaFinanceira status,
    LocalDateTime criadaEm,
    LocalDateTime atualizadaEm,
    LocalDateTime concluidaEm
) {
    public static MetaFinanceiraResposta from(MetaFinanceira metaFinanceira) {
        return new MetaFinanceiraResposta(
            metaFinanceira.getId(),
            metaFinanceira.getConta().getId(),
            metaFinanceira.getNome(),
            metaFinanceira.getValorAlvo(),
            metaFinanceira.getValorAtual(),
            metaFinanceira.getPrazo(),
            metaFinanceira.getStatus(),
            metaFinanceira.getCriadaEm(),
            metaFinanceira.getAtualizadaEm(),
            metaFinanceira.getConcluidaEm()
        );
    }
}
