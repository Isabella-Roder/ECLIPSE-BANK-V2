package com.eclipsebank.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.eclipsebank.backend.models.Conta;

public interface ContaRepository extends JpaRepository<Conta, Long> {
    
    Optional<Conta> findByUsuarioId(Long usuarioId);

    Optional<Conta> findByAgenciaAndNumero(String agencia, String numero);

    boolean existsByUsuarioId(Long usuarioId);

    boolean existsByAgenciaAndNumero(String agencia, String numero);

    Optional<Conta> findByUsuarioEmailIgnoreCase(String email);

    @Query("select c.usuario.id from Conta c where c.id = :contaId")
    Optional<Long> buscarUsuarioIdPelaContaId(@Param("contaId") Long contaId);
}
