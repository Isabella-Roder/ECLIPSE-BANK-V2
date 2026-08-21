package com.eclipsebank.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eclipsebank.backend.dto.CompraCadastro;
import com.eclipsebank.backend.dto.CompraResposta;
import com.eclipsebank.backend.models.Compra;
import com.eclipsebank.backend.models.Fatura;
import com.eclipsebank.backend.repository.CompraRepository;

@Service
public class CompraService {
    
    private final CompraRepository compraRepository;
    private final FaturaService faturaService;

    public CompraService(CompraRepository compraRepository, FaturaService faturaService) {
        this.compraRepository = compraRepository;
        this.faturaService = faturaService;
    }

    @Transactional
    public CompraResposta lancarCompra(Long cartaoId, CompraCadastro dados) {
        Fatura fatura = faturaService.obterFatuaAtualParaLancamento(cartaoId, dados.valor());

        Compra compra = new Compra();
        compra.setFatura(fatura);
        compra.setValor(dados.valor());
        compra.setDescricao(dados.descricao());
        compra.definirCategoria(dados.categoria(), dados.categoriaPersonalizada());

        return CompraResposta.from(compraRepository.save(compra));
    }

    @Transactional(readOnly = true)
    public List<CompraResposta> listarPorFatura(Long faturaId) {
        return compraRepository.findByFaturaId(faturaId).stream()
            .map(CompraResposta::from).toList();
    }
}
