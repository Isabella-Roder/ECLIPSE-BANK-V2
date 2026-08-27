package com.eclipsebank.backend.dto;

import java.time.LocalDateTime;

import com.eclipsebank.backend.enums.AcaoAuditoria;
import com.eclipsebank.backend.models.RegistroAuditoria;

public record RegistroAuditoriaResposta(
    Long id,
    Long usuarioId,
    AcaoAuditoria acao,
    String descricao,
    LocalDateTime criadoEm
) {
    public static RegistroAuditoriaResposta from(RegistroAuditoria auditoria) {
        return new RegistroAuditoriaResposta(
            auditoria.getId(),
            auditoria.getUsuarioId(),
            auditoria.getAcao(),
            auditoria.getDescricao(),
            auditoria.getCriadoEm()
        );
    }
}
