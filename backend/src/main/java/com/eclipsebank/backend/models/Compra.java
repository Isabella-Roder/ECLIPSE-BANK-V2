package com.eclipsebank.backend.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.eclipsebank.backend.enums.CategoriaCompra;

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
import jakarta.persistence.Table;

@Entity
@Table(
    name = "compras",
    indexes = {
        @Index(
            name = "idx_compra_fatura",
            columnList = "fatura_id"
        )
    }
)
public class Compra {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(
        name = "fatura_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_compra_fatura")
    )
    private Fatura fatura;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal valor;

    @Column(length = 180)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(30)")
    private CategoriaCompra categoria;

    @Column(length = 30)
    private String categoriaPersonalizada;

    @Column(nullable = false)
    private LocalDateTime dataCompra;

    public Compra() {

    }

    @PrePersist
    private void antesDeSalvar() {
        dataCompra = LocalDateTime.now();
    }

    public void definirCategoria(CategoriaCompra categoria, String categoriaPersonalizada) {
        if (categoria == CategoriaCompra.OUTROS && (categoriaPersonalizada == null || categoriaPersonalizada.isBlank())) {
            throw new IllegalArgumentException("Informe a categoria personalizada quando a categoria for OUTROS.");
        }

        if (categoria != CategoriaCompra.OUTROS) {
            categoriaPersonalizada = null;
        }

        this.categoria = categoria;
        this.categoriaPersonalizada = categoriaPersonalizada;
    }

    public String getCategoriaExibicao() {
        return categoria == CategoriaCompra.OUTROS ? categoriaPersonalizada : categoria.name();
    }

    public Long getId() {
        return id;
    }

    public Fatura getFatura() {
        return fatura;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public String getDescricao() {
        return descricao;
    }

    public CategoriaCompra getCategoria() {
        return categoria;
    }

    public String getCategoriaPersonalizada() {
        return categoriaPersonalizada;
    }

    public LocalDateTime getDataCompra() {
        return dataCompra;
    }

    public void setFatura(Fatura fatura) {
        this.fatura = fatura;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public void setCategoria(CategoriaCompra categoria) {
        this.categoria = categoria;
    }

    public void setCategoriaPersonalizada(String categoriaPersonalizada) {
        this.categoriaPersonalizada = categoriaPersonalizada;
    }

    public void setDataCompra(LocalDateTime dataCompra) {
        this.dataCompra = dataCompra;
    }
}
