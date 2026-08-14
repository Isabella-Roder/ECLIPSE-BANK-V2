package com.eclipsebank.backend.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.eclipsebank.backend.enums.StatusConta;
import com.eclipsebank.backend.exception.ConflitoException;
import com.eclipsebank.backend.exception.ContaIndisponivelException;
import com.eclipsebank.backend.exception.SaldoInsuficienteException;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;

@Entity
@Table(name = "contas",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_conta_agencia_numero",
            columnNames = {"agencia", "numero"}
        )
    }
)
public class Conta {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 4)
    private String agencia;

    @Column(nullable = false, length = 12)
    private String numero;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal saldo = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusConta status = StatusConta.ATIVA;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(
        name = "usuario_id",
        nullable = false,
        unique = true,
        foreignKey = @ForeignKey(name = "fk_conta_usuario")
    )
    private Usuario usuario;

    @Column(nullable = false, updatable = false)
    private LocalDateTime criadaEm;

    @Column(nullable = false)
    private LocalDateTime atualizadaEm;

    @Version
    private Long versao;

    public Conta() {

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

    public void creditar(BigDecimal valor) {
        validarContaAtiva();
        validarValorPositivo(valor);

        saldo = saldo.add(valor);
    }

    public void debitar(BigDecimal valor) {
        validarContaAtiva();
        validarValorPositivo(valor);

        if (saldo.compareTo(valor) < 0) {
            throw new SaldoInsuficienteException();
        }

        saldo = saldo.subtract(valor);
    }

    private void validarContaAtiva() {
        if (status != StatusConta.ATIVA) {
            throw new ContaIndisponivelException();
        }
    }

    private void validarValorPositivo(BigDecimal valor) {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O valor deve ser maior que zero");
        }
    }

    public void bloquear() {
        if (status == StatusConta.ENCERRADA) {
            throw new ConflitoException("Uma conta encerrada não pode ser bloqueada");
        }

        if (status == StatusConta.BLOQUEADA) {
            throw new ConflitoException("A conta já esta bloqueada");
        }

        status = StatusConta.BLOQUEADA;
    }

    public void desbloquear() {
        if (status == StatusConta.ENCERRADA) {
            throw new ConflitoException("Uma conta encerrada não pode ser desbloqueada");
        }

        if (status == StatusConta.ATIVA) {
            throw new ConflitoException("A conta já está ativa");
        }

        status = StatusConta.ATIVA;
    }

    public void encerrar() {
        if (status == StatusConta.ENCERRADA) {
            throw new ConflitoException("A conta já está encerrada");
        }

        if (saldo.compareTo(BigDecimal.ZERO) != 0) {
            throw new ConflitoException("A conta precisa estar com saldo zerado");
        }

        status = StatusConta.ENCERRADA;
    }

    public Long getId() {
        return id;
    }

    public String getAgencia() {
        return agencia;
    }

    public String getNumero() {
        return numero;
    }

    public BigDecimal getSaldo() {
        return saldo;
    }

    public StatusConta getStatus() {
        return status;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public LocalDateTime getCriadaEm() {
        return criadaEm;
    }

    public LocalDateTime getAtualizadaEm() {
        return atualizadaEm;
    }

    public Long getVersao() {
        return versao;
    }

    public void setAgencia(String agencia) {
        this.agencia = agencia;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public void setStatus(StatusConta status) {
        this.status = status;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

}
