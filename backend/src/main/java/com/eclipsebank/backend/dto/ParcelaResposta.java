package com.eclipsebank.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.eclipsebank.backend.enums.StatusParcela;
import com.eclipsebank.backend.models.Parcela;

public record ParcelaResposta(
    Long id,
    Integer numero,
    BigDecimal valor,
    LocalDate dataVencimento,
    StatusParcela status,
    LocalDate dataPagamento
) {
    public static ParcelaResposta from(Parcela parcela) {
        return new ParcelaResposta(
            parcela.getId(),
            parcela.getNumero(),
            parcela.getValor(),
            parcela.getDataVencimento(),
            parcela.getStatus(),
            parcela.getDataPagamento()
        );
    }
}
