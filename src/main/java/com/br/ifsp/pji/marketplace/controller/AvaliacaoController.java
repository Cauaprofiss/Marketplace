package com.br.ifsp.pji.marketplace.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.br.ifsp.pji.marketplace.Service.AvaliacaoService;
import com.br.ifsp.pji.marketplace.model.ItemCompra;
import com.br.ifsp.pji.marketplace.model.CompraVendedor;
import com.br.ifsp.pji.marketplace.model.Usuario;

import jakarta.servlet.http.HttpSession;

// Recebe avaliacoes dos produtos comprados e dos vendedores apos a conclusao do pedido.
@Controller
public class AvaliacaoController {

    private final AvaliacaoService avaliacaoService;

    public AvaliacaoController(AvaliacaoService avaliacaoService) {
        this.avaliacaoService = avaliacaoService;
    }

    @GetMapping("/compras/{idCompraVendedor}/avaliar")
    public String tela(@PathVariable Long idCompraVendedor, HttpSession session,
            Model model, RedirectAttributes redirect) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
        if (usuario == null) return "redirect:/login";

        try {
            List<ItemCompra> itens = avaliacaoService.listarItensPendentes(idCompraVendedor, usuario);
            if (itens.isEmpty()) {
                redirect.addFlashAttribute("sucesso", "Todos os itens desse pedido já foram avaliados.");
                return "redirect:/compras";
            }
            model.addAttribute("idCompraVendedor", idCompraVendedor);
            model.addAttribute("itens", itens);
            model.addAttribute("item", itens.get(0));
            return "avaliar-compra";
        } catch (RuntimeException e) {
            redirect.addFlashAttribute("erro", e.getMessage());
            return "redirect:/compras";
        }
    }

    @PostMapping("/compras/{idCompraVendedor}/avaliar")
    public String salvar(@PathVariable Long idCompraVendedor,
            @RequestParam Long idItemCompra, @RequestParam int nota,
            @RequestParam(required = false) String comentario,
            HttpSession session, RedirectAttributes redirect) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
        if (usuario == null) return "redirect:/login";

        try {
            avaliacaoService.avaliar(idCompraVendedor, idItemCompra, nota, comentario, usuario);
            if (!avaliacaoService.listarItensPendentes(idCompraVendedor, usuario).isEmpty()) {
                redirect.addFlashAttribute("sucesso", "Produto avaliado. Avalie o próximo item do pedido.");
                return "redirect:/compras/" + idCompraVendedor + "/avaliar";
            }
            redirect.addFlashAttribute("sucesso", "Avaliação enviada com sucesso.");
            return "redirect:/compras";
        } catch (RuntimeException e) {
            redirect.addFlashAttribute("erro", e.getMessage());
            return "redirect:/compras/" + idCompraVendedor + "/avaliar";
        }
    }

    @GetMapping("/compras/{idCompraVendedor}/avaliar-vendedor")
    public String telaVendedor(@PathVariable Long idCompraVendedor, HttpSession session,
            Model model, RedirectAttributes redirect) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
        if (usuario == null) return "redirect:/login";

        try {
            CompraVendedor grupo = avaliacaoService.buscarPedidoConcluido(idCompraVendedor, usuario);
            if (avaliacaoService.vendedorJaAvaliado(idCompraVendedor)) {
                redirect.addFlashAttribute("sucesso", "O vendedor deste pedido já foi avaliado.");
                return "redirect:/compras";
            }
            List<ItemCompra> itens = avaliacaoService.listarItensDoPedido(idCompraVendedor, usuario);
            model.addAttribute("idCompraVendedor", idCompraVendedor);
            model.addAttribute("grupo", grupo);
            model.addAttribute("itens", itens);
            model.addAttribute("item", itens.isEmpty() ? null : itens.get(0));
            model.addAttribute("mediaVendedor",
                    avaliacaoService.calcularMediaDoVendedor(grupo.getVendedor().getIdUsuario()));
            return "avaliar-vendedor";
        } catch (RuntimeException e) {
            redirect.addFlashAttribute("erro", e.getMessage());
            return "redirect:/compras";
        }
    }

    @PostMapping("/compras/{idCompraVendedor}/avaliar-vendedor")
    public String salvarVendedor(@PathVariable Long idCompraVendedor,
            @RequestParam int nota,
            @RequestParam(required = false) String comentario,
            HttpSession session, RedirectAttributes redirect) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
        if (usuario == null) return "redirect:/login";

        try {
            avaliacaoService.avaliarVendedor(idCompraVendedor, nota, comentario, usuario);
            redirect.addFlashAttribute("sucesso", "Avaliação do vendedor enviada com sucesso.");
            return "redirect:/compras";
        } catch (RuntimeException e) {
            redirect.addFlashAttribute("erro", e.getMessage());
            return "redirect:/compras/" + idCompraVendedor + "/avaliar-vendedor";
        }
    }
}
