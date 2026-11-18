package com.br.ifsp.pji.marketplace.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.br.ifsp.pji.marketplace.model.Cliente;
import com.br.ifsp.pji.marketplace.model.Usuario;

// Repositorio do perfil especializado de cliente.
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    void deleteByUsuario(Usuario usuario);
}