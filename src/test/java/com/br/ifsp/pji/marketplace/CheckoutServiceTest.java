package com.br.ifsp.pji.marketplace;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;

import com.br.ifsp.pji.marketplace.Service.*;
import com.br.ifsp.pji.marketplace.model.*;
import com.br.ifsp.pji.marketplace.repository.*;

class CheckoutServiceTest {
    private final CarrinhoService carrinho = mock(CarrinhoService.class);
    private final ItemCarrinhoRepository itens = mock(ItemCarrinhoRepository.class);
    private final CompraRepository compras = mock(CompraRepository.class);
    private final CompraVendedorRepository grupos = mock(CompraVendedorRepository.class);
    private final ItemCompraRepository itensCompra = mock(ItemCompraRepository.class);
    private final AgendamentoRepository agendamentos = mock(AgendamentoRepository.class);
    private final PagamentoRepository pagamentos = mock(PagamentoRepository.class);
    private final NotificacaoService notificacoes = mock(NotificacaoService.class);
    private final CheckoutService service = new CheckoutService(carrinho, itens, compras, grupos,
            itensCompra, agendamentos, pagamentos, notificacoes);
    private final Usuario comprador = new Usuario();
    private final Usuario vendedor = new Usuario();
    private ItemCarrinho item;

    @BeforeEach
    void preparar() {
        comprador.setIdUsuario(1L);
        vendedor.setIdUsuario(2L);
        vendedor.setNome("Ana Lima");
        vendedor.setChavePix("chave-de-teste");
        Produto produto = new Produto();
        produto.setNome("Bolo de chocolate");
        produto.setUsuario(vendedor);
        produto.setLocalRetirada(LocalRetirada.patio_externo);
        produto.setDisponibilidadeRetirada(DisponibilidadeRetirada.intervalo_manha);
        produto.setPreco(new BigDecimal("12.00"));
        item = new ItemCarrinho();
        item.setProduto(produto);
        item.setQuantidade(2);
        when(carrinho.listarItens(comprador)).thenReturn(List.of(item));
        when(compras.save(any())).thenAnswer(call -> {
            Compra compra = call.getArgument(0);
            compra.setIdCompra(42L);
            return compra;
        });
        when(grupos.save(any())).thenAnswer(call -> call.getArgument(0));
    }

    @ParameterizedTest
    @EnumSource(value = TipoPagamento.class, names = {"pix", "presencial"})
    void salvaADataEscolhidaEHorarioDefinidoPeloVendedor(TipoPagamento tipo) {
        LocalDate data = LocalDate.now().plusDays(15);
        LocalTime hora = LocalTime.of(12, 20);
        service.registrarCompra(comprador, tipo, data, hora);
        ArgumentCaptor<Agendamento> captura = ArgumentCaptor.forClass(Agendamento.class);
        verify(agendamentos).save(captura.capture());
        assertEquals(data, captura.getValue().getDataAgendada());
        assertEquals(LocalTime.of(9, 40), captura.getValue().getHorario());
        assertEquals(LocalRetirada.patio_externo, captura.getValue().getLocalRetirada());
        verify(agendamentos, never()).findByVendedorIdUsuarioAndDataAgendadaAndHorarioAndStatusIn(anyLong(), any(), any(), any());
        ArgumentCaptor<Pagamento> pagamento = ArgumentCaptor.forClass(Pagamento.class);
        verify(pagamentos).save(pagamento.capture());
        assertEquals(tipo, pagamento.getValue().getTipo());
        assertEquals(new BigDecimal("24.00"), pagamento.getValue().getValor());
        verify(itens).deleteAll(List.of(item));
    }

    @Test
    void rejeitaDataPassadaSemLimparCarrinhoOuSalvarCompra() {
        assertThrows(IllegalArgumentException.class, () -> service.registrarCompra(comprador,
                TipoPagamento.presencial, LocalDate.now().minusDays(1), LocalTime.NOON));
        verifyNoInteractions(compras, grupos, agendamentos, pagamentos, itens);
    }

    @Test
    void exigeData() {
        assertThrows(IllegalArgumentException.class, () -> service.registrarCompra(comprador,
                TipoPagamento.presencial, null, LocalTime.NOON));
        verifyNoInteractions(compras, itens);
    }

    @Test
    void permiteMaisDeUmAgendamentoNoMesmoIntervalo() {
        when(agendamentos.findByVendedorIdUsuarioAndDataAgendadaAndHorarioAndStatusIn(anyLong(), any(), any(), any()))
                .thenReturn(List.of(new Agendamento()));
        service.registrarCompra(comprador, TipoPagamento.presencial,
                LocalDate.now().plusDays(7), LocalTime.NOON);
        verify(agendamentos).save(any());
        verify(agendamentos, never()).findByVendedorIdUsuarioAndDataAgendadaAndHorarioAndStatusIn(anyLong(), any(), any(), any());
    }

    @Test
    void preservaValidacaoDaChavePix() {
        vendedor.setChavePix(null);
        assertThrows(RuntimeException.class, () -> service.registrarCompra(comprador,
                TipoPagamento.pix, LocalDate.now().plusDays(7), LocalTime.NOON));
        verifyNoInteractions(compras, itens);
    }
}
