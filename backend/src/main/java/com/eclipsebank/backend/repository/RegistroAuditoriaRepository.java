package com.eclipsebank.backend.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.eclipsebank.backend.enums.AcaoAuditoria;
import com.eclipsebank.backend.models.RegistroAuditoria;

public interface RegistroAuditoriaRepository extends JpaRepository<RegistroAuditoria, Long> {
    
    List<RegistroAuditoria> findAllByOrderByCriadoEmDesc();

    long countByUsuarioIdAndAcaoAndCriadoEmAfter(Long usuarioId, AcaoAuditoria acao, LocalDateTime desde);
}
