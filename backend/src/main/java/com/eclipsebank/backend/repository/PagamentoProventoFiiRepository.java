package com.eclipsebank.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.eclipsebank.backend.models.PagamentoProventoFii;

public interface PagamentoProventoFiiRepository extends JpaRepository<PagamentoProventoFii, Long> {
    boolean existsByAplicacaoIdAndCompetencia(Long aplicacaoId, String competencia);
}
