package com.br.ifsp.pji.marketplace;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import com.br.ifsp.pji.marketplace.Service.NotificacaoService;
import com.br.ifsp.pji.marketplace.model.Notificacao;
import com.br.ifsp.pji.marketplace.model.Usuario;
import com.br.ifsp.pji.marketplace.repository.NotificacaoRepository;

class NotificacaoServiceTest {

    private final NotificacaoRepository repository = mock(NotificacaoRepository.class);
    private final NotificacaoService service = new NotificacaoService(repository);

    @Test
    void mensagemApontaParaConversaCorrespondente() {
        Usuario destinatario = usuario(1L);
        when(repository.save(any(Notificacao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Notificacao notificacao = service.notificarNovaMensagem(destinatario, "Ana", 42L);

        assertEquals("/conversas?id=42", notificacao.getDestino());
        assertFalse(notificacao.getLida());
    }

    @Test
    void excluiSomenteNotificacaoDoProprioUsuario() {
        Usuario dono = usuario(1L);
        Notificacao notificacao = new Notificacao();
        notificacao.setUsuario(dono);
        when(repository.findById(7L)).thenReturn(Optional.of(notificacao));

        service.excluir(7L, dono);

        verify(repository).delete(notificacao);
        assertThrows(RuntimeException.class, () -> service.excluir(7L, usuario(2L)));
    }

    @Test
    void limparTodasUsaOUsuarioLogado() {
        Usuario usuario = usuario(9L);

        service.limparTodas(usuario);

        verify(repository).deleteByUsuarioIdUsuario(9L);
    }

    @Test
    void destinoLegadoSeguroDependeDoTipo() {
        Notificacao notificacao = new Notificacao();
        notificacao.setMensagem("O horário do agendamento mudou.");
        notificacao.setTipo(null);

        assertEquals("/agendamentos", notificacao.getDestino());
    }

    private Usuario usuario(Long id) {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(id);
        return usuario;
    }
}
