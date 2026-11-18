package com.br.ifsp.pji.marketplace;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import com.br.ifsp.pji.marketplace.Dto.ConversaResumo;
import com.br.ifsp.pji.marketplace.Service.ConversaService;
import com.br.ifsp.pji.marketplace.Service.AvaliacaoService;
import com.br.ifsp.pji.marketplace.controller.ConversaController;
import com.br.ifsp.pji.marketplace.model.Categoria;
import com.br.ifsp.pji.marketplace.model.Conversa;
import com.br.ifsp.pji.marketplace.model.Mensagem;
import com.br.ifsp.pji.marketplace.model.Produto;
import com.br.ifsp.pji.marketplace.model.Usuario;

class ConversaScreensTest {
    private ConversaService conversas;
    private AvaliacaoService avaliacoes;
    private MockMvc mvc;
    private MockHttpSession session;
    private Usuario usuario;
    private ConversaResumo resumo;

    @BeforeEach
    void preparar() {
        conversas = mock(ConversaService.class);
        avaliacoes = mock(AvaliacaoService.class);
        ClassLoaderTemplateResolver templates = new ClassLoaderTemplateResolver();
        templates.setPrefix("templates/"); templates.setSuffix(".html");
        templates.setCharacterEncoding("UTF-8"); templates.setTemplateMode("HTML");
        SpringTemplateEngine engine = new SpringTemplateEngine(); engine.setTemplateResolver(templates);
        ThymeleafViewResolver views = new ThymeleafViewResolver();
        views.setTemplateEngine(engine); views.setCharacterEncoding("UTF-8");
        mvc = MockMvcBuilders.standaloneSetup(new ConversaController(conversas, avaliacoes)).setViewResolvers(views).build();

        usuario = new Usuario(); usuario.setIdUsuario(1L); usuario.setNome("Rafa");
        Usuario vendedor = new Usuario(); vendedor.setIdUsuario(2L); vendedor.setNome("Ana"); vendedor.setSobrenome("Souza");
        Categoria categoria = new Categoria(); categoria.setNome("Alimentos");
        Produto produto = new Produto(); produto.setIdProduto(8L); produto.setNome("Bolo de chocolate");
        produto.setDescricao("Massa caseira e cobertura de brigadeiro."); produto.setPreco(new BigDecimal("12.00"));
        produto.setQuantidade(4); produto.setCategoria(categoria); produto.setUsuario(vendedor);

        resumo = new ConversaResumo(); resumo.setId(12L); resumo.setOutroUsuario(vendedor); resumo.setAnuncio(produto);
        resumo.setIniciais("AS"); resumo.setUltimaMensagem("Ainda está disponível?"); resumo.setTempo("agora"); resumo.setNaoLida(true);
        session = new MockHttpSession(); session.setAttribute("usuarioLogado", usuario);
    }

    @Test
    void renderizaLayoutDoFigmaSemPerderAsAcoesDoChat() throws Exception {
        Mensagem mensagem = new Mensagem(); mensagem.setRemetente(usuario); mensagem.setConteudo("Quero reservar uma unidade.");
        mensagem.setDataEnvio(LocalDateTime.of(2026, 9, 22, 14, 30));
        when(conversas.listarConversasDoUsuario(usuario)).thenReturn(List.of(resumo));
        when(conversas.buscarConversaAtual(12L, usuario)).thenReturn(Optional.of(resumo));
        when(conversas.listarMensagens(12L, usuario)).thenReturn(List.of(mensagem));
        when(avaliacoes.buscarPedidoAvaliavelDaConversa(12L, usuario)).thenReturn(Optional.of(27L));

        String html = mvc.perform(get("/conversas?id=12").session(session))
                .andExpect(status().isOk()).andExpect(view().name("conversas"))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        assertTrue(html.contains("/Assets/css/conversas.css"));
        assertTrue(html.contains("/Assets/JavaScript/conversas.js"));
        assertTrue(html.contains("action=\"/conversas/mensagem\""));
        assertTrue(html.contains("name=\"idConversa\" value=\"12\""));
        assertTrue(html.contains("Quero reservar uma unidade."));
        assertTrue(html.contains("Bolo de chocolate"));
        assertTrue(html.contains("data-conversation-filter=\"unread\""));
        assertTrue(html.contains("ui-icon-search"));
        assertTrue(html.contains("ui-icon-arrow-right"));
        assertTrue(html.contains("Excluir conversa"));
        assertTrue(html.contains("Avaliar vendedor"));
        assertTrue(html.contains("/compras/27/avaliar"));
        assertTrue(html.contains("action=\"/conversas/12/excluir\""));
        assertFalse(html.contains("th:text="));

        Path preview = Path.of("target", "classes", "static", "chat-preview.html");
        Files.createDirectories(preview.getParent());
        Files.writeString(preview, html);
    }

    @Test
    void enviaMensagemERetornaParaAConversaSelecionada() throws Exception {
        mvc.perform(post("/conversas/mensagem").session(session)
                        .param("idConversa", "12").param("conteudo", "Pode separar para mim?"))
                .andExpect(redirectedUrl("/conversas?id=12"));
        verify(conversas).enviarMensagem(12L, "Pode separar para mim?", usuario);
    }

    @Test
    void iniciaConversaDoPedidoEAbreOChatDoVendedor() throws Exception {
        Conversa conversa = new Conversa();
        conversa.setIdConversa(33L);
        when(conversas.iniciarConversa(2L, 8L, usuario)).thenReturn(conversa);

        mvc.perform(post("/conversas/iniciar").session(session)
                        .param("idUsuario", "2")
                        .param("idProduto", "8"))
                .andExpect(redirectedUrl("/conversas?id=33"));

        verify(conversas).iniciarConversa(2L, 8L, usuario);
    }

    @Test
    void continuaExigindoLogin() throws Exception {
        mvc.perform(get("/conversas")).andExpect(redirectedUrl("/login"));
        mvc.perform(post("/conversas/mensagem").param("idConversa", "12").param("conteudo", "Oi"))
                .andExpect(redirectedUrl("/login"));
        verifyNoInteractions(conversas);
    }

    @Test
    void excluiConversaAutorizadaERetornaParaALista() throws Exception {
        mvc.perform(post("/conversas/12/excluir").session(session))
                .andExpect(redirectedUrl("/conversas"))
                .andExpect(flash().attribute("sucessoConversa", "Conversa excluída com sucesso."));
        verify(conversas).excluirConversa(12L, usuario);
    }
}
