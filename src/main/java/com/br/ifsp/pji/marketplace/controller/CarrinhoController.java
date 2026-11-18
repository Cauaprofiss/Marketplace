package com.br.ifsp.pji.marketplace.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.br.ifsp.pji.marketplace.Service.CarrinhoService;
import com.br.ifsp.pji.marketplace.model.ItemCarrinho;
import com.br.ifsp.pji.marketplace.model.Usuario;
import jakarta.servlet.http.HttpSession;

// Apresenta o carrinho e encaminha operacoes de adicionar, alterar, remover e limpar itens.
@Controller
@RequestMapping("/carrinho")
public class CarrinhoController {

    private final CarrinhoService carrinhoService;

    public CarrinhoController(CarrinhoService carrinhoService) {
        this.carrinhoService = carrinhoService;
    }

    @GetMapping
    public String verCarrinho(HttpSession session, Model model) {

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
        if (usuario == null) {
            return "redirect:/login";
        }

        List<ItemCarrinho> itens = carrinhoService.listarItens(usuario);

        BigDecimal total = itens.stream()
                .map(item -> item.getProduto().getPreco()
                        .multiply(BigDecimal.valueOf(item.getQuantidade())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("itens", itens);
        model.addAttribute("total", total);

        return "carrinho";
    }

    @PostMapping("/adicionar")
    public String adicionar(
            @RequestParam Long idProduto,
            @RequestParam(defaultValue = "1") int quantidade,
            HttpSession session,
            RedirectAttributes redirect) {

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
        if (usuario == null) {
            return "redirect:/login";
        }

        try {
            carrinhoService.adicionarProduto(usuario, idProduto, quantidade);
            redirect.addFlashAttribute(
                    "sucessoCarrinho",
                    quantidade == 1
                            ? "Produto adicionado ao carrinho."
                            : quantidade + " unidades adicionadas ao carrinho."
            );
        } catch (RuntimeException e) {
            redirect.addFlashAttribute("erroCarrinho", e.getMessage());
        }

        return "redirect:/produtos/" + idProduto;
    }

    // ALTERADO: agora captura a exceção de estoque insuficiente
    @PostMapping("/atualizar")
    public String atualizar(
            @RequestParam Long idItemCarrinho,
            @RequestParam int quantidade,
            HttpSession session,
            RedirectAttributes redirect) {

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
        if (usuario == null) {
            return "redirect:/login";
        }

        try {
            carrinhoService.atualizarQuantidade(usuario, idItemCarrinho, quantidade);
        } catch (RuntimeException e) {
            redirect.addFlashAttribute("erroCarrinho", e.getMessage());
        }

        return "redirect:/carrinho";
    }

    @PostMapping("/remover")
    public String remover(
            @RequestParam Long idItemCarrinho,
            HttpSession session) {

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
        if (usuario == null) {
            return "redirect:/login";
        }

        carrinhoService.removerItem(usuario, idItemCarrinho);

        return "redirect:/carrinho";
    }

    @PostMapping("/limpar")
    public String limpar(HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
        if (usuario == null) {
            return "redirect:/login";
        }
        carrinhoService.limparCarrinho(usuario);
        return "redirect:/carrinho";
    }
}
