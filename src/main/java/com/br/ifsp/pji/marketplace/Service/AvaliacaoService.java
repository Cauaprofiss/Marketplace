package com.br.ifsp.pji.marketplace.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.br.ifsp.pji.marketplace.model.Agendamento;
import com.br.ifsp.pji.marketplace.model.Avaliacao;
import com.br.ifsp.pji.marketplace.model.CompraVendedor;
import com.br.ifsp.pji.marketplace.model.Conversa;
import com.br.ifsp.pji.marketplace.model.ItemCompra;
import com.br.ifsp.pji.marketplace.model.Produto;
import com.br.ifsp.pji.marketplace.model.StatusAgendamento;
import com.br.ifsp.pji.marketplace.model.TipoAvaliacao;
import com.br.ifsp.pji.marketplace.model.Usuario;
import com.br.ifsp.pji.marketplace.repository.AgendamentoRepository;
import com.br.ifsp.pji.marketplace.repository.AvaliacaoRepository;
import com.br.ifsp.pji.marketplace.repository.CompraVendedorRepository;
import com.br.ifsp.pji.marketplace.repository.ConversaRepository;
import com.br.ifsp.pji.marketplace.repository.ItemCompraRepository;

@Service
// Centraliza as regras para avaliar produtos, vendedores e calcular medias exibidas.
public class AvaliacaoService {

    private final AvaliacaoRepository avaliacaoRepository;
    private final CompraVendedorRepository compraVendedorRepository;
    private final ItemCompraRepository itemCompraRepository;
    private final AgendamentoRepository agendamentoRepository;
    private final NotificacaoService notificacaoService;
    private final ConversaRepository conversaRepository;

    public AvaliacaoService(AvaliacaoRepository avaliacaoRepository,
            CompraVendedorRepository compraVendedorRepository,
            ItemCompraRepository itemCompraRepository,
            AgendamentoRepository agendamentoRepository,
            NotificacaoService notificacaoService,
            ConversaRepository conversaRepository) {
        this.avaliacaoRepository = avaliacaoRepository;
        this.compraVendedorRepository = compraVendedorRepository;
        this.itemCompraRepository = itemCompraRepository;
        this.agendamentoRepository = agendamentoRepository;
        this.notificacaoService = notificacaoService;
        this.conversaRepository = conversaRepository;
    }

    @Transactional(readOnly = true)
    public List<ItemCompra> listarItensPendentes(Long idCompraVendedor, Usuario usuario) {
        CompraVendedor grupo = buscarCompraDoUsuario(idCompraVendedor, usuario);
        validarPedidoConcluido(grupo);
        return itemCompraRepository.findByCompraVendedorIdCompraVendedor(idCompraVendedor)
                .stream()
                .filter(item -> !avaliacaoRepository.existeAvaliacaoProdutoOuLegada(
                        item.getIdItemCompra(), TipoAvaliacao.produto))
                .toList();
    }

    @Transactional
    public Avaliacao avaliar(Long idCompraVendedor, Long idItemCompra, int nota,
            String comentario, Usuario usuario) {
        CompraVendedor grupo = buscarCompraDoUsuario(idCompraVendedor, usuario);
        validarPedidoConcluido(grupo);

        if (nota < 1 || nota > 5) {
            throw new RuntimeException("A nota deve ser de 1 a 5 estrelas.");
        }

        ItemCompra item = itemCompraRepository.findById(idItemCompra)
                .orElseThrow(() -> new RuntimeException("Item da compra não encontrado."));

        if (!item.getCompraVendedor().getIdCompraVendedor().equals(idCompraVendedor)) {
            throw new RuntimeException("Este item não pertence ao pedido informado.");
        }

        if (avaliacaoRepository.existeAvaliacaoProdutoOuLegada(
                idItemCompra, TipoAvaliacao.produto)) {
            throw new RuntimeException("Este item já foi avaliado.");
        }

        Avaliacao avaliacao = new Avaliacao();
        avaliacao.setItemCompra(item);
        avaliacao.setCompraVendedor(grupo);
        avaliacao.setAvaliador(usuario);
        avaliacao.setAvaliado(grupo.getVendedor());
        avaliacao.setNota((byte) nota);
        avaliacao.setTipo(TipoAvaliacao.produto);
        avaliacao.setComentario(validarComentario(comentario));
        avaliacao.setDataAvaliacao(LocalDateTime.now());

        Avaliacao salva = avaliacaoRepository.save(avaliacao);
        notificacaoService.notificarNovaAvaliacao(grupo.getVendedor(), nota);
        return salva;
    }

    @Transactional(readOnly = true)
    public CompraVendedor buscarPedidoConcluido(Long idCompraVendedor, Usuario usuario) {
        CompraVendedor grupo = buscarCompraDoUsuario(idCompraVendedor, usuario);
        validarPedidoConcluido(grupo);
        return grupo;
    }

    @Transactional(readOnly = true)
    public List<ItemCompra> listarItensDoPedido(Long idCompraVendedor, Usuario usuario) {
        buscarPedidoConcluido(idCompraVendedor, usuario);
        return itemCompraRepository.findByCompraVendedorIdCompraVendedor(idCompraVendedor);
    }

    @Transactional(readOnly = true)
    public boolean vendedorJaAvaliado(Long idCompraVendedor) {
        return avaliacaoRepository.existsByCompraVendedorIdCompraVendedorAndTipo(
                idCompraVendedor, TipoAvaliacao.vendedor);
    }

    @Transactional
    public Avaliacao avaliarVendedor(Long idCompraVendedor, int nota,
            String comentario, Usuario usuario) {
        CompraVendedor grupo = buscarCompraDoUsuario(idCompraVendedor, usuario);
        validarPedidoConcluido(grupo);

        if (nota < 1 || nota > 5) {
            throw new RuntimeException("A nota deve ser de 1 a 5 estrelas.");
        }
        if (vendedorJaAvaliado(idCompraVendedor)) {
            throw new RuntimeException("O vendedor deste pedido já foi avaliado.");
        }

        ItemCompra itemPrincipal = itemCompraRepository
                .findByCompraVendedorIdCompraVendedor(idCompraVendedor)
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("O pedido não possui produtos."));

        Avaliacao avaliacao = new Avaliacao();
        avaliacao.setItemCompra(itemPrincipal);
        avaliacao.setCompraVendedor(grupo);
        avaliacao.setAvaliador(usuario);
        avaliacao.setAvaliado(grupo.getVendedor());
        avaliacao.setNota((byte) nota);
        avaliacao.setTipo(TipoAvaliacao.vendedor);
        avaliacao.setComentario(validarComentario(comentario));
        avaliacao.setDataAvaliacao(LocalDateTime.now());

        Avaliacao salva = avaliacaoRepository.save(avaliacao);
        notificacaoService.notificarNovaAvaliacao(grupo.getVendedor(), nota);
        return salva;
    }

    private String validarComentario(String comentario) {
        if (comentario == null) return null;
        String texto = comentario.trim();
        if (texto.length() > 1000) throw new IllegalArgumentException("O comentário deve ter no máximo 1000 caracteres.");
        return texto;
    }

    private CompraVendedor buscarCompraDoUsuario(Long idCompraVendedor, Usuario usuario) {
        if (usuario == null || usuario.getIdUsuario() == null) throw new IllegalArgumentException("Entre na sua conta para avaliar.");
        CompraVendedor grupo = compraVendedorRepository.findById(idCompraVendedor)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado."));
        if (!grupo.getCompra().getComprador().getIdUsuario().equals(usuario.getIdUsuario())) {
            throw new RuntimeException("Este pedido não pertence à sua conta.");
        }
        return grupo;
    }

    private void validarPedidoConcluido(CompraVendedor grupo) {
        Agendamento agendamento = agendamentoRepository
                .findByCompraVendedorIdCompraVendedor(grupo.getIdCompraVendedor())
                .orElseThrow(() -> new RuntimeException("O pedido ainda não possui retirada registrada."));
        if (agendamento.getStatus() != StatusAgendamento.concluido) {
            throw new RuntimeException("A avaliação fica disponível depois da entrega.");
        }
    }
    
    @Transactional(readOnly = true)
    public List<Avaliacao> listarAvaliacoesDoProduto(Long idProduto) {
        return avaliacaoRepository.listarAvaliacoesProduto(idProduto, TipoAvaliacao.produto);
    }

    @Transactional(readOnly = true)
    public double calcularMediaDoProduto(Long idProduto) {
        List<Avaliacao> avaliacoes = listarAvaliacoesDoProduto(idProduto);
        if (avaliacoes.isEmpty()) {
            return 0;
        }
        return avaliacoes.stream()
                .mapToInt(Avaliacao::getNota)
                .average()
                .orElse(0);
    }

    @Transactional(readOnly = true)
    public Map<Long, Double> calcularMediasDosProdutos(List<Produto> produtos) {
        Map<Long, Double> medias = new LinkedHashMap<>();
        if (produtos == null) {
            return medias;
        }
        produtos.forEach(produto -> {
            if (produto != null && produto.getIdProduto() != null) {
                double media = calcularMediaDoProduto(produto.getIdProduto());
                medias.put(produto.getIdProduto(), Math.max(0d, Math.min(5d, media)));
            }
        });
        return medias;
    }

    @Transactional(readOnly = true)
    public List<Avaliacao> listarAvaliacoesDoVendedor(Long idVendedor) {
        return avaliacaoRepository.findByAvaliadoIdUsuarioAndTipoOrderByDataAvaliacaoDesc(
                idVendedor, TipoAvaliacao.vendedor);
    }

    @Transactional(readOnly = true)
    public double calcularMediaDoVendedor(Long idVendedor) {
        return listarAvaliacoesDoVendedor(idVendedor).stream()
                .mapToInt(Avaliacao::getNota)
                .average()
                .orElse(0);
    }

    @Transactional(readOnly = true)
    public Optional<Long> buscarPedidoAvaliavelDaConversa(Long idConversa, Usuario usuario) {
        if (usuario == null || usuario.getIdUsuario() == null) return Optional.empty();

        Optional<Conversa> conversaEncontrada = conversaRepository.findById(idConversa);
        if (conversaEncontrada.isEmpty()) return Optional.empty();

        Conversa conversa = conversaEncontrada.get();
        if (!conversa.getComprador().getIdUsuario().equals(usuario.getIdUsuario())) {
            return Optional.empty();
        }

        Long idComprador = conversa.getComprador().getIdUsuario();
        Long idVendedor = conversa.getVendedor().getIdUsuario();
        List<ItemCompra> itens = compraVendedorRepository
                .findPedidosDoCompradorComVendedor(idComprador, idVendedor)
                .stream()
                .flatMap(grupo -> itemCompraRepository
                        .findByCompraVendedorIdCompraVendedor(grupo.getIdCompraVendedor())
                        .stream())
                .filter(item -> conversa.getAnuncio() == null
                        || (item.getProduto() != null
                            && conversa.getAnuncio().getIdProduto().equals(item.getProduto().getIdProduto())))
                .toList();

        return itens.stream()
                .filter(item -> item.getIdItemCompra() != null)
                .map(ItemCompra::getCompraVendedor)
                .filter(grupo -> !avaliacaoRepository
                        .existsByCompraVendedorIdCompraVendedorAndTipo(
                                grupo.getIdCompraVendedor(), TipoAvaliacao.vendedor))
                .filter(grupo -> agendamentoRepository
                        .findByCompraVendedorIdCompraVendedor(grupo.getIdCompraVendedor())
                        .map(agendamento -> agendamento.getStatus() == StatusAgendamento.concluido)
                        .orElse(false))
                .map(CompraVendedor::getIdCompraVendedor)
                .findFirst();
    }
}
