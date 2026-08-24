package com.eclipsebank.backend.dto;

import com.eclipsebank.backend.models.Empresa;

public record EmpresaResposta(
    Long id,
    String cnpj,
    String razaoSocial,
    String nomeFantasia
) {
    public static EmpresaResposta from(Empresa empresa) {
        return new EmpresaResposta(
            empresa.getId(),
            empresa.getCnpj(),
            empresa.getRazaoSocial(),
            empresa.getNomeFantasia()
        );
    }
}
