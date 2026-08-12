package com.eclipsebank.backend.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.eclipsebank.backend.enums.StatusMovimentacao;
import com.eclipsebank.backend.enums.TipoMovimentacao;

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
    name = "movimentacoes",
    indexes = {
        @Index(
            name = "idx_movimentacao_conta_data",
            columnList = "conta_id, criada_em"
        ),
        @Index(
            name = "idx_movimentacao_codigo",
            columnList = "codigo",
            unique = true
        )
    }
)
public class Movimentacao {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false, length = 36)
    private String codigo;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(
        name = "conta_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_movimentacao_conta")
    )
    private Conta conta;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoMovimentacao tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusMovimentacao status = StatusMovimentacao.CONCLUIDA;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal valor;

    @Column(name = "saldo_resultante", nullable = false, precision = 19, scale = 2)
    private BigDecimal saldoResultante;

    @Column(length = 180)
    private String descricao;

    @Column(name = "criada_em", nullable = false, updatable = false)
    private LocalDateTime criadaEm;

    public Movimentacao() {

    }

    @PrePersist
    private void antesDeSalvar() {
        if (codigo == null) {
            codigo = UUID.randomUUID().toString();
        }

        criadaEm = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getCodigo() {
        return codigo;
    }

    public Conta getConta() {
        return conta;
    }

    public TipoMovimentacao getTipo() {
        return tipo;
    }

    public StatusMovimentacao getStatus() {
        return status;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public BigDecimal getSaldoResultante() {
        return saldoResultante;
    }

    public String getDescricao() {
        return descricao;
    }

    public LocalDateTime getCriadaEm() {
        return criadaEm;
    }

    public void setConta(Conta conta) {
        this.conta = conta;
    }

    public void setTipo(TipoMovimentacao tipo) {
        this.tipo = tipo;
    }

    public void setStatus(StatusMovimentacao status) {
        this.status = status;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public void setSaldoResultante(BigDecimal saldoResultante) {
        this.saldoResultante = saldoResultante;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
}
