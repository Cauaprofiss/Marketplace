package com.br.ifsp.pji.marketplace;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import com.br.ifsp.pji.marketplace.Dto.AgendamentoView;
import com.br.ifsp.pji.marketplace.Service.*;
import com.br.ifsp.pji.marketplace.controller.*;
import com.br.ifsp.pji.marketplace.model.*;

class CheckoutScreensTest {
    private CarrinhoService carrinho;
    private CheckoutService checkout;
    private AgendamentoService agendamentos;
    private MockMvc mvc;
    private MockHttpSession session;
    private Usuario usuario;
    private Usuario vendedor;
    private Compra compra;
    private CompraVendedor grupo;
    private Produto produto;

    @BeforeEach
    void preparar() {
        carrinho = mock(CarrinhoService.class);
        checkout = mock(CheckoutService.class);
        agendamentos = mock(AgendamentoService.class);
        ClassLoaderTemplateResolver templates = new ClassLoaderTemplateResolver();
        templates.setPrefix("templates/"); templates.setSuffix(".html");
        templates.setCharacterEncoding("UTF-8"); templates.setTemplateMode("HTML");
        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.setTemplateResolver(templates);
        ThymeleafViewResolver views = new ThymeleafViewResolver();
        views.setTemplateEngine(engine); views.setCharacterEncoding("UTF-8");
        mvc = MockMvcBuilders.standaloneSetup(new CheckoutController(carrinho, checkout, agendamentos),
                new AgendamentoController(agendamentos)).setViewResolvers(views).build();
        usuario = new Usuario(); usuario.setIdUsuario(1L); usuario.setNome("Rafa");
        vendedor = new Usuario(); vendedor.setIdUsuario(2L); vendedor.setNome("Ana Lima"); vendedor.setChavePix("teste");
        session = new MockHttpSession(); session.setAttribute("usuarioLogado", usuario);
        compra = new Compra(); compra.setIdCompra(1042L);
        grupo = new CompraVendedor(); grupo.setIdCompraVendedor(11L); grupo.setCompra(compra); grupo.setVendedor(vendedor);
        produto = produto("Bolo de chocolate", "12.00");
        when(carrinho.listarItens(usuario)).thenReturn(List.of(item(produto), item(produto("Brownie artesanal", "8.00")), item(produto("Caixa de mini donuts", "18.00"))));
    }

    private Produto produto(String nome, String preco) {
        Produto p = new Produto(); p.setNome(nome); p.setPreco(new BigDecimal(preco));
        p.setUsuario(vendedor); p.setLocalRetirada(LocalRetirada.patio_externo);
        p.setDisponibilidadeRetirada(DisponibilidadeRetirada.intervalo_manha); return p;
    }

    private ItemCarrinho item(Produto p) {
        ItemCarrinho i = new ItemCarrinho(); i.setProduto(p); i.setQuantidade(1); return i;
    }

    private Agendamento agendamento(long id, StatusAgendamento status) {
        Agendamento a = new Agendamento(); a.setIdAgendamento(id); a.setCompraVendedor(grupo);
        a.setComprador(usuario); a.setVendedor(vendedor); a.setDataAgendada(LocalDate.now().plusDays(7));
        a.setHorario(LocalTime.of(14, 30)); a.setLocalRetirada(LocalRetirada.patio_externo); a.setStatus(status); return a;
    }

    private String render(String url, String arquivo) throws Exception {
        String html = mvc.perform(get(url).session(session).locale(Locale.forLanguageTag("pt-BR")))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertTrue(html.contains("</html>"));
        assertFalse(html.contains("th:text="));
        Path output = Path.of("target", "visual-review", arquivo + ".html");
        Files.createDirectories(output.getParent()); Files.writeString(output, html);
        return html;
    }

    @Test
    void renderizaCheckoutComDataObrigatoriaEPix() throws Exception {
        String html = render("/checkout", "checkout-page");
        assertTrue(html.contains("name=\"dataRetirada\""));
        assertTrue(html.contains("min=\"" + LocalDate.now() + "\""));
        assertTrue(html.contains("R$ 38,00"));
        assertTrue(html.contains("Continuar com Pix"));
        assertTrue(html.contains("Intervalo manhã · 9:40 - 10:00"));
    }

    @Test
    void selecionaPresencialQuandoPixIndisponivel() throws Exception {
        vendedor.setChavePix(null);
        String html = render("/checkout", "checkout-no-pix");
        assertTrue(html.contains("Indisponível para este pedido"));
        assertTrue(html.contains("Continuar com retirada"));
    }

    @Test
    void enviaDataEscolhidaAoServicoEMantemRotasDePagamento() throws Exception {
        LocalDate data = LocalDate.now().plusDays(14);
        for (TipoPagamento tipo : List.of(TipoPagamento.pix, TipoPagamento.presencial)) {
            when(checkout.registrarCompra(usuario, tipo, data, LocalTime.of(12, 20))).thenReturn(compra);
            mvc.perform(post("/compras/registrar").session(session).param("formaPagamento", tipo.name())
                    .param("dataRetirada", data.toString()).param("horarioRetirada", "12:20"))
                    .andExpect(redirectedUrl("/compras/" + (tipo == TipoPagamento.pix ? "pix/" : "concluido/") + "1042"));
            verify(checkout).registrarCompra(usuario, tipo, data, LocalTime.of(12, 20));
        }
    }

    @Test
    void dataAusenteOuInvalidaRetornaAoCheckoutComErro() throws Exception {
        for (String data : List.of("", "2026-02-30", "amanhã")) {
            mvc.perform(post("/compras/registrar").session(session).param("formaPagamento", "presencial")
                    .param("dataRetirada", data).param("horarioRetirada", "12:20"))
                    .andExpect(redirectedUrl("/checkout")).andExpect(flash().attributeExists("erroCheckout"));
        }
        verifyNoInteractions(checkout);
    }

    @Test
    void falhaNoRegistroPreservaEscolhasEPermiteCorrigirNaMesmaTela() throws Exception {
        LocalDate data = LocalDate.now().plusDays(1);
        when(checkout.registrarCompra(any(), any(), any(), any())).thenThrow(new RuntimeException("Não foi possível registrar."));
        mvc.perform(post("/compras/registrar").session(session).param("formaPagamento", "presencial")
                .param("dataRetirada", data.toString()).param("horarioRetirada", "15:30"))
                .andExpect(redirectedUrl("/checkout")).andExpect(flash().attribute("dataRetirada", data.toString()))
                .andExpect(flash().attribute("horarioRetirada", "15:30"));
    }

    @Test
    void renderizaAgendamentosComProdutosEEstadosEReagendamento() throws Exception {
        when(agendamentos.listarComoComprador(usuario)).thenReturn(List.of(
                new AgendamentoView(agendamento(1, StatusAgendamento.confirmado), "Bolo de chocolate"),
                new AgendamentoView(agendamento(2, StatusAgendamento.concluido), "Brownie artesanal"),
                new AgendamentoView(agendamento(3, StatusAgendamento.cancelado), "Caixa de mini donuts")));
        when(agendamentos.listarComoVendedor(usuario)).thenReturn(List.of(new AgendamentoView(agendamento(4, StatusAgendamento.pendente), "Venda de teste")));
        String html = render("/agendamentos", "agendamentos-page");
        assertTrue(html.contains("14:30")); assertTrue(html.contains("Bolo de chocolate"));
        assertTrue(html.contains("data-status=\"cancelado\""));
        when(agendamentos.buscarComPermissao(1L, usuario)).thenReturn(agendamento(1, StatusAgendamento.confirmado));
        when(agendamentos.buscarDisponibilidadeRetirada(any(Agendamento.class)))
                .thenReturn(DisponibilidadeRetirada.intervalo_manha);
        String reagendar = render("/agendamentos/1/reagendar", "reagendar-page");
        assertTrue(reagendar.contains("/agendamentos/1/atualizar"));
        assertTrue(reagendar.contains("Intervalo Manhã 9:40 - 10:00"));
        assertTrue(reagendar.contains("name=\"horario\" value=\"09:40\""));
    }

    @Test
    void confirmacaoRespeitaFormaDePagamento() throws Exception {
        when(checkout.buscarCompra(1042L, usuario)).thenReturn(compra);
        when(checkout.listarGruposDaCompra(1042L)).thenReturn(List.of(grupo));
        ItemCompra i = new ItemCompra(); i.setProduto(produto); i.setQuantidade(1); i.setPrecoUnitario(new BigDecimal("12"));
        when(checkout.listarItensDoGrupo(11L)).thenReturn(List.of(i));
        when(checkout.pagamentoPresencial(1042L)).thenReturn(true);
        assertTrue(render("/compras/concluido/1042", "confirmado-page").contains("O pagamento será realizado na retirada."));
        when(checkout.pagamentoPresencial(1042L)).thenReturn(false);
        assertTrue(render("/compras/concluido/1042", "confirmado-pix").contains("O pagamento via Pix foi confirmado."));
    }

    @Test
    void exigeLogin() throws Exception {
        mvc.perform(get("/checkout")).andExpect(redirectedUrl("/login"));
        mvc.perform(get("/agendamentos")).andExpect(redirectedUrl("/login"));
    }
}
