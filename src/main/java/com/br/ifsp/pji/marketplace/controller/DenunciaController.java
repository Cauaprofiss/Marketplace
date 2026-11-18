package com.br.ifsp.pji.marketplace.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.br.ifsp.pji.marketplace.Dto.DenunciaRequest;
import com.br.ifsp.pji.marketplace.Service.DenunciaService;
import com.br.ifsp.pji.marketplace.Service.ProdutoService;
import com.br.ifsp.pji.marketplace.model.Denuncia;
import com.br.ifsp.pji.marketplace.model.Produto;
import com.br.ifsp.pji.marketplace.model.Usuario;

import jakarta.servlet.http.HttpSession;

// Exibe o formulario de denuncia de anuncios e a confirmacao do protocolo enviado.
@Controller
public class DenunciaController {

    private final DenunciaService denunciaService;
    private final ProdutoService produtoService;

    public DenunciaController(DenunciaService denunciaService, ProdutoService produtoService) {
        this.denunciaService = denunciaService;
        this.produtoService = produtoService;
    }

    // Tela do formulário: /denuncias/novo?idProduto=123
    @GetMapping("/denuncias/novo")
    public String telaDenuncia(
            @RequestParam Long idProduto,
            HttpSession session,
            Model model) {

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
        if (usuario == null) {
            return "redirect:/login";
        }

        Produto produto = produtoService.buscarProdutoVisivelPorId(idProduto).orElse(null);

        if (produto == null) {
            return "redirect:/produtos";
        }

        model.addAttribute("produto", produto);
        model.addAttribute("idProduto", idProduto);

        return "denuncia-anuncio";
    }

    @PostMapping("/denuncias")
    public String enviarDenuncia(
            @ModelAttribute DenunciaRequest req,
            HttpSession session,
            Model model,
            RedirectAttributes redirect) {

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
        if (usuario == null) {
            return "redirect:/login";
        }

        try {
            Denuncia denuncia = denunciaService.registrar(req, usuario);
            return "redirect:/denuncias/enviada/" + denuncia.getIdDenuncia();

        } catch (RuntimeException e) {

            Produto produto = produtoService.buscarProdutoVisivelPorId(req.getIdProduto()).orElse(null);

            model.addAttribute("produto", produto);
            model.addAttribute("idProduto", req.getIdProduto());
            model.addAttribute("motivo", req.getMotivo());
            model.addAttribute("detalhes", req.getDetalhes());
            model.addAttribute("erro", e.getMessage());

            return "denuncia-anuncio";
        }
    }

    @GetMapping("/denuncias/enviada/{id}")
    public String telaEnviada(
            @org.springframework.web.bind.annotation.PathVariable Long id,
            Model model) {

        // Busca simples só para exibir protocolo e nome do anúncio na confirmação
        Denuncia denuncia = denunciaService.listarPendentes().stream()
                .filter(d -> d.getIdDenuncia().equals(id))
                .findFirst()
                .orElse(null);

        if (denuncia != null) {
            model.addAttribute("protocolo", "#D-" + String.format("%03d", denuncia.getIdDenuncia()));
            model.addAttribute("anuncio", denuncia.getProduto());
        }

        return "denuncia-enviada";
    }
}
