package com.br.ifsp.pji.marketplace;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.*;
import org.junit.jupiter.api.*;
import com.br.ifsp.pji.marketplace.Service.*;
import com.br.ifsp.pji.marketplace.model.*;
import com.br.ifsp.pji.marketplace.repository.*;

class AvaliacaoFlowTest {
    AvaliacaoRepository repository;
    ItemCompraRepository items;
    AvaliacaoService service;
    Usuario buyer, seller;
    Agendamento appointment;
    CompraVendedor group;
    ItemCompra item;
    @BeforeEach void setup() {
        repository=mock(AvaliacaoRepository.class); items=mock(ItemCompraRepository.class);
        var groups=mock(CompraVendedorRepository.class); var appointments=mock(AgendamentoRepository.class);
        service=new AvaliacaoService(repository,groups,items,appointments,mock(NotificacaoService.class),mock(ConversaRepository.class));
        buyer=new Usuario();buyer.setIdUsuario(1L); seller=new Usuario();seller.setIdUsuario(2L);
        Compra purchase=new Compra();purchase.setComprador(buyer);
        group=new CompraVendedor();group.setIdCompraVendedor(3L);group.setCompra(purchase);group.setVendedor(seller);
        item=new ItemCompra();item.setIdItemCompra(4L);item.setCompraVendedor(group);
        Produto product=new Produto();product.setIdProduto(5L);item.setProduto(product);
        appointment=new Agendamento();appointment.setStatus(StatusAgendamento.concluido);
        when(groups.findById(3L)).thenReturn(Optional.of(group));
        when(appointments.findByCompraVendedorIdCompraVendedor(3L)).thenReturn(Optional.of(appointment));
        when(items.findById(4L)).thenReturn(Optional.of(item));
        when(items.findByCompraVendedorIdCompraVendedor(3L)).thenReturn(List.of(item));
        when(repository.save(any())).thenAnswer(i->i.getArgument(0));
    }
    @Test void sendsProductAndSellerSeparately() {
        var product=service.avaliar(3L,4L,4," bom ",buyer);
        var vendor=service.avaliarVendedor(3L,5," ótimo ",buyer);
        assertEquals(TipoAvaliacao.produto,product.getTipo());assertEquals(TipoAvaliacao.vendedor,vendor.getTipo());
        assertEquals("bom",product.getComentario());assertEquals(seller,vendor.getAvaliado());
        assertEquals(buyer,product.getAvaliador());verify(repository,times(2)).save(any());
    }
    @Test void rejectsWrongBuyerUnfinishedPurchaseAndInvalidStars() {
        assertThrows(RuntimeException.class,()->service.avaliar(3L,4L,5,null,seller));
        assertThrows(RuntimeException.class,()->service.avaliarVendedor(3L,5,null,seller));
        appointment.setStatus(StatusAgendamento.pendente);
        assertThrows(RuntimeException.class,()->service.avaliar(3L,4L,5,null,buyer));
        assertThrows(RuntimeException.class,()->service.avaliarVendedor(3L,5,null,buyer));
        appointment.setStatus(StatusAgendamento.concluido);
        for(int star:new int[]{0,6}) {
            assertThrows(RuntimeException.class,()->service.avaliar(3L,4L,star,null,buyer));
            assertThrows(RuntimeException.class,()->service.avaliarVendedor(3L,star,null,buyer));
        }
        verify(repository,never()).save(any());
    }
    @Test void preventsDuplicatesAndOverlongComments() {
        assertThrows(RuntimeException.class,()->service.avaliar(3L,4L,5,"a".repeat(1001),buyer));
        assertThrows(RuntimeException.class,()->service.avaliarVendedor(3L,5,"a".repeat(1001),buyer));
        when(repository.existeAvaliacaoProdutoOuLegada(4L,TipoAvaliacao.produto)).thenReturn(true);
        when(repository.existsByCompraVendedorIdCompraVendedorAndTipo(3L,TipoAvaliacao.vendedor)).thenReturn(true);
        assertThrows(RuntimeException.class,()->service.avaliar(3L,4L,5,null,buyer));
        assertThrows(RuntimeException.class,()->service.avaliarVendedor(3L,5,null,buyer));
        assertTrue(service.listarItensPendentes(3L,buyer).isEmpty());
        verify(repository,never()).save(any());
    }
    @Test void averagesAreIndependentAndEmptyHasNoScore() {
        when(repository.listarAvaliacoesProduto(5L,TipoAvaliacao.produto)).thenReturn(List.of(score(3),score(4)));
        when(repository.findByAvaliadoIdUsuarioAndTipoOrderByDataAvaliacaoDesc(2L,TipoAvaliacao.vendedor)).thenReturn(List.of(score(4),score(5)));
        assertEquals(3.5,service.calcularMediaDoProduto(5L));assertEquals(4.5,service.calcularMediaDoVendedor(2L));
        assertEquals(3.5,service.calcularMediasDosProdutos(List.of(item.getProduto())).get(5L));
        assertEquals(0,service.calcularMediaDoProduto(99L));assertEquals(0,service.calcularMediaDoVendedor(99L));
    }
    Avaliacao score(int value){var a=new Avaliacao();a.setNota((byte)value);return a;}
}
