package com.br.ifsp.pji.marketplace.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.br.ifsp.pji.marketplace.Dto.ConversaResumo;
import com.br.ifsp.pji.marketplace.model.Conversa;
import com.br.ifsp.pji.marketplace.model.Mensagem;
import com.br.ifsp.pji.marketplace.model.Produto;
import com.br.ifsp.pji.marketplace.model.Usuario;
import com.br.ifsp.pji.marketplace.repository.ConversaRepository;
import com.br.ifsp.pji.marketplace.repository.MensagemRepository;
import com.br.ifsp.pji.marketplace.repository.ProdutoRepository;
import com.br.ifsp.pji.marketplace.repository.UsuarioRepository;

@Service
// Implementa a comunicacao entre comprador e vendedor, incluindo leitura e notificacao.
public class ConversaService {

    private final ConversaRepository conversaRepository;
    private final MensagemRepository mensagemRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProdutoRepository produtoRepository;
    private final NotificacaoService notificacaoService;

    public ConversaService(
            ConversaRepository conversaRepository,
            MensagemRepository mensagemRepository,
            UsuarioRepository usuarioRepository,
            ProdutoRepository produtoRepository,
            NotificacaoService notificacaoService) {

        this.conversaRepository = conversaRepository;
        this.mensagemRepository = mensagemRepository;
        this.usuarioRepository = usuarioRepository;
        this.produtoRepository = produtoRepository;
        this.notificacaoService = notificacaoService;
    }

    public List<ConversaResumo> listarConversasDoUsuario(Usuario usuarioLogado) {

        List<Conversa> conversas = conversaRepository
                .findByCompradorIdUsuarioOrVendedorIdUsuarioOrderByIdConversaDesc(
                        usuarioLogado.getIdUsuario(), usuarioLogado.getIdUsuario());

        return conversas.stream()
                .map(c -> montarResumo(c, usuarioLogado))
                .toList();
    }

    public Optional<ConversaResumo> buscarConversaAtual(Long idConversa, Usuario usuarioLogado) {

        return conversaRepository.findById(idConversa)
                .filter(c -> pertenceAoUsuario(c, usuarioLogado))
                .map(c -> montarResumo(c, usuarioLogado));
    }

    public List<Mensagem> listarMensagens(Long idConversa, Usuario usuarioLogado) {

        List<Mensagem> mensagens = mensagemRepository
                .findByConversaIdConversaOrderByDataEnvioAsc(idConversa);

        // marca como lidas as mensagens recebidas (não enviadas pelo próprio usuário)
        mensagens.stream()
                .filter(m -> !m.getRemetente().getIdUsuario().equals(usuarioLogado.getIdUsuario()))
                .filter(m -> !m.isLida())
                .forEach(m -> {
                    m.setLida(true);
                    mensagemRepository.save(m);
                });

        return mensagens;
    }

    @Transactional
    public Conversa iniciarConversa(Long idVendedor, Long idProduto, Usuario comprador) {

        if (idVendedor.equals(comprador.getIdUsuario())) {
            throw new IllegalArgumentException("Não é possível iniciar uma conversa consigo mesmo");
        }

        Usuario vendedor = usuarioRepository.findById(idVendedor)
                .orElseThrow(() -> new IllegalArgumentException("Vendedor não encontrado"));

        Produto produto = idProduto != null
                ? produtoRepository.findById(idProduto).orElse(null)
                : null;

        Optional<Conversa> existente = produto != null
                ? conversaRepository.findByCompradorIdUsuarioAndVendedorIdUsuarioAndAnuncioIdProduto(
                        comprador.getIdUsuario(), vendedor.getIdUsuario(), produto.getIdProduto())
                : conversaRepository.findByCompradorIdUsuarioAndVendedorIdUsuarioAndAnuncioIsNull(
                        comprador.getIdUsuario(), vendedor.getIdUsuario());

        if (existente.isPresent()) {
            return existente.get();
        }

        Conversa conversa = new Conversa();
        conversa.setComprador(comprador);
        conversa.setVendedor(vendedor);
        conversa.setAnuncio(produto);

        return conversaRepository.save(conversa);
    }

    @Transactional
    public Mensagem enviarMensagem(Long idConversa, String conteudo, Usuario remetente) {

        Conversa conversa = conversaRepository.findById(idConversa)
                .orElseThrow(() -> new IllegalArgumentException("Conversa não encontrada"));

        if (!pertenceAoUsuario(conversa, remetente)) {
            throw new IllegalStateException("Usuário não participa desta conversa");
        }

        Mensagem mensagem = new Mensagem();
        mensagem.setConversa(conversa);
        mensagem.setRemetente(remetente);
        mensagem.setConteudo(conteudo);
        mensagem.setDataEnvio(LocalDateTime.now());
        mensagem.setLida(false);

        Mensagem salva = mensagemRepository.save(mensagem);

        Usuario destinatario = conversa.getComprador().getIdUsuario().equals(remetente.getIdUsuario())
                ? conversa.getVendedor()
                : conversa.getComprador();

        notificacaoService.notificarNovaMensagem(
                destinatario,
                remetente.getNome(),
                conversa.getIdConversa());

        return salva;
    }

    private boolean pertenceAoUsuario(Conversa c, Usuario usuario) {
        return c.getComprador().getIdUsuario().equals(usuario.getIdUsuario())
                || c.getVendedor().getIdUsuario().equals(usuario.getIdUsuario());
    }

    private ConversaResumo montarResumo(Conversa c, Usuario usuarioLogado) {

        Usuario outroUsuario = c.getComprador().getIdUsuario().equals(usuarioLogado.getIdUsuario())
                ? c.getVendedor()
                : c.getComprador();

        List<Mensagem> mensagens = mensagemRepository
                .findByConversaIdConversaOrderByDataEnvioAsc(c.getIdConversa());

        Mensagem ultima = mensagens.isEmpty() ? null : mensagens.get(mensagens.size() - 1);

        long naoLidas = mensagemRepository
                .countByConversaIdConversaAndLidaFalseAndRemetenteIdUsuarioNot(
                        c.getIdConversa(), usuarioLogado.getIdUsuario());

        ConversaResumo resumo = new ConversaResumo();
        resumo.setId(c.getIdConversa());
        resumo.setOutroUsuario(outroUsuario);
        resumo.setAnuncio(c.getAnuncio());
        resumo.setUltimaMensagem(ultima != null ? ultima.getConteudo() : "Inicie a conversa");
        resumo.setTempo(ultima != null ? formatarTempo(ultima.getDataEnvio()) : null);
        resumo.setNaoLida(naoLidas > 0);
        resumo.setOnline(false);
        resumo.setIniciais(gerarIniciais(outroUsuario));

        return resumo;
    }

    private String gerarIniciais(Usuario usuario) {
        String nome = usuario.getNome() != null ? usuario.getNome() : "";
        String sobrenome = usuario.getSobrenome() != null ? usuario.getSobrenome() : "";

        String i1 = nome.isEmpty() ? "" : nome.substring(0, 1);
        String i2 = sobrenome.isEmpty() ? "" : sobrenome.substring(0, 1);

        String iniciais = (i1 + i2).toUpperCase();
        return iniciais.isEmpty() ? "?" : iniciais;
    }

    private String formatarTempo(LocalDateTime dataEnvio) {

        Duration diff = Duration.between(dataEnvio, LocalDateTime.now());

        long minutos = diff.toMinutes();
        if (minutos < 1) return "agora";
        if (minutos < 60) return "há " + minutos + " min";

        long horas = diff.toHours();
        if (horas < 24) return "há " + horas + "h";

        long dias = diff.toDays();
        if (dias == 1) return "ontem";
        return "há " + dias + " dias";
    }

    @Transactional
    public void excluirConversa(Long idConversa, Usuario usuarioLogado) {
        Conversa conversa = conversaRepository.findById(idConversa)
                .orElseThrow(() -> new IllegalArgumentException("Conversa não encontrada."));

        if (!pertenceAoUsuario(conversa, usuarioLogado)) {
            throw new IllegalStateException("Você não pode excluir esta conversa.");
        }

        mensagemRepository.deleteByConversaIdConversa(idConversa);
        conversaRepository.delete(conversa);
    }
}
