package com.br.ifsp.pji.marketplace.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.br.ifsp.pji.marketplace.model.Conversa;

// Busca conversas das quais um usuario participa, com ou sem anuncio vinculado.
public interface ConversaRepository extends JpaRepository<Conversa, Long> {

    List<Conversa> findByCompradorIdUsuarioOrVendedorIdUsuarioOrderByIdConversaDesc(
            Long idComprador, Long idVendedor);

    Optional<Conversa> findByCompradorIdUsuarioAndVendedorIdUsuarioAndAnuncioIdProduto(
            Long idComprador, Long idVendedor, Long idProduto);

    Optional<Conversa> findByCompradorIdUsuarioAndVendedorIdUsuarioAndAnuncioIsNull(
            Long idComprador, Long idVendedor);
    
    List<Conversa> findByCompradorIdUsuarioOrVendedorIdUsuario(Long idComprador, Long idVendedor);
}