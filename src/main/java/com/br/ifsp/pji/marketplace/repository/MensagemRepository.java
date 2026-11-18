package com.br.ifsp.pji.marketplace.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.br.ifsp.pji.marketplace.model.Mensagem;

// Busca mensagens em ordem cronologica e contabiliza as que ainda nao foram lidas.
public interface MensagemRepository extends JpaRepository<Mensagem, Long> {

    List<Mensagem> findByConversaIdConversaOrderByDataEnvioAsc(Long idConversa);

    long countByConversaIdConversaAndLidaFalseAndRemetenteIdUsuarioNot(
            Long idConversa, Long idUsuarioLogado);

    long deleteByConversaIdConversa(Long idConversa);
    
    void deleteByRemetenteIdUsuario(Long idUsuario);
}
