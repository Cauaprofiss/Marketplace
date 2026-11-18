package com.br.ifsp.pji.marketplace.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.br.ifsp.pji.marketplace.model.ItemCarrinho;

// Consultas dos itens pertencentes a carrinhos e de produtos selecionados.
public interface ItemCarrinhoRepository extends JpaRepository<ItemCarrinho, Long> {

    List<ItemCarrinho> findByCarrinhoIdCarrinho(Long idCarrinho);

    Optional<ItemCarrinho> findByCarrinhoIdCarrinhoAndProdutoIdProduto(
            Long idCarrinho, Long idProduto);
}