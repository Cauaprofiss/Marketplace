package com.br.ifsp.pji.marketplace.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.br.ifsp.pji.marketplace.model.ItemCompra;

// Consultas das linhas de produtos que compoem cada parte de um pedido.
public interface ItemCompraRepository extends JpaRepository<ItemCompra, Long> {

    List<ItemCompra> findByCompraVendedorIdCompraVendedor(Long idCompraVendedor);

}
