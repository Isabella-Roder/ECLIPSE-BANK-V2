package com.eclipsebank.backend.models;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.eclipsebank.backend.enums.StatusParcela;
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
import jakarta.persistence.Table;

@Entity
@Table(
    name = "parcelas",
    indexes = {
        @Index(
            name = "idx_parcela_emprestimo",
            columnList = "emprestimo_id"
        )
    }
)
public class Parcela {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(
        name = "emprestimo_id",
        nullable = false, 
        foreignKey = @ForeignKey(name = "fk_parcela_emprestimo")
    )
    private Emprestimo emprestimo;

    @Column(nullable = false)
    private Integer numero;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal valor;

    @Column(nullable = false)
    private LocalDate dataVencimento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusParcela status = StatusParcela.PENDENTE;

    private LocalDate dataPagamento;

    public Parcela() {

    }

    public void pagar() {
        if (status != StatusParcela.PENDENTE) {
            throw new ConflitoException("Status precisa estar pendente para pagar.");
        }

        status = StatusParcela.PAGA;
        dataPagamento = LocalDate.now();
    }

    public Long getId() {
        return id;
    }

    public Emprestimo getEmprestimo() {
        return emprestimo;
    }

    public Integer getNumero() {
        return numero;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public LocalDate getDataVencimento() {
        return dataVencimento;
    }

    public StatusParcela getStatus() {
        return status;
    }

    public LocalDate getDataPagamento() {
        return dataPagamento;
    }

    public void setEmprestimo(Emprestimo emprestimo) {
        this.emprestimo = emprestimo;
    }

    public void setNumero(Integer numero) {
        this.numero = numero;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public void setDataVencimento(LocalDate dataVencimento) {
        this.dataVencimento = dataVencimento;
    }

    public void setStatus(StatusParcela status) {
        this.status = status;
    }

    public void setDataPagamento(LocalDate dataPagamento) {
        this.dataPagamento = dataPagamento;
    }
}
