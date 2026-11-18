package com.br.ifsp.pji.marketplace;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import java.util.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.*;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import com.br.ifsp.pji.marketplace.Service.*;
import com.br.ifsp.pji.marketplace.controller.*;
import com.br.ifsp.pji.marketplace.model.*;
import com.br.ifsp.pji.marketplace.repository.*;

class AvaliacaoScreensTest {
    MockMvc mvc; AvaliacaoService ratings; MockHttpSession session;
    @BeforeEach void setup() {
        ratings=mock(AvaliacaoService.class);
        var products=mock(ProdutoService.class);var users=mock(UsuarioService.class);var cart=mock(CarrinhoService.class);var favorites=mock(FavoritoRepository.class);
        Usuario seller=new Usuario();seller.setIdUsuario(2L);seller.setNome("Ana");
        Usuario buyer=new Usuario();buyer.setIdUsuario(1L);buyer.setNome("Rafa");
        session=new MockHttpSession();session.setAttribute("usuarioLogado",buyer);
        Produto p=new Produto();p.setIdProduto(5L);p.setNome("Produto avaliado");p.setUsuario(seller);p.setPreco(BigDecimal.TEN);p.setQuantidade(5);p.setStatus(StatusProduto.disponivel);
        when(products.listarProdutosVisiveis()).thenReturn(List.of(p));when(products.buscarProdutoVisivelPorId(5L)).thenReturn(Optional.of(p));
        when(products.listarProdutosVisiveisDoVendedor(2L)).thenReturn(List.of(p));when(users.buscarPorId(2L)).thenReturn(seller);
        when(ratings.calcularMediasDosProdutos(any())).thenReturn(Map.of(5L,3.5));
        when(ratings.calcularMediaDoVendedor(2L)).thenReturn(4.5);
        when(ratings.listarAvaliacoesDoVendedor(2L)).thenReturn(List.of(new Avaliacao(),new Avaliacao()));
        var templates=new ClassLoaderTemplateResolver();templates.setPrefix("templates/");templates.setSuffix(".html");templates.setCharacterEncoding("UTF-8");templates.setTemplateMode("HTML");
        var engine=new SpringTemplateEngine();engine.setTemplateResolver(templates);
        var views=new ThymeleafViewResolver();views.setTemplateEngine(engine);views.setCharacterEncoding("UTF-8");
        mvc=MockMvcBuilders.standaloneSetup(new ProdutoController(products,cart,favorites,ratings),
          new PerfilPublicoController(users,products,cart,ratings,favorites),
          new UsuarioController(users,mock(TurmaRepository.class),products,mock(NotificacaoService.class),favorites,ratings),
          new AvaliacaoController(ratings)).setViewResolvers(views).build();
    }
    String page(String url) throws Exception { return mvc.perform(get(url).session(session)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8); }
    @Test void homeCardsShowProductAverage() throws Exception {
        String html=page("/home");assertTrue(html.contains("3,5"));assertTrue(html.contains("Avaliação média 3,5 de 5"));
    }
    @Test void sellerScoreAppearsInProfileAndProduct() throws Exception {
        String profile=page("/vendedores/2");assertTrue(profile.contains("4,5"));assertTrue(profile.contains("2 avaliações"));
        String product=page("/produtos/5");assertTrue(product.contains("Avaliação média do vendedor: 4,5 de 5"));
        when(ratings.listarAvaliacoesDoVendedor(2L)).thenReturn(List.of());
        assertTrue(page("/produtos/5").contains("Vendedor ainda sem avaliações"));
    }
    @Test void bothPostRoutesSaveAndRequireLogin() throws Exception {
        mvc.perform(post("/compras/3/avaliar").session(session).param("idItemCompra","4").param("nota","4")).andExpect(redirectedUrl("/compras"));
        verify(ratings).avaliar(eq(3L),eq(4L),eq(4),isNull(),any());
        mvc.perform(post("/compras/3/avaliar-vendedor").session(session).param("nota","5")).andExpect(redirectedUrl("/compras"));
        verify(ratings).avaliarVendedor(eq(3L),eq(5),isNull(),any());
        mvc.perform(post("/compras/3/avaliar-vendedor").param("nota","5")).andExpect(redirectedUrl("/login"));
    }
}
