package com.br.ifsp.pji.marketplace.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.br.ifsp.pji.marketplace.model.Denuncia;
import com.br.ifsp.pji.marketplace.model.StatusDenuncia;

// Consultas para registrar, listar e verificar denuncias de anuncios e usuarios.
public interface DenunciaRepository extends JpaRepository<Denuncia, Long> {

    List<Denuncia> findByStatus(StatusDenuncia status);

    List<Denuncia> findByStatusOrderByDataDenunciaDesc(StatusDenuncia status);

    boolean existsByDenuncianteIdUsuarioAndProdutoIdProduto(Long idDenunciante, Long idProduto);
    
    List<Denuncia> findByProdutoIdProduto(Long idProduto);
    List<Denuncia> findByDenuncianteIdUsuarioOrUsuarioDenunciadoIdUsuario(Long idDenunciante, Long idDenunciado);
    List<Denuncia> findByModeradorIdUsuario(Long idModerador);
}
