package com.eclipsebank.backend.dto;

import com.eclipsebank.backend.enums.TipoCartao;

import jakarta.validation.constraints.NotNull;

public record CartaoCadastro(
    @NotNull(message = "O tipo do cartão é obrigatório.")
    TipoCartao tipo
) {
    
}
