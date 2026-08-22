package com.eclipsebank.backend.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.eclipsebank.backend.enums.StatusEmprestimo;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(
    name = "emprestimos",
    indexes = {
        @Index(
            name = "idx_emprestimo_conta",
            columnList = "conta_id"
        )
    }
)
public class Emprestimo {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(
        name = "conta_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_emprestimo_conta")
    )
    private Conta conta;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal valorSolicitado;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal taxaJuros;

    @Column(nullable = false)
    private Integer quantidadeParcelas;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal valorTotal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusEmprestimo status = StatusEmprestimo.SOLICITADO;

    @Column(nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @Column(nullable = false)
    private LocalDateTime atualizdoEm;

    @OneToMany(mappedBy = "emprestimo")
    private List<Parcela> parcela;

    public Emprestimo() {
        
    }

    @PrePersist
    private void antesDeSalvar() {
        LocalDateTime agora = LocalDateTime.now();

        criadoEm = agora;
        atualizdoEm = agora;
    }

    @PreUpdate
    private void antesDeAtualizar() {
        atualizdoEm = LocalDateTime.now();
    }

    public void aprovar() {
        if (status != StatusEmprestimo.SOLICITADO) {
            throw new ConflitoException("Emprestimo precisa estar pendente para aprovar.");
        }

        status = StatusEmprestimo.APROVADO;
    }

    public void iniciar() {
        if (status != StatusEmprestimo.APROVADO) {
            throw new ConflitoException("Emprestimo precisa estar aprovado para iniciar.");
        }

        status = StatusEmprestimo.EM_ANDAMENTO;
    }

    public Long getId() {
        return id;
    }

    public Conta getConta() {
        return conta;
    }

    public BigDecimal getValorSolicitado() {
        return valorSolicitado;
    }

    public BigDecimal getTaxaJuros() {
        return taxaJuros;
    }

    public Integer getQuantidadeParcelas() {
        return quantidadeParcelas;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public StatusEmprestimo getStatus() {
        return status;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public LocalDateTime getAtualizadoEm() {
        return atualizdoEm;
    }

    public List<Parcela> getParcela() {
        return parcela;
    }

    public void setConta(Conta conta) {
        this.conta = conta;
    }

    public void setValorSolicitado(BigDecimal valorSolicitado) {
        this.valorSolicitado = valorSolicitado;
    }

    public void setTaxaJuros(BigDecimal taxaJuros) {
        this.taxaJuros = taxaJuros;
    }

    public void setQuantidadeParcelas(Integer quantidadeParcelas) {
        this.quantidadeParcelas = quantidadeParcelas;
    }

    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    }

    public void setStatus(StatusEmprestimo status) {
        this.status = status;
    }

    public void setAtualizadoEm(LocalDateTime atualizadoEm) {
        this.atualizdoEm = atualizadoEm;
    }

    public void setParcela(List<Parcela> parcela) {
        this.parcela = parcela;
    }
}
