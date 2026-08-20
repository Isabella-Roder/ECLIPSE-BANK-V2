package com.eclipsebank.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.eclipsebank.backend.models.Fatura;

public interface FaturaRepository extends JpaRepository<Fatura, Long> {
    
    List<Fatura> findByCartaoId(Long cartaoId);

    Optional<Fatura> findByCartaoIdAndMesReferencia(Long cartaoId, String mesReferencia);
}
