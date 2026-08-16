package com.eclipsebank.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.eclipsebank.backend.enums.TipoInvestimento;
import com.eclipsebank.backend.models.ProdutoInvestimento;

public record ProdutoInvestimentoResposta(
    Long id,
    String nome,
    String codigo,
    TipoInvestimento tipo,
    BigDecimal valorMinimo,
    BigDecimal rentabilidadeAnualEstimada,
    BigDecimal precoCota,
    BigDecimal proventoMensalPorCota,
    boolean ativo,
    LocalDateTime criadoEm
) {
    public static ProdutoInvestimentoResposta from(ProdutoInvestimento produto) {
        return new ProdutoInvestimentoResposta(
            produto.getId(),
            produto.getNome(),
            produto.getCodigo(),
            produto.getTipo(),
            produto.getValorMinimo(),
            produto.getRentabilidadeAnualEstimada(),
            produto.getPrecoCota(),
            produto.getProventoMensalPorCota(),
            produto.getAtivo(),
            produto.getCriadoEm()
        );
    }
}
