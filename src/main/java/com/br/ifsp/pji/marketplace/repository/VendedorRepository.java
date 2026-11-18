package com.br.ifsp.pji.marketplace.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.br.ifsp.pji.marketplace.model.Vendedor;

// Repositorio do perfil especializado de vendedor.
public interface VendedorRepository extends JpaRepository<Vendedor, Long>{

}
