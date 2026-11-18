package com.br.ifsp.pji.marketplace;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import com.br.ifsp.pji.marketplace.Service.NotificacaoService;
import com.br.ifsp.pji.marketplace.Service.AvaliacaoService;
import com.br.ifsp.pji.marketplace.Service.ProdutoService;
import com.br.ifsp.pji.marketplace.Service.UsuarioService;
import com.br.ifsp.pji.marketplace.controller.UsuarioController;
import com.br.ifsp.pji.marketplace.controller.NotificacaoModelAdvice;
import com.br.ifsp.pji.marketplace.model.Notificacao;
import com.br.ifsp.pji.marketplace.model.TipoNotificacao;
import com.br.ifsp.pji.marketplace.model.Usuario;
import com.br.ifsp.pji.marketplace.repository.FavoritoRepository;
import com.br.ifsp.pji.marketplace.repository.TurmaRepository;

class UsuarioScreensTest {
    private UsuarioService usuarios;
    private TurmaRepository turmas;
    private ProdutoService produtos;
    private NotificacaoService notificacoes;
    private FavoritoRepository favoritos;
    private AvaliacaoService avaliacoes;
    private MockMvc mvc;

    @BeforeEach
    void preparar() {
        usuarios = mock(UsuarioService.class);
        turmas = mock(TurmaRepository.class);
        produtos = mock(ProdutoService.class);
        notificacoes = mock(NotificacaoService.class);
        favoritos = mock(FavoritoRepository.class);
        avaliacoes = mock(AvaliacaoService.class);
        when(turmas.findAll()).thenReturn(List.of());
        when(produtos.listarProdutosVisiveis()).thenReturn(List.of());

        ClassLoaderTemplateResolver templates = new ClassLoaderTemplateResolver();
        templates.setPrefix("templates/");
        templates.setSuffix(".html");
        templates.setCharacterEncoding("UTF-8");
        templates.setTemplateMode("HTML");
        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.setTemplateResolver(templates);
        ThymeleafViewResolver views = new ThymeleafViewResolver();
        views.setTemplateEngine(engine);
        views.setCharacterEncoding("UTF-8");

        UsuarioController controller = new UsuarioController(
                usuarios,
                turmas,
                produtos,
                notificacoes,
                favoritos,
                avaliacoes
        );
        mvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new NotificacaoModelAdvice(notificacoes))
                .setViewResolvers(views)
                .build();
    }

    @Test
    void credenciaisInvalidasDestacamEmailESenhaEPreservamEmail() throws Exception {
        when(usuarios.login("aluno@gmail.com", "incorreta")).thenReturn(null);

        String html = mvc.perform(post("/login")
                        .param("email", "aluno@gmail.com")
                        .param("senha", "incorreta"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        assertTrue(html.matches("(?s).*class=\"[^\"]*input-erro[^\"]*\" id=\"areaEmail\".*"));
        assertTrue(html.matches("(?s).*class=\"[^\"]*input-erro[^\"]*\" id=\"areaSenha\".*"));
        assertTrue(html.contains("value=\"aluno@gmail.com\""));
        assertEquals(2, html.split("aria-invalid=\"true\"", -1).length - 1);

        Path output = Path.of("target", "visual-review", "login-invalido.html");
        Files.createDirectories(output.getParent());
        Files.writeString(output, html);
    }

    @Test
    void cadastroCarregaValidacaoDeEmailCompartilhadaComLogin() throws Exception {
        String html = mvc.perform(get("/cadastro"))
                .andExpect(status().isOk())
                .andExpect(view().name("cadastro"))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        assertTrue(html.contains("id=\"Cadastro-email\""));
        assertTrue(html.contains("inputmode=\"email\""));
        assertTrue(html.contains("/Assets/JavaScript/Cadastro.js"));

        Path output = Path.of("target", "visual-review", "cadastro-email.html");
        Files.createDirectories(output.getParent());
        Files.writeString(output, html);
    }

    @Test
    void homeUsaCabecalhoCompartilhadoComNotificacoesFuncionais() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(1L);
        usuario.setNome("Gustavo");
        Notificacao notificacao = new Notificacao();
        notificacao.setIdNotificacao(7L);
        notificacao.setUsuario(usuario);
        notificacao.setMensagem("Seu agendamento foi atualizado.");
        notificacao.setTipo(TipoNotificacao.agendamento);
        notificacao.setLida(false);
        when(notificacoes.listarPorUsuario(usuario)).thenReturn(List.of(notificacao));
        when(notificacoes.contarNaoLidas(usuario)).thenReturn(1L);

        String html = mvc.perform(get("/home").sessionAttr("usuarioLogado", usuario))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        assertTrue(html.contains("data-notifications-clear"));
        assertTrue(html.contains("data-notification-remove"));
        assertTrue(html.contains("data-notification-empty"));
        assertTrue(html.contains("data-profile-toggle"));
        assertTrue(html.contains("/Assets/JavaScript/campus-header.js"));
        assertFalse(html.contains(">Ver todas</a>"));
        assertEquals(1, html.split("class=\"site-header\"", -1).length - 1);
    }
}
