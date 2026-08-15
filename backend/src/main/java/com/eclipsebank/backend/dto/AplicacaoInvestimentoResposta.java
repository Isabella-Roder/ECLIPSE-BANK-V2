package com.eclipsebank.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.eclipsebank.backend.enums.StatusAplicacaoInvestimento;
import com.eclipsebank.backend.enums.TipoInvestimento;
import com.eclipsebank.backend.models.AplicacaoInvestimento;

public record AplicacaoInvestimentoResposta(
    Long id,
    Long contaId,
    Long produtoId,
    String produtoNome,
    String produtoCodigo,
    TipoInvestimento produtoTipo,
    BigDecimal rentabilidadeAnualEstimada,
    BigDecimal valorAplicado,
    BigDecimal saldoInvestido,
    StatusAplicacaoInvestimento status,
    LocalDateTime aplicadaEm,
    LocalDateTime atualizadaEm,
    LocalDateTime resgatadaEm
) {
    public static AplicacaoInvestimentoResposta from(AplicacaoInvestimento aplicacao) {
        return new AplicacaoInvestimentoResposta(
            aplicacao.getId(),
            aplicacao.getConta().getId(),
            aplicacao.getProduto().getId(),
            aplicacao.getProduto().getNome(),
            aplicacao.getProduto().getCodigo(),
            aplicacao.getProduto().getTipo(),
            aplicacao.getProduto().getRentabilidadeAnualEstimada(),
            aplicacao.getValorAplicado(),
            aplicacao.getSaldoInvestido(),
            aplicacao.getStatus(),
            aplicacao.getAplicadaEm(),
            aplicacao.getAtualizadaEm(),
            aplicacao.getResgatadaEm()
        );
    }
}
