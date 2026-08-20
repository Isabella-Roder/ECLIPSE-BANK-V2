package com.eclipsebank.backend.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.eclipsebank.backend.enums.StatusCartao;
import com.eclipsebank.backend.enums.TipoCartao;
import com.eclipsebank.backend.exception.ConflitoException;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(
    name = "cartoes",
    indexes = {
        @Index(
            name = "idx_cartao_conta",
            columnList = "conta_id"
        )
    }
)
public class Cartao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(
        name = "conta_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_cartao_conta")
    )
    private Conta conta;

    @Column(nullable = false, length = 100)
    private String titular;

    @Column(nullable = false, length = 16)
    private String numero;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoCartao tipo;

    @Column(precision = 19, scale = 2)
    private BigDecimal limite;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusCartao status = StatusCartao.ATIVO;

    @Column(nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @Column(nullable = false)
    private LocalDateTime atualizadoEm;

    public Cartao() {

    }

    @PrePersist
    private void antesDeSalvar() {
        LocalDateTime agora = LocalDateTime.now();

        criadoEm = agora;
        atualizadoEm = agora;
    }

    @PreUpdate
    private void antesDeAtualizar() {
        atualizadoEm = LocalDateTime.now();
    }

    public void bloquear() {
        if (status != StatusCartao.ATIVO) {
            throw new ConflitoException("O cartão Precisa estar ativo para bloquear.");
        }

        status = StatusCartao.BLOQUEADO;
    }

    public void desbloquear() {
        if (status != StatusCartao.BLOQUEADO) {
            throw new ConflitoException("O cartão precisa estar bloqueado para desbloquear.");
        }

        status = StatusCartao.ATIVO;
    }

    public void cancelar() {
        if (status == StatusCartao.CANCELADO) {
            throw new ConflitoException("O cartão não pode estar cancelado para cancelar.");
        }

        status = StatusCartao.CANCELADO;
    }

    public Long getId() {
        return id;
    }

    public Conta getConta() {
        return conta;
    }

    public String getTitular() {
        return titular;
    }

    public String getNumero() {
        return numero;
    }

    public TipoCartao getTipo() {
        return tipo;
    }

    public BigDecimal getLimite() {
        return limite;
    }

    public StatusCartao getStatus() {
        return status;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public LocalDateTime getAtualizadoEm() {
        return atualizadoEm;
    }

    public void setConta(Conta conta) {
        this.conta = conta;
    }

    public void setTitular(String titular) {
        this.titular = titular;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public void setTipo(TipoCartao tipo) {
        this.tipo = tipo;
    }

    public void setLimite(BigDecimal limite) {
        this.limite = limite;
    }

    public void setStatus(StatusCartao status) {
        this.status = status;
    }

    public void setAtualizadoEm(LocalDateTime atualizadoEm) {
        this.atualizadoEm = atualizadoEm;
    }
}
