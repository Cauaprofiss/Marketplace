package com.br.ifsp.pji.marketplace.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.br.ifsp.pji.marketplace.model.Pagamento;

// Consultas do pagamento associado a cada parte de pedido.
public interface PagamentoRepository extends JpaRepository<Pagamento, Long> {

    Optional<Pagamento> findByCompraVendedorIdCompraVendedor(Long idCompraVendedor);
}