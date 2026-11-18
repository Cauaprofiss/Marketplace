package com.br.ifsp.pji.marketplace.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.br.ifsp.pji.marketplace.model.Produto;
import com.br.ifsp.pji.marketplace.model.ProdutoImagem;
import com.br.ifsp.pji.marketplace.model.StatusProduto;
import com.br.ifsp.pji.marketplace.repository.ProdutoImagemRepository;
import com.br.ifsp.pji.marketplace.repository.ProdutoRepository;

@Service
// Fornece consultas do catalogo e resolve as imagens publicas de cada produto.
public class ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final ProdutoImagemRepository produtoImagemRepository;

    public ProdutoService(
            ProdutoRepository produtoRepository,
            ProdutoImagemRepository produtoImagemRepository) {
        this.produtoRepository = produtoRepository;
        this.produtoImagemRepository = produtoImagemRepository;
    }

    public List<Produto> listarProdutosVisiveis() {
        return produtoRepository.findByStatusInOrderByDataPublicacaoDesc(
                Arrays.asList(StatusProduto.disponivel, StatusProduto.indisponivel)
        );
    }

    /*
     * Usado pela página de detalhes do produto. Só retorna o
     * produto se ele estiver numa das situações públicas
     * (disponivel/indisponivel) — impede acessar rascunho ou
     * em_revisao digitando o id direto na URL.
     */
    public Optional<Produto> buscarProdutoVisivelPorId(Long id) {
        return produtoRepository.findById(id)
                .filter(p -> p.getStatus() == StatusProduto.disponivel
                        || p.getStatus() == StatusProduto.indisponivel);
    }

    /*
     * Usado na página pública do vendedor, para listar só os
     * produtos dele que fazem sentido mostrar para visitantes.
     */
    public List<Produto> listarProdutosVisiveisDoVendedor(Long idVendedor) {
        return produtoRepository.findByUsuarioIdUsuario(idVendedor).stream()
                .filter(p -> p.getStatus() == StatusProduto.disponivel
                        || p.getStatus() == StatusProduto.indisponivel)
                .toList();
    }

    public List<String> listarImagensDoProduto(Produto produto) {
        if (produto == null || produto.getIdProduto() == null) {
            return List.of();
        }

        List<String> imagens = produtoImagemRepository
                .findByProdutoIdProdutoOrderByIdImagemAsc(produto.getIdProduto())
                .stream()
                .map(ProdutoImagem::getUrlImagem)
                .filter(url -> url != null && !url.isBlank())
                .distinct()
                .toList();

        if (!imagens.isEmpty()) {
            return imagens;
        }

        if (produto.getFotoUrl() != null && !produto.getFotoUrl().isBlank()) {
            return List.of(produto.getFotoUrl());
        }

        return List.of();
    }
}
