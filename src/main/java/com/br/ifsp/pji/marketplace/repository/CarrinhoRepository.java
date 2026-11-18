package com.br.ifsp.pji.marketplace.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.br.ifsp.pji.marketplace.model.Carrinho;

// Persistencia do carrinho unico de cada usuario.
public interface CarrinhoRepository extends JpaRepository<Carrinho, Long> {

    Optional<Carrinho> findByUsuarioIdUsuario(Long idUsuario);
    
}