package com.br.ifsp.pji.marketplace.repository;

import java.util.List;
import com.br.ifsp.pji.marketplace.model.Notificacao;
import org.springframework.data.jpa.repository.JpaRepository;

// Consulta notificacoes por usuario, leitura e data de criacao.
public interface NotificacaoRepository extends JpaRepository<Notificacao, Long> {
    List<Notificacao> findByUsuarioIdUsuarioOrderByDataDesc(Long idUsuario);
    List<Notificacao> findByUsuarioIdUsuarioAndLidaFalse(Long idUsuario);
    long deleteByUsuarioIdUsuario(Long idUsuario);
    
}
