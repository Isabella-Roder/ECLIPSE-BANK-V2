package com.eclipsebank.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.eclipsebank.backend.enums.StatusConta;
import com.eclipsebank.backend.enums.TipoConta;
import com.eclipsebank.backend.models.Conta;

public record ContaResposta(
    Long id,
    String agencia,
    String numero,
    BigDecimal saldo,
    StatusConta status,
    TipoConta tipo,
    Long usuarioId,
    Long empresaId,
    String titular,
    LocalDateTime criadaEm,
    LocalDateTime atualizadaEm
) {
    public static ContaResposta from(Conta conta) {
        String titular;
        Long usuarioId = null;
        Long empresaId = null;

        if (conta.getUsuario() != null) {
            titular = conta.getUsuario().getNomeSocial() != null
                ? conta.getUsuario().getNomeSocial()
                : conta.getUsuario().getNome();
            usuarioId = conta.getUsuario().getId();
        } else {
            titular = conta.getEmpresa().getNomeFantasia() != null
                ? conta.getEmpresa().getNomeFantasia()
                : conta.getEmpresa().getRazaoSocial();
            empresaId = conta.getEmpresa().getId();
        }

        return new ContaResposta(
            conta.getId(),
            conta.getAgencia(),
            conta.getNumero(),
            conta.getSaldo(),
            conta.getStatus(),
            conta.getTipo(),
            usuarioId,
            empresaId,
            titular,
            conta.getCriadaEm(),
            conta.getAtualizadaEm()
        );
    }
}
