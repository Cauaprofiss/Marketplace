package com.br.ifsp.pji.marketplace;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.*;
import java.nio.file.*;
import org.junit.jupiter.api.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import com.br.ifsp.pji.marketplace.Service.*;
import com.br.ifsp.pji.marketplace.Dto.EditarAnuncioRequest;
import com.br.ifsp.pji.marketplace.model.*;
import com.br.ifsp.pji.marketplace.repository.*;

class EditarFotosServiceTest {
    AnuncioService service;
    ProdutoRepository products;
    ProdutoImagemRepository images;
    Produto product;
    List<ProdutoImagem> photos;
    @BeforeEach void setup() throws Exception {
        products = mock(ProdutoRepository.class); images = mock(ProdutoImagemRepository.class);
        service = new AnuncioService(products, images, mock(CategoriaRepository.class), mock(UsuarioRepository.class), mock(NotificacaoService.class));
        Files.createDirectories(Path.of("target/visual-review"));
        ReflectionTestUtils.setField(service, "uploadDir", Files.createTempDirectory(Path.of("target/visual-review"), "edit-photos-").toString());
        product = new Produto(); product.setIdProduto(7L); product.setFotoUrl("/main.png");
        Usuario user = new Usuario(); user.setIdUsuario(1L); product.setUsuario(user);
        photos = new ArrayList<>();
        for (String url : List.of("/main.png", "/one.png", "/two.png", "/three.png")) photos.add(new ProdutoImagem(product, url));
        when(products.findById(7L)).thenReturn(Optional.of(product));
        when(products.save(product)).thenReturn(product);
        when(images.findByProdutoIdProduto(7L)).thenReturn(photos);
        when(images.save(any())).thenAnswer(invocation -> { ProdutoImagem image = invocation.getArgument(0); if (!photos.contains(image)) photos.add(image); return image; });
    }
    MockMultipartFile photo() { return new MockMultipartFile("imagem", "new.png", "image/png", new byte[]{1,2,3}); }
    @Test void editingMainPreservesAllSecondaryPhotos() {
        service.editarPublicado(7L, 1L, new EditarAnuncioRequest(), photo());
        assertEquals(4, photos.size());
        assertEquals(List.of("/one.png", "/two.png", "/three.png"), photos.subList(1,4).stream().map(ProdutoImagem::getUrlImagem).toList());
        assertEquals(product.getFotoUrl(), photos.get(0).getUrlImagem());
        verify(images, never()).deleteAll(any());
    }
    @Test void editingOneSecondaryPreservesMainAndOthers() {
        service.editarPublicado(7L, 1L, new EditarAnuncioRequest(), null, List.of(photo()), List.of("/two.png"));
        assertEquals("/main.png", product.getFotoUrl());
        assertEquals("/one.png", photos.get(1).getUrlImagem());
        assertNotEquals("/two.png", photos.get(2).getUrlImagem());
        assertEquals("/three.png", photos.get(3).getUrlImagem());
        assertEquals(4, service.buscarPorId(7L).getUrlsFotos().size());
    }
    @Test void noUploadPreservesGallery() {
        service.editarPublicado(7L, 1L, new EditarAnuncioRequest(), null);
        verify(images, never()).save(any());
        assertEquals(4, photos.size());
    }
    @Test void rejectsForeignPhotoAndOverflowBeforeWriting() {
        assertThrows(IllegalArgumentException.class, () -> service.editarPublicado(7L,1L,new EditarAnuncioRequest(),photo(),List.of(photo()),List.of("/foreign.png")));
        assertThrows(IllegalArgumentException.class, () -> service.editarPublicado(7L,1L,new EditarAnuncioRequest(),null,List.of(photo()),List.of("")));
        verify(images, never()).save(any());
        assertEquals("/main.png", product.getFotoUrl());
    }
    @Test void canAddPhotoInEmptySlot() {
        photos.remove(3);
        service.editarPublicado(7L,1L,new EditarAnuncioRequest(),null,List.of(photo()),List.of(""));
        assertEquals(4, photos.size());
        assertEquals("/main.png", product.getFotoUrl());
    }
}
