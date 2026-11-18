package com.br.ifsp.pji.marketplace.controller;

import java.util.Optional;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.br.ifsp.pji.marketplace.Service.AvaliacaoService;
import com.br.ifsp.pji.marketplace.Service.CarrinhoService;
import com.br.ifsp.pji.marketplace.Service.ProdutoService;
import com.br.ifsp.pji.marketplace.model.Produto;
import com.br.ifsp.pji.marketplace.model.Categoria;
import com.br.ifsp.pji.marketplace.model.FavoritoId;
import com.br.ifsp.pji.marketplace.model.Usuario;
import com.br.ifsp.pji.marketplace.repository.FavoritoRepository;
import jakarta.servlet.http.HttpSession;

// Publica a vitrine de produtos e a pagina de detalhes de cada anuncio visivel.
@Controller
public class ProdutoController {

    private final ProdutoService produtoService;
    private final CarrinhoService carrinhoService;
    private final FavoritoRepository favoritoRepository;
    private final AvaliacaoService avaliacaoService;

    public ProdutoController(
            ProdutoService produtoService,
            CarrinhoService carrinhoService,
            FavoritoRepository favoritoRepository,
            AvaliacaoService avaliacaoService) {

        this.produtoService = produtoService;
        this.carrinhoService = carrinhoService;
        this.favoritoRepository = favoritoRepository;
        this.avaliacaoService = avaliacaoService;
    }

    @GetMapping("/produtos")
    public String listarProdutos(HttpSession session, Model model) {

        List<Produto> produtos = produtoService.listarProdutosVisiveis();

        model.addAttribute(
            "produtos",
            produtos
        );
        model.addAttribute("mediasAvaliacoesProduto", avaliacaoService.calcularMediasDosProdutos(produtos));

        model.addAttribute(
            "categoriasProdutos",
            produtos.stream()
                    .map(Produto::getCategoria)
                    .filter(categoria -> categoria != null)
                    .distinct()
                    .toList()
        );

        model.addAttribute(
            "precoMaximo",
            produtos.stream()
                    .map(Produto::getPreco)
                    .filter(preco -> preco != null)
                    .mapToDouble(java.math.BigDecimal::doubleValue)
                    .max()
                    .orElse(80d)
        );

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");

        model.addAttribute("usuario", usuario);
        model.addAttribute("favoritosIds", usuario == null ? java.util.Set.of()
                : favoritoRepository.findByUsuarioIdUsuarioOrderByProdutoDataPublicacaoDesc(usuario.getIdUsuario())
                        .stream().map(favorito -> favorito.getProduto().getIdProduto())
                        .collect(java.util.stream.Collectors.toSet()));

        model.addAttribute(
            "totalItensCarrinho",
            usuario == null ? 0 : carrinhoService.contarItens(usuario)
        );

        return "produtos";
    }

    // NOVO: página de detalhes de um produto
    @GetMapping("/produtos/{id}")
    public String verProduto(@PathVariable Long id, HttpSession session, Model model) {

        Optional<Produto> produtoOpt = produtoService.buscarProdutoVisivelPorId(id);

        if (produtoOpt.isEmpty()) {
            return "redirect:/produtos";
        }

        Produto anuncio = produtoOpt.get();
        model.addAttribute("avaliacoesProduto", avaliacaoService.listarAvaliacoesDoProduto(id));
        model.addAttribute("mediaAvaliacoesProduto", avaliacaoService.calcularMediaDoProduto(id));
        Long idVendedor = anuncio.getUsuario() != null ? anuncio.getUsuario().getIdUsuario() : null;
        model.addAttribute("mediaVendedor", idVendedor == null ? 0d : avaliacaoService.calcularMediaDoVendedor(idVendedor));
        model.addAttribute("totalAvaliacoesVendedor", idVendedor == null ? 0 : avaliacaoService.listarAvaliacoesDoVendedor(idVendedor).size());
        model.addAttribute("anuncio", anuncio);
        model.addAttribute("imagens", produtoService.listarImagensDoProduto(anuncio));
        model.addAttribute(
                "produtosRelacionados",
                produtoService.listarProdutosVisiveis().stream()
                        .filter(produto -> !produto.getIdProduto().equals(anuncio.getIdProduto()))
                        .sorted((primeiro, segundo) -> {
                            boolean primeiroMesmaCategoria = primeiro.getCategoria() != null
                                    && anuncio.getCategoria() != null
                                    && primeiro.getCategoria().getIdCategoria()
                                            .equals(anuncio.getCategoria().getIdCategoria());
                            boolean segundoMesmaCategoria = segundo.getCategoria() != null
                                    && anuncio.getCategoria() != null
                                    && segundo.getCategoria().getIdCategoria()
                                            .equals(anuncio.getCategoria().getIdCategoria());
                            return Boolean.compare(segundoMesmaCategoria, primeiroMesmaCategoria);
                        })
                        .limit(4)
                        .toList()
        );

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
        model.addAttribute("usuario", usuario);
        model.addAttribute(
            "favorito",
            usuario != null && favoritoRepository.existsById(
                new FavoritoId(usuario.getIdUsuario(), anuncio.getIdProduto())
            )
        );
        model.addAttribute(
            "quantidadeCarrinho",
            usuario == null ? 0 : carrinhoService.contarItens(usuario)
        );
        model.addAttribute(
            "totalItensCarrinho",
            usuario == null ? 0 : carrinhoService.contarItens(usuario)
        );

        return "produto";
    }
}
