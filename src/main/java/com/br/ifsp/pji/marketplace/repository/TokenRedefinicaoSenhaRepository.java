package com.br.ifsp.pji.marketplace.repository;

import java.util.Optional;
import com.br.ifsp.pji.marketplace.model.TokenRedefinicaoSenha;
import org.springframework.data.jpa.repository.JpaRepository;

// Busca codigos de recuperacao por usuario e validade.
public interface TokenRedefinicaoSenhaRepository extends JpaRepository<TokenRedefinicaoSenha, Long> {
    Optional<TokenRedefinicaoSenha> findByToken(String token);
    void deleteByUsuarioIdUsuario(Long idUsuario);
}