package com.eclipsebank.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.eclipsebank.backend.models.PagamentoProventoFii;

public record ProventoFiiResposta(
    Long id,
    Long aplicacaoId,
    String produtoNome,
    String competencia,
    BigDecimal valor,
    LocalDateTime pagoEm
) {
    public static ProventoFiiResposta from(PagamentoProventoFii pagamento) {
        return new ProventoFiiResposta(
            pagamento.getId(),
            pagamento.getAplicacao().getId(),
            pagamento.getAplicacao().getProduto().getNome(),
            pagamento.getCompetencia(),
            pagamento.getValor(),
            pagamento.getPagoEm()
        );
    }
}
