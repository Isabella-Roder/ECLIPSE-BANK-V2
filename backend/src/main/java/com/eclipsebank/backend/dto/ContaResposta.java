package com.eclipsebank.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.eclipsebank.backend.enums.StatusConta;
import com.eclipsebank.backend.models.Conta;

public record ContaResposta(
    Long id,
    String agencia,
    String numero,
    BigDecimal saldo,
    StatusConta status,
    Long usuarioId,
    String titular,
    LocalDateTime criadaEm,
    LocalDateTime atualizadaEm
) {
    public static ContaResposta from(Conta conta) {
        String nomeExibicao = conta.getUsuario().getNomeSocial() != null
            ? conta.getUsuario().getNomeSocial()
            : conta.getUsuario().getNome();

        return new ContaResposta(
            conta.getId(),
            conta.getAgencia(),
            conta.getNumero(),
            conta.getSaldo(),
            conta.getStatus(),
            conta.getUsuario().getId(),
            nomeExibicao,
            conta.getCriadaEm(),
            conta.getAtualizadaEm()
        );
    }
}
