package com.eclipsebank.backend.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
    name = "pagamentos_provento_fii",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_provento_aplicacao_competencia",
        columnNames = {"aplicacao_id", "competencia"}
    )
)
public class PagamentoProventoFii {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "aplicacao_id", nullable = false)
    private AplicacaoInvestimento aplicacao;

    @Column(nullable = false, length = 7)
    private String competencia;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal valor;

    @Column(nullable = false, updatable = false)
    private LocalDateTime pagoEm;

    @PrePersist
    private void antesDeSalvar() {
        pagoEm = LocalDateTime.now();
    }

    public void registrar(AplicacaoInvestimento aplicacao, String competencia, BigDecimal valor) {
        this.aplicacao = aplicacao;
        this.competencia = competencia;
        this.valor = valor;
    }

    public Long getId() { return id; }
    public AplicacaoInvestimento getAplicacao() { return aplicacao; }
    public String getCompetencia() { return competencia; }
    public BigDecimal getValor() { return valor; }
    public LocalDateTime getPagoEm() { return pagoEm; }
}
