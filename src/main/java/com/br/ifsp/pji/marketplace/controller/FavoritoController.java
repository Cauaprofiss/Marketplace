package com.br.ifsp.pji.marketplace.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.br.ifsp.pji.marketplace.model.Favorito;
import com.br.ifsp.pji.marketplace.model.FavoritoId;
import com.br.ifsp.pji.marketplace.model.Produto;
import com.br.ifsp.pji.marketplace.model.Usuario;
import com.br.ifsp.pji.marketplace.repository.FavoritoRepository;
import com.br.ifsp.pji.marketplace.repository.ProdutoRepository;

import jakarta.servlet.http.HttpSession;

// Gerencia a listagem e a alternancia de anuncios favoritos do usuario autenticado.
@Controller
public class FavoritoController {

    private final FavoritoRepository favoritoRepository;
    private final ProdutoRepository produtoRepository;

    public FavoritoController(FavoritoRepository favoritoRepository,
            ProdutoRepository produtoRepository) {
        this.favoritoRepository = favoritoRepository;
        this.produtoRepository = produtoRepository;
    }

    @GetMapping("/favoritos")
    public String listar(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
        if (usuario == null) return "redirect:/login";
        model.addAttribute("produtos", favoritoRepository
                .findByUsuarioIdUsuarioOrderByProdutoDataPublicacaoDesc(usuario.getIdUsuario())
                .stream().map(Favorito::getProduto).toList());
        return "favoritos";
    }

    @PostMapping("/favoritos/{idProduto}/alternar")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> alternar(@PathVariable Long idProduto,
            HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("autenticado", false, "favorito", false));
        }

        FavoritoId id = new FavoritoId(usuario.getIdUsuario(), idProduto);
        if (favoritoRepository.existsById(id)) {
            favoritoRepository.deleteById(id);
            return ResponseEntity.ok(Map.of("autenticado", true, "favorito", false));
        }

        Produto produto = produtoRepository.findById(idProduto).orElse(null);
        if (produto == null) return ResponseEntity.notFound().build();

        Favorito favorito = new Favorito();
        favorito.setId(id);
        favorito.setUsuario(usuario);
        favorito.setProduto(produto);
        favoritoRepository.save(favorito);
        return ResponseEntity.ok(Map.of("autenticado", true, "favorito", true));
    }
}
