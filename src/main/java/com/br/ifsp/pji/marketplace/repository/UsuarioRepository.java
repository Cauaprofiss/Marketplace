package com.br.ifsp.pji.marketplace.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.br.ifsp.pji.marketplace.model.Usuario;

// Consultas de autenticacao, unicidade e persistencia dos usuarios.
public interface UsuarioRepository extends JpaRepository<Usuario,Long>{
	Optional<Usuario> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByTelefone(String telefone);
}
