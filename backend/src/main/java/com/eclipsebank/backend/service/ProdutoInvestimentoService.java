package com.eclipsebank.backend.service;

import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eclipsebank.backend.dto.ProdutoInvestimentoCadastro;
import com.eclipsebank.backend.dto.ProdutoInvestimentoResposta;
import com.eclipsebank.backend.exception.ConflitoException;
import com.eclipsebank.backend.models.ProdutoInvestimento;
import com.eclipsebank.backend.repository.ProdutoInvestimentoRepository;

@Service
public class ProdutoInvestimentoService {
    
    private final ProdutoInvestimentoRepository produtoInvestimentoRepository;

    public ProdutoInvestimentoService(ProdutoInvestimentoRepository produtoInvestimentoRepository) {
        this.produtoInvestimentoRepository = produtoInvestimentoRepository;
    }

    private String normalizarCodigo(String codigo) {
        return codigo.trim().toUpperCase(Locale.ROOT);
    }

    private void validarCodigoDisponivel(String codigo) {
        if (produtoInvestimentoRepository.existsByCodigoIgnoreCase(codigo)) {
            throw new ConflitoException("Já existe um produto de investimento com esse código.");
        }
    }

    @Transactional
    public ProdutoInvestimentoResposta cadastrar(ProdutoInvestimentoCadastro dados) {
        String codigo = normalizarCodigo(dados.codigo());

        validarCodigoDisponivel(codigo);

        ProdutoInvestimento produto = new ProdutoInvestimento();
        produto.setNome(dados.nome().trim());
        produto.setCodigo(codigo);
        produto.setTipo(dados.tipo());
        produto.setValorMinimo(dados.valorMinimo());
        produto.setRentabilidadeAnualEstimada(dados.rentabilidadeAnualEstimada());

        ProdutoInvestimento produtoSalvo = produtoInvestimentoRepository.save(produto);

        return ProdutoInvestimentoResposta.from(produtoSalvo);
    }

    @Transactional(readOnly = true)
    public List<ProdutoInvestimentoResposta> listarAtivos() {
        return produtoInvestimentoRepository.findByAtivoTrueOrderByNomeAsc() 
            .stream().map(ProdutoInvestimentoResposta::from).toList();
    }
}
