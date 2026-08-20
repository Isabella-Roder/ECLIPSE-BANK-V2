package com.eclipsebank.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.eclipsebank.backend.enums.StatusCartao;
import com.eclipsebank.backend.enums.TipoCartao;
import com.eclipsebank.backend.models.Cartao;

public record CartaoResposta(
    Long id,
    Long contaId,
    String titular,
    String numero,
    TipoCartao tipo,
    BigDecimal limite,
    StatusCartao status,
    LocalDateTime criadoEm
) {
    public static CartaoResposta from(Cartao cartao) {
        return new CartaoResposta(
            cartao.getId(),
            cartao.getConta().getId(),
            cartao.getTitular(),
            cartao.getNumero(),
            cartao.getTipo(),
            cartao.getLimite(),
            cartao.getStatus(),
            cartao.getCriadoEm()
        );
    }    
}
