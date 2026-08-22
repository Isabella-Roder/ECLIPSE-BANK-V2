package com.eclipsebank.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.eclipsebank.backend.enums.StatusEmprestimo;
import com.eclipsebank.backend.models.Emprestimo;

public record EmprestimoResposta(
    Long id,
    Long contaId,
    BigDecimal valorSolicitado,
    BigDecimal taxaJuros,
    Integer quantidadeParcelas,
    BigDecimal valorTotal,
    StatusEmprestimo status,
    LocalDateTime criadoEm
) {
    public static EmprestimoResposta from(Emprestimo emprestimo) {
        return new EmprestimoResposta(
            emprestimo.getId(),
            emprestimo.getConta().getId(),
            emprestimo.getValorSolicitado(),
            emprestimo.getTaxaJuros(),
            emprestimo.getQuantidadeParcelas(),
            emprestimo.getValorTotal(),
            emprestimo.getStatus(),
            emprestimo.getCriadoEm()
        );
    }
}
