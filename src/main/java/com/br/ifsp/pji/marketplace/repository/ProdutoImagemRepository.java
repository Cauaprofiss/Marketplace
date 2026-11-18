package com.br.ifsp.pji.marketplace.repository;

import java.util.List;
import com.br.ifsp.pji.marketplace.model.ProdutoImagem;
import org.springframework.data.jpa.repository.JpaRepository;

// Persistencia e ordenacao das imagens vinculadas a cada anuncio.
public interface ProdutoImagemRepository extends JpaRepository<ProdutoImagem, Long> {
    List<ProdutoImagem> findByProdutoIdProduto(Long idProduto);

    List<ProdutoImagem> findByProdutoIdProdutoOrderByIdImagemAsc(Long idProduto);
}
