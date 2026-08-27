package com.eclipsebank.backend.models;

import java.time.LocalDateTime;

import com.eclipsebank.backend.enums.AcaoAuditoria;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "registros_auditoria")
public class RegistroAuditoria {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id")
    private Long usuarioId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AcaoAuditoria acao;

    private String descricao;

    @Column(nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    public RegistroAuditoria() {

    }

    public RegistroAuditoria(
        Long usuarioId,
        AcaoAuditoria acao,
        String descricao
    ) {
        this.usuarioId = usuarioId;
        this.acao = acao;
        this.descricao = descricao;
    }

    @PrePersist
    private void antesDeSalvar() {
        criadoEm = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public AcaoAuditoria getAcao() {
        return acao;
    }

    public String getDescricao() {
        return descricao;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }
}
