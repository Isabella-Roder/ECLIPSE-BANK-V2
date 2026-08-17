package com.eclipsebank.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.eclipsebank.backend.models.MetaFinanceira;

public interface MetaFinanceiraRepository extends JpaRepository<MetaFinanceira, Long> {
    
    List<MetaFinanceira> findByContaIdOrderByCriadaEmDesc(Long contaId);

    Optional<MetaFinanceira> findByIdAndContaId(Long id, Long contaId);
}
