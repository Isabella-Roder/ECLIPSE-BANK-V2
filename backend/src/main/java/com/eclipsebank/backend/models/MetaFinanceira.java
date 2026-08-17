package com.eclipsebank.backend.models;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.eclipsebank.backend.enums.StatusMetaFinanceira;
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
import jakarta.persistence.Version;

@Entity
@Table(
    name = "metas_financeiras",
    indexes = {
        @Index(
            name = "idx_meta_financeira_conta",
            columnList = "contaId"
        )
    }
)
public class MetaFinanceira {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String nome;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal valorAlvo;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal valorAtual = BigDecimal.ZERO;

    @Column
    private LocalDate prazo;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private StatusMetaFinanceira status;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(
        name = "conta_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_meta_financeira_conta")
    )
    private Conta conta;

    @Column(nullable = false, updatable = false)
    private LocalDateTime criadaEm;

    private LocalDateTime atualizadaEm;

    private LocalDateTime concluidaEm;

    @Version
    private Long versao;

    public MetaFinanceira() {

    }

    @PrePersist
    private void antesDeSalvar() {
        LocalDateTime agora = LocalDateTime.now();

        criadaEm = agora;
        atualizadaEm = agora;
    }

    @PreUpdate
    private void antesDeAtualizar() {
        atualizadaEm = LocalDateTime.now();
    }

    public void aportar(BigDecimal valor) {
        validarValorPositivo(valor);

        if (status != StatusMetaFinanceira.EM_ANDAMENTO) {
            throw new ConflitoException("Meta financeira já concluida ou resgatada.");
        }

        valorAtual = valorAtual.add(valor);

        if (valorAtual.compareTo(valorAlvo) >= 0) {
            status = StatusMetaFinanceira.CONCLUIDA;
            concluidaEm = LocalDateTime.now();
        }
    }

    public void resgatar(BigDecimal valor) {
        validarValorPositivo(valor);

        if (status != StatusMetaFinanceira.EM_ANDAMENTO) {
            throw new ConflitoException("Meta financeira já concluida ou resgatada.");
        }

        if (valorAtual.compareTo(valor) < 0) {
            throw new ConflitoException("Esta meta não tem valor para resgatar.");
        }

        valorAtual = valorAtual.subtract(valor);
    }

    private void validarValorPositivo(BigDecimal valor) {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O valor deve ser maior que zero.");
        }
    }

    public void atualizarMeta(BigDecimal valorAtualizado) {
        if (status != StatusMetaFinanceira.EM_ANDAMENTO) {
            throw new ConflitoException("Meta financeira já concluida ou resgatada");
        }

        validarValorPositivo(valorAtualizado);

        valorAlvo = valorAtualizado;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public BigDecimal getValorAlvo() {
        return valorAlvo;
    }

    public BigDecimal getValorAtual() {
        return valorAtual;
    }

    public LocalDate getPrazo() {
        return prazo;
    }

    public StatusMetaFinanceira getStatus() {
        return status;
    }

    public Conta getConta() {
        return conta;
    }

    public LocalDateTime getCriadaEm() {
        return criadaEm;
    }

    public LocalDateTime getAtualizadaEm() {
        return atualizadaEm;
    }

    public LocalDateTime getConcluidaEm() {
        return concluidaEm;
    }

    public Long getVersao() {
        return versao;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setValorAlvo(BigDecimal valorAlvo) {
        this.valorAlvo = valorAlvo;
    }

    public void setPrazo(LocalDate prazo) {
        this.prazo = prazo;
    }

    public void setStatus(StatusMetaFinanceira status) {
        this.status = status;
    }

    public void setConta(Conta conta) {
        this.conta = conta;
    }

    public void setAtualizadaEm(LocalDateTime atualizadaEm) {
        this.atualizadaEm = atualizadaEm;
    }

    public void setConcluidaEm(LocalDateTime concluidaEm) {
        this.concluidaEm = concluidaEm;
    }


}
