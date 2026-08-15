package com.eclipsebank.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.eclipsebank.backend.models.AplicacaoInvestimento;

public interface AplicacaoInvestimentoRepository extends JpaRepository<AplicacaoInvestimento, Long>{
    
    List<AplicacaoInvestimento> findByContaIdOrderByAplicadaEmDesc(Long contaId);

    Optional<AplicacaoInvestimento> findByIdAndContaId(Long id, Long contaId);

}
