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
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import com.br.ifsp.pji.marketplace.Dto.AnuncioWizardDTO;
import com.br.ifsp.pji.marketplace.Service.AnuncioService;
import com.br.ifsp.pji.marketplace.Service.CategoriaService;
import com.br.ifsp.pji.marketplace.controller.AnuncioController;
import com.br.ifsp.pji.marketplace.controller.MeusAnunciosController;
import com.br.ifsp.pji.marketplace.model.Produto;
import com.br.ifsp.pji.marketplace.model.Usuario;

class AnuncioFotosScreensTest {
    private AnuncioService service;
    private MockMvc mvc;
    private MockHttpSession session;
    private AnuncioWizardDTO dto;

    @BeforeEach void setup() {
        service = mock(AnuncioService.class);
        var templates = new ClassLoaderTemplateResolver();
        templates.setPrefix("templates/"); templates.setSuffix(".html");
        templates.setCharacterEncoding("UTF-8"); templates.setTemplateMode("HTML");
        var engine = new SpringTemplateEngine(); engine.setTemplateResolver(templates);
        var views = new ThymeleafViewResolver(); views.setTemplateEngine(engine); views.setCharacterEncoding("UTF-8");
        mvc = MockMvcBuilders.standaloneSetup(new AnuncioController(service, mock(CategoriaService.class)),
                new MeusAnunciosController(service, mock(CategoriaService.class))).setViewResolvers(views).build();
        var user = new Usuario(); user.setIdUsuario(1L); user.setNome("Rafa");
        session = new MockHttpSession(); session.setAttribute("usuarioLogado", user);
        var product = new Produto(); product.setIdProduto(7L);
        when(service.buscarRascunhoDoVendedor(1L)).thenReturn(product);
        dto = new AnuncioWizardDTO(); dto.setIdProduto(7L);
        when(service.buscarPorId(7L)).thenReturn(dto);
    }

    @Test void renderEmptyAndExistingPhotos() throws Exception {
        for (int count : new int[]{0, 1, 4}) {
            dto.setUrlsFotos(java.util.Collections.nCopies(count, "/Assets/Imagens/demo-vendedor/bolo-chocolate.png"));
            String html = mvc.perform(get("/anuncios/novo/fotos").param("idProduto", "7").session(session))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
            assertEquals(3, html.split("class=\"photo-item\"", -1).length - 1);
            assertTrue(html.contains("Revisar fotos do produto"));
            Files.createDirectories(Path.of("target/visual-review"));
            Files.writeString(Path.of("target/visual-review/photos-" + count + ".html"), html);
        }
    }

    @Test void editShowsThreeIndependentSecondaryInputs() throws Exception {
        dto.setNome("Produto de teste");
        dto.setUrlsFotos(java.util.Collections.nCopies(4, "/Assets/Imagens/demo-vendedor/bolo-chocolate.png"));
        String html = mvc.perform(get("/anuncios/editar/7").session(session)).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        for (int i = 1; i <= 3; i++) {
            assertTrue(html.contains("name=\"fotoSecundaria" + i + "\""));
            assertTrue(html.contains("name=\"urlSecundaria" + i + "\""));
        }
        assertEquals(3, html.split("class=\"secondary-photo-slot\"", -1).length - 1);
        Files.writeString(Path.of("target/visual-review/edit-photos.html"), html);
        var secondary = new MockMultipartFile("fotoSecundaria3", "extra.png", "image/png", new byte[]{1});
        mvc.perform(multipart("/anuncios/editar/7").file(secondary).param("urlSecundaria3", "/three.png").session(session))
                .andExpect(redirectedUrl("/anuncios"));
        verify(service).editarPublicado(eq(7L), eq(1L), any(), isNull(),
                eq(java.util.Arrays.asList(null, null, secondary)), eq(List.of("", "", "/three.png")));
    }

    @Test void ownerCanRemoveButOtherDraftCannotBeChanged() throws Exception {
        mvc.perform(post("/anuncios/novo/fotos/remover").param("idProduto", "7").param("urlFoto", "/photo.png").session(session)).andExpect(status().isNoContent());
        verify(service).removerFoto(7L, "/photo.png");
        mvc.perform(post("/anuncios/novo/fotos/remover").param("idProduto", "8").param("urlFoto", "/other.png").session(session)).andExpect(status().isBadRequest());
        mvc.perform(post("/anuncios/novo/fotos/remover").param("idProduto", "7").param("urlFoto", "/other.png")).andExpect(status().isBadRequest());
        verify(service, never()).removerFoto(8L, "/other.png");
        verify(service, never()).removerFoto(7L, "/other.png");
    }

    @Test void multipartUploadStillContinuesToCategory() throws Exception {
        dto.setUrlsFotos(List.of("/photo.png"));
        var main = new MockMultipartFile("fotoPrincipal", "main.png", "image/png", new byte[]{1});
        var extra = new MockMultipartFile("fotosAdicionais", "extra.png", "image/png", new byte[]{2});
        mvc.perform(multipart("/anuncios/novo/fotos").file(main).file(extra).param("idProduto", "7").session(session))
            .andExpect(redirectedUrl("/anuncios/novo/categoria?idProduto=7"));
        verify(service).adicionarFotos(7L, List.of(main, extra));
    }
}
