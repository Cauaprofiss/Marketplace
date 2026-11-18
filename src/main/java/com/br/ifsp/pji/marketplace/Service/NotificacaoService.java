package com.br.ifsp.pji.marketplace.Service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.br.ifsp.pji.marketplace.model.Notificacao;
import com.br.ifsp.pji.marketplace.model.TipoNotificacao;
import com.br.ifsp.pji.marketplace.model.Usuario;
import com.br.ifsp.pji.marketplace.repository.NotificacaoRepository;

@Service
// Cria, consulta e marca notificacoes geradas pelos eventos do marketplace.
public class NotificacaoService {

    private final NotificacaoRepository notificacaoRepository;

    public NotificacaoService(NotificacaoRepository notificacaoRepository) {
        this.notificacaoRepository = notificacaoRepository;
    }

    // ============================================================
    // CRIAÇÃO GENÉRICA
    // ============================================================

    /*
     * Ponto único de criação de notificação. Toda notificação do
     * sistema passa por aqui, sempre associada a um usuário
     * específico (o "identifica o usuário relacionado ao evento"
     * do RF38... RF44).
     */
    @Transactional
    public Notificacao criar(Usuario destinatario, String mensagem) {
        return criar(destinatario, mensagem, TipoNotificacao.sistema);
    }

    @Transactional
    public Notificacao criar(
            Usuario destinatario,
            String mensagem,
            TipoNotificacao tipo) {
        return criar(destinatario, mensagem, tipo, null);
    }

    @Transactional
    public Notificacao criar(
            Usuario destinatario,
            String mensagem,
            TipoNotificacao tipo,
            String destino) {

        if (destinatario == null) {
            throw new IllegalArgumentException("Notificação precisa de um destinatário.");
        }

        if (mensagem == null || mensagem.isBlank()) {
            throw new IllegalArgumentException("Mensagem da notificação não pode ser vazia.");
        }

        Notificacao notificacao = new Notificacao();
        notificacao.setUsuario(destinatario);
        notificacao.setMensagem(mensagem);
        notificacao.setTipo(tipo != null ? tipo : TipoNotificacao.sistema);
        notificacao.setDestino(destino);
        notificacao.setLida(false);
        notificacao.setData(LocalDateTime.now());

        return notificacaoRepository.save(notificacao);
    }

    // ============================================================
    // ATALHOS POR TIPO DE EVENTO
    //
    // Cada evento relevante do sistema tem seu próprio método,
    // para que quem for disparar a notificação (CheckoutService,
    // MensagemService, AvaliacaoService, AgendamentoService, etc.)
    // não precise montar a string de mensagem na mão.
    // ============================================================

    public Notificacao notificarNovaVenda(Usuario vendedor, String nomeProduto) {
        return criar(
                vendedor,
                "Você recebeu um novo pedido de " + nomeProduto + ".",
                TipoNotificacao.venda);
    }

    public Notificacao notificarNovoPedido(Usuario comprador, String codigoPedido) {
        return criar(
                comprador,
                "Seu pedido " + codigoPedido + " foi registrado com sucesso.",
                TipoNotificacao.pedido);
    }

    public Notificacao notificarPagamentoConfirmado(Usuario usuario, String codigoPedido) {
        return criar(
                usuario,
                "O pagamento do pedido " + codigoPedido + " foi confirmado.",
                TipoNotificacao.pagamento);
    }

    public Notificacao notificarNovaMensagem(Usuario destinatario, String nomeRemetente) {
        return notificarNovaMensagem(destinatario, nomeRemetente, null);
    }

    public Notificacao notificarNovaMensagem(Usuario destinatario, String nomeRemetente, Long idConversa) {
        return criar(
                destinatario,
                nomeRemetente + " respondeu sobre sua retirada.",
                TipoNotificacao.mensagem,
                idConversa != null ? "/conversas?id=" + idConversa : "/conversas");
    }

    public Notificacao notificarNovaAvaliacao(Usuario avaliado, int nota) {
        return criar(
                avaliado,
                "Você recebeu uma avaliação de " + nota + " estrela(s).",
                TipoNotificacao.sistema);
    }

    public Notificacao notificarAgendamentoCriado(Usuario usuario, String nomeOutraParte) {
        return criar(
                usuario,
                "Um novo agendamento foi combinado com " + nomeOutraParte + ".",
                TipoNotificacao.agendamento);
    }

    public Notificacao notificarAgendamentoAtualizado(Usuario usuario, String nomeOutraParte) {
        return notificarAgendamentoAtualizado(usuario, nomeOutraParte, null);
    }

    public Notificacao notificarAgendamentoAtualizado(Usuario usuario, String nomeOutraParte, Long idAgendamento) {
        return criar(
                usuario,
                nomeOutraParte + " atualizou o horário do agendamento.",
                TipoNotificacao.agendamento,
                destinoAgendamento(idAgendamento));
    }

    public Notificacao notificarAgendamentoCancelado(Usuario usuario, String nomeOutraParte) {
        return notificarAgendamentoCancelado(usuario, nomeOutraParte, null);
    }

    public Notificacao notificarAgendamentoCancelado(Usuario usuario, String nomeOutraParte, Long idAgendamento) {
        return criar(
                usuario,
                nomeOutraParte + " cancelou o agendamento.",
                TipoNotificacao.agendamento,
                destinoAgendamento(idAgendamento));
    }

    public Notificacao notificarAnuncioAprovado(Usuario vendedor, String nomeProduto) {
        return notificarAnuncioAprovado(vendedor, nomeProduto, null);
    }

    public Notificacao notificarAnuncioAprovado(Usuario vendedor, String nomeProduto, Long idProduto) {
        return criar(
                vendedor,
                "Seu anúncio \"" + nomeProduto + "\" foi aprovado e já está disponível.",
                TipoNotificacao.anuncio,
                idProduto != null ? "/produtos/" + idProduto : "/anuncios");
    }

    public Notificacao notificarAnuncioReprovado(Usuario vendedor, String nomeProduto) {
        return criar(
                vendedor,
                "Seu anúncio \"" + nomeProduto + "\" foi reprovado na revisão.",
                TipoNotificacao.anuncio,
                "/anuncios");
    }

    public Notificacao notificarAnuncioExcluido(Usuario vendedor, String nomeProduto) {
        return criar(
                vendedor,
                "Seu anúncio \"" + nomeProduto + "\" foi excluído pela administração.",
                TipoNotificacao.anuncio,
                "/anuncios");
    }

    public Notificacao notificarDenunciaRecebida(Usuario denunciado, String nomeProduto) {
        return criar(
                denunciado,
                "Seu anúncio \"" + nomeProduto + "\" recebeu uma denúncia e está em análise.",
                TipoNotificacao.denuncia);
    }
    
    public Notificacao notificarRetiradaConfirmada(Usuario vendedor, String nomeComprador) {
        return criar(
                vendedor,
                nomeComprador + " confirmou que recebeu o produto.",
                TipoNotificacao.agendamento);
    }

    public Notificacao notificarProblemaNaRetirada(Usuario vendedor, String nomeComprador) {
        return criar(
                vendedor,
                nomeComprador + " informou que não recebeu o produto combinado. Isso foi registrado como denúncia.",
                TipoNotificacao.denuncia);
    }

    public Notificacao notificarCompradorNaoCompareceu(Usuario comprador, String nomeVendedor) {
        return criar(
                comprador,
                nomeVendedor + " informou que você não compareceu à retirada combinada.",
                TipoNotificacao.agendamento);
    }

    // ============================================================
    // CONSULTA
    //
    // Critério de aceitação: notificações só chegam ao usuário
    // relacionado ao evento. Como toda consulta aqui é filtrada
    // por idUsuario, um usuário nunca recebe notificação de outro.
    // ============================================================

    public List<Notificacao> listarPorUsuario(Usuario usuario) {
        return notificacaoRepository.findByUsuarioIdUsuarioOrderByDataDesc(usuario.getIdUsuario());
    }

    public List<Notificacao> listarNaoLidas(Usuario usuario) {
        return notificacaoRepository.findByUsuarioIdUsuarioAndLidaFalse(usuario.getIdUsuario());
    }

    public long contarNaoLidas(Usuario usuario) {
        return listarNaoLidas(usuario).size();
    }

    // NOVO: lista curta para o dropdown do sininho no header
    public List<Notificacao> listarRecentes(Usuario usuario, int limite) {
        return listarPorUsuario(usuario).stream()
                .limit(limite)
                .toList();
    }

    // ============================================================
    // MARCAR COMO LIDA
    // ============================================================

    @Transactional
    public void marcarComoLida(Long idNotificacao, Usuario usuario) {

        Notificacao notificacao = notificacaoRepository.findById(idNotificacao)
                .orElseThrow(() -> new RuntimeException("Notificação não encontrada."));

        /*
         * Garante que o usuário só consegue marcar como lida a
         * PRÓPRIA notificação — mesma regra de posse usada em
         * AgendamentoService.buscarComPermissao.
         */
        if (!notificacao.getUsuario().getIdUsuario().equals(usuario.getIdUsuario())) {
            throw new RuntimeException("Esta notificação não pertence à sua conta.");
        }

        if (Boolean.TRUE.equals(notificacao.getLida())) {
            return;
        }

        notificacao.setLida(true);
        notificacaoRepository.save(notificacao);
    }

    @Transactional
    public void marcarTodasComoLidas(Usuario usuario) {

        List<Notificacao> naoLidas = listarNaoLidas(usuario);

        for (Notificacao notificacao : naoLidas) {
            notificacao.setLida(true);
        }

        notificacaoRepository.saveAll(naoLidas);
    }

    @Transactional
    public void excluir(Long idNotificacao, Usuario usuario) {
        Notificacao notificacao = buscarComPermissao(idNotificacao, usuario);
        notificacaoRepository.delete(notificacao);
    }

    @Transactional
    public void limparTodas(Usuario usuario) {
        notificacaoRepository.deleteByUsuarioIdUsuario(usuario.getIdUsuario());
    }

    public Notificacao buscarComPermissao(Long idNotificacao, Usuario usuario) {
        Notificacao notificacao = notificacaoRepository.findById(idNotificacao)
                .orElseThrow(() -> new RuntimeException("Notificação não encontrada."));

        if (!notificacao.getUsuario().getIdUsuario().equals(usuario.getIdUsuario())) {
            throw new RuntimeException("Esta notificação não pertence à sua conta.");
        }
        return notificacao;
    }

    private String destinoAgendamento(Long idAgendamento) {
        return idAgendamento != null
                ? "/agendamentos#agendamento-" + idAgendamento
                : "/agendamentos";
    }
}
