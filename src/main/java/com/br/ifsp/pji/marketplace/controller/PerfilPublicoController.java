package com.br.ifsp.pji.marketplace.controller;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.br.ifsp.pji.marketplace.Service.CarrinhoService;
import com.br.ifsp.pji.marketplace.Service.AvaliacaoService;
import com.br.ifsp.pji.marketplace.Service.ProdutoService;
import com.br.ifsp.pji.marketplace.Service.UsuarioService;
import com.br.ifsp.pji.marketplace.model.Produto;
import com.br.ifsp.pji.marketplace.model.Usuario;
import com.br.ifsp.pji.marketplace.repository.FavoritoRepository;

import jakarta.servlet.http.HttpSession;

// Monta o perfil publico de um vendedor, seus anuncios, favoritos e avaliacao media.
@Controller
public class PerfilPublicoController {

    private final UsuarioService usuarioService;
    private final ProdutoService produtoService;
    private final CarrinhoService carrinhoService;
    private final AvaliacaoService avaliacaoService;
    private final FavoritoRepository favoritoRepository;

    public PerfilPublicoController(
            UsuarioService usuarioService,
            ProdutoService produtoService,
            CarrinhoService carrinhoService,
            AvaliacaoService avaliacaoService,
            FavoritoRepository favoritoRepository) {

        this.usuarioService = usuarioService;
        this.produtoService = produtoService;
        this.carrinhoService = carrinhoService;
        this.avaliacaoService = avaliacaoService;
        this.favoritoRepository = favoritoRepository;
    }

    @GetMapping("/vendedores/{id}")
    public String verPerfilVendedor(
            @PathVariable("id") Long idVendedor,
            HttpSession session,
            Model model) {

        Usuario vendedor;
        try {
            vendedor = usuarioService.buscarPorId(idVendedor);
        } catch (RuntimeException e) {
            return "redirect:/produtos";
        }

        List<Produto> produtos = produtoService.listarProdutosVisiveisDoVendedor(idVendedor);

        model.addAttribute("vendedor", vendedor);
        model.addAttribute("produtos", produtos);
        model.addAttribute("mediaVendedor", avaliacaoService.calcularMediaDoVendedor(idVendedor));
        model.addAttribute("totalAvaliacoesVendedor",
                avaliacaoService.listarAvaliacoesDoVendedor(idVendedor).size());

        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        model.addAttribute("usuarioLogado", usuarioLogado);
        Set<Long> favoritosIds = usuarioLogado == null ? Set.of() : favoritoRepository
                .findByUsuarioIdUsuarioOrderByProdutoDataPublicacaoDesc(usuarioLogado.getIdUsuario())
                .stream().map(f -> f.getProduto().getIdProduto()).collect(Collectors.toSet());
        model.addAttribute("favoritosIds", favoritosIds);
        model.addAttribute(
            "quantidadeCarrinho",
            usuarioLogado == null ? 0 : carrinhoService.contarItens(usuarioLogado)
        );

        return "perfil-publico";
    }
}
