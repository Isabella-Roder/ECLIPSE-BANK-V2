package com.eclipsebank.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.eclipsebank.backend.models.RegistroAuditoria;

public interface RegistroAuditoriaRepository extends JpaRepository<RegistroAuditoria, Long> {
    
    List<RegistroAuditoria> findAllByOrderByCriadoEmDesc();
}
