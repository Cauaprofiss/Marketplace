package com.br.ifsp.pji.marketplace.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.br.ifsp.pji.marketplace.model.CompraVendedor;

// Consultas das partes do pedido separadas por vendedor.
public interface CompraVendedorRepository extends JpaRepository<CompraVendedor, Long> {

    List<CompraVendedor> findByCompraIdCompra(Long idCompra);

    List<CompraVendedor> findByVendedorIdUsuario(Long idUsuario);

    List<CompraVendedor> findByVendedorIdUsuarioOrderByIdCompraVendedorDesc(Long idUsuario);

    @Query("""
            select grupo from CompraVendedor grupo
            where grupo.compra.comprador.idUsuario = :idComprador
              and grupo.vendedor.idUsuario = :idVendedor
            order by grupo.idCompraVendedor desc
            """)
    List<CompraVendedor> findPedidosDoCompradorComVendedor(
            @Param("idComprador") Long idComprador,
            @Param("idVendedor") Long idVendedor);
  
}
