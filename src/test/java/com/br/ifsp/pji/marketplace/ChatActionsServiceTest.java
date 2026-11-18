package com.br.ifsp.pji.marketplace;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.br.ifsp.pji.marketplace.Service.AvaliacaoService;
import com.br.ifsp.pji.marketplace.Service.ConversaService;
import com.br.ifsp.pji.marketplace.Service.NotificacaoService;
import com.br.ifsp.pji.marketplace.model.Agendamento;
import com.br.ifsp.pji.marketplace.model.Compra;
import com.br.ifsp.pji.marketplace.model.CompraVendedor;
import com.br.ifsp.pji.marketplace.model.Conversa;
import com.br.ifsp.pji.marketplace.model.ItemCompra;
import com.br.ifsp.pji.marketplace.model.Produto;
import com.br.ifsp.pji.marketplace.model.StatusAgendamento;
import com.br.ifsp.pji.marketplace.model.TipoAvaliacao;
import com.br.ifsp.pji.marketplace.model.Usuario;
import com.br.ifsp.pji.marketplace.repository.*;

class ChatActionsServiceTest {
    private ConversaRepository conversaRepository;
    private MensagemRepository mensagemRepository;
    private ItemCompraRepository itemCompraRepository;
    private AgendamentoRepository agendamentoRepository;
    private AvaliacaoRepository avaliacaoRepository;
    private CompraVendedorRepository compraVendedorRepository;
    private ConversaService conversas;
    private AvaliacaoService avaliacoes;
    private Usuario comprador;
    private Usuario vendedor;
    private Conversa conversa;

    @BeforeEach
    void preparar() {
        conversaRepository = mock(ConversaRepository.class);
        mensagemRepository = mock(MensagemRepository.class);
        itemCompraRepository = mock(ItemCompraRepository.class);
        agendamentoRepository = mock(AgendamentoRepository.class);
        avaliacaoRepository = mock(AvaliacaoRepository.class);
        compraVendedorRepository = mock(CompraVendedorRepository.class);
        conversas = new ConversaService(conversaRepository, mensagemRepository,
                mock(UsuarioRepository.class), mock(ProdutoRepository.class), mock(NotificacaoService.class));
        avaliacoes = new AvaliacaoService(avaliacaoRepository, compraVendedorRepository,
                itemCompraRepository, agendamentoRepository, mock(NotificacaoService.class), conversaRepository);

        comprador = usuario(1L, "Comprador");
        vendedor = usuario(2L, "Vendedor");
        Produto produto = new Produto(); produto.setIdProduto(8L); produto.setUsuario(vendedor);
        conversa = new Conversa(); conversa.setIdConversa(12L); conversa.setComprador(comprador);
        conversa.setVendedor(vendedor); conversa.setAnuncio(produto);
    }

    @Test
    void excluiMensagensAntesDaConversaQuandoUsuarioParticipa() {
        when(conversaRepository.findById(12L)).thenReturn(Optional.of(conversa));
        conversas.excluirConversa(12L, comprador);
        var ordem = inOrder(mensagemRepository, conversaRepository);
        ordem.verify(mensagemRepository).deleteByConversaIdConversa(12L);
        ordem.verify(conversaRepository).delete(conversa);
    }

    @Test
    void impedeExclusaoPorQuemNaoParticipa() {
        when(conversaRepository.findById(12L)).thenReturn(Optional.of(conversa));
        assertThrows(IllegalStateException.class, () -> conversas.excluirConversa(12L, usuario(9L, "Outro")));
        verify(mensagemRepository, never()).deleteByConversaIdConversa(anyLong());
        verify(conversaRepository, never()).delete(any());
    }

    @Test
    void liberaAvaliacaoDoVendedorSomenteAposEntregaConcluida() {
        Compra compra = new Compra(); compra.setComprador(comprador);
        CompraVendedor grupo = new CompraVendedor(); grupo.setIdCompraVendedor(27L); grupo.setCompra(compra); grupo.setVendedor(vendedor);
        ItemCompra item = new ItemCompra(); item.setIdItemCompra(31L); item.setCompraVendedor(grupo); item.setProduto(conversa.getAnuncio());
        Agendamento agendamento = new Agendamento(); agendamento.setCompraVendedor(grupo); agendamento.setStatus(StatusAgendamento.concluido);
        when(conversaRepository.findById(12L)).thenReturn(Optional.of(conversa));
        when(compraVendedorRepository.findPedidosDoCompradorComVendedor(1L, 2L)).thenReturn(List.of(grupo));
        when(itemCompraRepository.findByCompraVendedorIdCompraVendedor(27L)).thenReturn(List.of(item));
        when(agendamentoRepository.findByCompraVendedorIdCompraVendedor(27L)).thenReturn(Optional.of(agendamento));

        assertEquals(Optional.of(27L), avaliacoes.buscarPedidoAvaliavelDaConversa(12L, comprador));
    }

    @Test
    void naoLiberaAvaliacaoParaVendedorNemParaItemJaAvaliado() {
        when(conversaRepository.findById(12L)).thenReturn(Optional.of(conversa));
        assertTrue(avaliacoes.buscarPedidoAvaliavelDaConversa(12L, vendedor).isEmpty());

        Compra compra = new Compra(); compra.setComprador(comprador);
        CompraVendedor grupo = new CompraVendedor(); grupo.setIdCompraVendedor(27L); grupo.setCompra(compra); grupo.setVendedor(vendedor);
        ItemCompra item = new ItemCompra(); item.setIdItemCompra(31L); item.setCompraVendedor(grupo); item.setProduto(conversa.getAnuncio());
        when(compraVendedorRepository.findPedidosDoCompradorComVendedor(1L, 2L)).thenReturn(List.of(grupo));
        when(itemCompraRepository.findByCompraVendedorIdCompraVendedor(27L)).thenReturn(List.of(item));
        when(avaliacaoRepository.existsByCompraVendedorIdCompraVendedorAndTipo(
                27L, TipoAvaliacao.vendedor)).thenReturn(true);
        assertTrue(avaliacoes.buscarPedidoAvaliavelDaConversa(12L, comprador).isEmpty());
    }

    private Usuario usuario(long id, String nome) {
        Usuario usuario = new Usuario(); usuario.setIdUsuario(id); usuario.setNome(nome); return usuario;
    }
}
