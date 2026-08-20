package com.eclipsebank.backend.models;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.eclipsebank.backend.enums.StatusFatura;
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
    name = "faturas",
    indexes = {
        @Index(
            name = "idx_fatura_cartao",
            columnList = "cartao_id"
        )
    }
)
public class Fatura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(
        name = "cartao_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_fatura_cartao")
    )
    private Cartao cartao;

    @Column(nullable = false)
    private String mesReferencia;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal valorTotal = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusFatura status = StatusFatura.ABERTA;

    @Column(nullable = false)
    private LocalDate dataVencimento;

    @Column(nullable = false, updatable = false)
    private LocalDateTime criadaEm;

    @Column(nullable = false)
    private LocalDateTime atualizadaEm;

    public Fatura() {

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

    public void lancarCompra(BigDecimal valor) {
        if (status != StatusFatura.ABERTA) {
            throw new ConflitoException("A fatura precisa estar ativa.");
        }

        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O valor deve ser maior que zero.");
        }

        valorTotal = valorTotal.add(valor);
    }

    public void fechar() {
        if (status != StatusFatura.ABERTA) {
            throw new ConflitoException("A fatura precisa estar aberta para fechar.");
        }

        status = StatusFatura.FECHADA;
    }

    public void pagar() {
        if (status != StatusFatura.FECHADA) {
            throw new ConflitoException("A fatura precisa estar fechada para pagar.");
        }

        status = StatusFatura.PAGA;
    }

    public Long getId() {
        return id;
    }

    public Cartao getCartao() {
        return cartao;
    }

    public String getMesReferencia() {
        return mesReferencia;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public StatusFatura getStatus() {
        return status;
    }

    public LocalDate getDataVencimento() {
        return dataVencimento;
    }

    public LocalDateTime getCriadaEm() {
        return criadaEm;
    }

    public LocalDateTime getAtualizadaEm() {
        return atualizadaEm;
    }

    public void setCartao(Cartao cartao) {
        this.cartao = cartao;
    }

    public void setMesReferencia(String mesReferencia) {
        this.mesReferencia = mesReferencia;
    }

    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    }

    public void setStatus(StatusFatura status) {
        this.status = status;
    }

    public void setDataVencimento(LocalDate dataVencimento) {
        this.dataVencimento = dataVencimento;
    }

    public void setAtualizadaEm(LocalDateTime atualizadaEm) {
        this.atualizadaEm = atualizadaEm;
    }
    
}
