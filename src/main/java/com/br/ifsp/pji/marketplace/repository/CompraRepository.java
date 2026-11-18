package com.br.ifsp.pji.marketplace.repository;

import java.util.List;
import com.br.ifsp.pji.marketplace.model.Compra;
import org.springframework.data.jpa.repository.JpaRepository;

// Consultas dos pedidos principais realizados por cada comprador.
public interface CompraRepository extends JpaRepository<Compra, Long> {
    List<Compra> findByCompradorIdUsuario(Long idCliente);
    List<Compra> findByCompradorIdUsuarioOrderByIdCompraDesc(Long idComprador);
}

