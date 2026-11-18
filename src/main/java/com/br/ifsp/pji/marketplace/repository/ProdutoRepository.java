package com.br.ifsp.pji.marketplace.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.br.ifsp.pji.marketplace.model.Produto;
import com.br.ifsp.pji.marketplace.model.StatusProduto;

// Consultas do catalogo, incluindo filtros por status, categoria, vendedor e codigo.
public interface ProdutoRepository
        extends JpaRepository<Produto, Long> {

    List<Produto> findByStatus(StatusProduto status);

    // NOVO: usado para listar disponivel + indisponivel de uma vez
    List<Produto> findByStatusIn(List<StatusProduto> status);

    List<Produto> findByStatusInOrderByDataPublicacaoDesc(List<StatusProduto> status);

    List<Produto> findByNomeContainingIgnoreCase(String nome);

    List<Produto> findByCategoriaIdCategoria(Long idCategoria);

    List<Produto> findByUsuarioIdUsuario(Long idUsuario);

    /*~~(class org.openrewrite.java.tree.J$Erroneous cannot be cast to class org.openrewrite.java.tree.J$Assignment (org.openrewrite.java.tree.J$Erroneous and org.openrewrite.java.tree.J$Assignment are in unnamed module of loader 'app'))~~>*/@Query("SELECT p FROM Produto p WHERE p.codigoProduto = :codigoProduto")
    Optional<Produto> findByCodigoProduto(String codigoProduto);
}
