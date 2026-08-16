package com.eclipsebank.backend.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.eclipsebank.backend.enums.StatusAplicacaoInvestimento;
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
    name = "aplicacao_investimento",
    indexes = {
        @Index(
            name = "idx_aplicacao_conta",
            columnList = "conta_id"
        ),
        @Index(
            name = "idx_aplicacao_produto",
            columnList = "produto_id"
        )
    }
)
public class AplicacaoInvestimento {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal valorAplicado;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal saldoInvestido;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusAplicacaoInvestimento status = StatusAplicacaoInvestimento.ATIVA;

    @Column
    private LocalDateTime resgatadaEm;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(
        name = "conta_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_aplicacao_conta")
    )
    private Conta conta;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(
        name = "produto_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_aplicacao_produto")
    )
    private ProdutoInvestimento produto;

    @Column(nullable = false, updatable = false)
    private LocalDateTime aplicadaEm;

    @Column(nullable = false)
    private LocalDateTime atualizadaEm;

    @Version
    private Long versao;

    public AplicacaoInvestimento() {

    }

    @PrePersist
    private void antesDeSalvar() {
        LocalDateTime agora = LocalDateTime.now();
        aplicadaEm = agora;
        atualizadaEm = agora;
    }

    @PreUpdate
    private void antesDeAtualizar() {
        atualizadaEm = LocalDateTime.now();
    }

    public void aplicar(BigDecimal valor) {
        validarValorPositivo(valor);

        if (valorAplicado != null) {
            throw new ConflitoException("A aplicação já foi inicializada.");
        }

        valorAplicado = valor;
        saldoInvestido = valor;
    }

    public void resgatar(BigDecimal valor) {
        validarValorPositivo(valor);

        if (status != StatusAplicacaoInvestimento.ATIVA) {
            throw new ConflitoException("A aplicação já foi resgatada.");
        }

        if (saldoInvestido.compareTo(valor) < 0) {
            throw new ConflitoException(
                "O valor do resgate é maior que o saldo investido."
            );
        }

        saldoInvestido = saldoInvestido.subtract(valor);

        if (saldoInvestido.compareTo(BigDecimal.ZERO) == 0) {
            status = StatusAplicacaoInvestimento.RESGATADA;
            resgatadaEm = LocalDateTime.now();
        }
    }

    private void validarValorPositivo(BigDecimal valor) {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                "O valor deve ser maior que zero."
            );
        }
    }

    public void atualizarSaldoInvestido(BigDecimal novoSaldo) {
        if (status != StatusAplicacaoInvestimento.ATIVA) {
            throw new ConflitoException("A aplicação já foi resgatada.");
        }

        validarValorPositivo(novoSaldo);

        saldoInvestido = novoSaldo;
    }

    public Long getId() {
        return id;
    }

    public Conta getConta() {
        return conta;
    }

    public ProdutoInvestimento getProduto() {
        return produto;
    }

    public BigDecimal getValorAplicado() {
        return valorAplicado;
    }

    public BigDecimal getSaldoInvestido() {
        return saldoInvestido;
    }

    public StatusAplicacaoInvestimento getStatus() {
        return status;
    }

    public LocalDateTime getAplicadaEm() {
        return aplicadaEm;
    }

    public LocalDateTime getAtualizadaEm() {
        return atualizadaEm;
    }

    public LocalDateTime getResgatadaEm() {
        return resgatadaEm;
    }

    public Long getVersao() {
        return versao;
    }

    public void setConta(Conta conta) {
        this.conta = conta;
    }

    public void setProduto(ProdutoInvestimento produto) {
        this.produto = produto;
    }
}
