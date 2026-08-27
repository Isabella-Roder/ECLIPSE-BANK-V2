package com.eclipsebank.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eclipsebank.backend.dto.RegistroAuditoriaResposta;
import com.eclipsebank.backend.enums.AcaoAuditoria;
import com.eclipsebank.backend.models.RegistroAuditoria;
import com.eclipsebank.backend.repository.RegistroAuditoriaRepository;

@Service
public class AuditoriaService {
    
    private final RegistroAuditoriaRepository registroAuditoriaRepository;

    public AuditoriaService(RegistroAuditoriaRepository registroAuditoriaRepository) {
        this.registroAuditoriaRepository = registroAuditoriaRepository;
    }

    @Transactional
    public void registrar(Long usuarioId, AcaoAuditoria acao, String descricao) {
        registroAuditoriaRepository.save(new RegistroAuditoria(usuarioId, acao, descricao));
    }

    @Transactional(readOnly = true)
    public List<RegistroAuditoriaResposta> listar() {
        return registroAuditoriaRepository.findAllByOrderByCriadoEmDesc()
            .stream()
            .map(RegistroAuditoriaResposta::from)
            .toList();
    }
}
