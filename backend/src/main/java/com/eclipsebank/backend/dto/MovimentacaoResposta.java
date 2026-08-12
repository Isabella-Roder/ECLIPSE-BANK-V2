package com.eclipsebank.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.eclipsebank.backend.enums.StatusMovimentacao;
import com.eclipsebank.backend.enums.TipoMovimentacao;
import com.eclipsebank.backend.models.Movimentacao;

public record MovimentacaoResposta(
    Long id,
    String codigo,
    Long contaId,
    TipoMovimentacao tipo,
    StatusMovimentacao status,
    BigDecimal valor,
    BigDecimal saldoResultante,
    String descricao,
    LocalDateTime criadaEm
) {

    public static MovimentacaoResposta from(Movimentacao movimentacao) {
        return new MovimentacaoResposta(
            movimentacao.getId(),
            movimentacao.getCodigo(),
            movimentacao.getConta().getId(),
            movimentacao.getTipo(),
            movimentacao.getStatus(),
            movimentacao.getValor(),
            movimentacao.getSaldoResultante(),
            movimentacao.getDescricao(),
            movimentacao.getCriadaEm()
        );
    }
    
}
