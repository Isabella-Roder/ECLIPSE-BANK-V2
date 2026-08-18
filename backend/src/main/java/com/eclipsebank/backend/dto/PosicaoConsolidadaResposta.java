package com.eclipsebank.backend.dto;

import java.math.BigDecimal;

import com.eclipsebank.backend.enums.TipoInvestimento;

public record PosicaoConsolidadaResposta(
    Long produtoId,
    String produtoNome,
    String produtoCodigo,
    TipoInvestimento produtoTipo,
    BigDecimal quantidadeTotalCotas,
    BigDecimal valorTotalAplicado,
    BigDecimal valorTotalAtual,
    BigDecimal rentabilidadeNominal,
    BigDecimal rentabilidadePercentual,
    BigDecimal precoMedio
) {
    
}
