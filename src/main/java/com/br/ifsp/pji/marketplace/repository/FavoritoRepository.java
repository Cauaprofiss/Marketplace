package com.br.ifsp.pji.marketplace.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.br.ifsp.pji.marketplace.model.Favorito;
import com.br.ifsp.pji.marketplace.model.FavoritoId;

// Persistencia da relacao muitos-para-muitos entre usuarios e anuncios favoritos.
public interface FavoritoRepository extends JpaRepository<Favorito, FavoritoId> {

    boolean existsByIdIdUsuarioAndIdIdProduto(Long idUsuario, Long idProduto);

    List<Favorito> findByUsuarioIdUsuarioOrderByProdutoDataPublicacaoDesc(Long idUsuario);
    
    void deleteByUsuarioIdUsuario(Long idUsuario);

}
