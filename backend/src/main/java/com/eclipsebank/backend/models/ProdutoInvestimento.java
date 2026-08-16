package com.eclipsebank.backend.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.eclipsebank.backend.enums.TipoInvestimento;

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
@Table(name = "produtos_investimento")
public class ProdutoInvestimento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String nome;

    @Column(nullable = false, unique = true, length = 10)
    private String codigo;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TipoInvestimento tipo;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal valorMinimo;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal rentabilidadeAnualEstimada;

    @Column(precision = 19, scale = 2)
    private BigDecimal precoCota;

    @Column(precision = 19, scale = 2)
    private BigDecimal proventoMensalPorCota;

    @Column(nullable = false)
    private boolean ativo = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    public ProdutoInvestimento() {

    }

    @PrePersist
    private void antesDeSalvar() {
        criadoEm = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getCodigo() {
        return codigo;
    }

    public TipoInvestimento getTipo() {
        return tipo;
    }

    public BigDecimal getValorMinimo() {
        return valorMinimo;
    }

    public BigDecimal getRentabilidadeAnualEstimada() {
        return rentabilidadeAnualEstimada;
    }

    public BigDecimal getPrecoCota() {
        return precoCota;
    }

    public BigDecimal getProventoMensalPorCota() {
        return proventoMensalPorCota;
    }

    public boolean getAtivo() {
        return ativo;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public void setTipo(TipoInvestimento tipo) {
        this.tipo = tipo;
    }

    public void setValorMinimo(BigDecimal valorMinimo) {
        this.valorMinimo = valorMinimo;
    }

    public void setRentabilidadeAnualEstimada(BigDecimal rentabilidadeAnualEstimada) {
        this.rentabilidadeAnualEstimada = rentabilidadeAnualEstimada;
    }

    public void setPrecoCota(BigDecimal precoCota) {
        this.precoCota = precoCota;
    }

    public void setProventoMensalPorCota(BigDecimal proventoMensalPorCota) {
        this.proventoMensalPorCota = proventoMensalPorCota;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

}
