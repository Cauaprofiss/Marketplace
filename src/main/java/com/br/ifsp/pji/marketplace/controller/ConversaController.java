package com.br.ifsp.pji.marketplace.controller;

import java.util.Optional;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.br.ifsp.pji.marketplace.Service.ConversaService;
import com.br.ifsp.pji.marketplace.Service.AvaliacaoService;
import com.br.ifsp.pji.marketplace.Dto.ConversaResumo;
import com.br.ifsp.pji.marketplace.model.Conversa;
import com.br.ifsp.pji.marketplace.model.Usuario;

import jakarta.servlet.http.HttpSession;

// Controla a listagem de conversas, o envio de mensagens e a exclusao de conversas.
@Controller
@RequestMapping("/conversas")
public class ConversaController {

    private final ConversaService conversaService;
    private final AvaliacaoService avaliacaoService;

    public ConversaController(ConversaService conversaService, AvaliacaoService avaliacaoService) {
        this.conversaService = conversaService;
        this.avaliacaoService = avaliacaoService;
    }

    @GetMapping
    public String verConversas(
            @RequestParam(required = false) Long id,
            HttpSession session,
            Model model) {

        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null) {
            return "redirect:/login";
        }

        List<ConversaResumo> conversas = conversaService.listarConversasDoUsuario(usuarioLogado);
        model.addAttribute("conversas", conversas);

        if (id == null && !conversas.isEmpty()) {
            id = conversas.get(0).getId();
        }

        if (id != null) {
            Optional<ConversaResumo> conversaAtual = conversaService.buscarConversaAtual(id, usuarioLogado);

            if (conversaAtual.isPresent()) {
                model.addAttribute("conversaAtual", conversaAtual.get());
                model.addAttribute("mensagens", conversaService.listarMensagens(id, usuarioLogado));
                avaliacaoService.buscarPedidoAvaliavelDaConversa(id, usuarioLogado)
                        .ifPresent(idPedido -> model.addAttribute("idPedidoAvaliavel", idPedido));
            }
        }

        return "conversas";
    }

    @PostMapping("/iniciar")
    public String iniciarConversa(
            @RequestParam Long idUsuario,
            @RequestParam(required = false) Long idProduto,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null) {
            return "redirect:/login";
        }

        try {
            Conversa conversa = conversaService.iniciarConversa(idUsuario, idProduto, usuarioLogado);
            return "redirect:/conversas?id=" + conversa.getIdConversa();

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
            return idProduto != null
                    ? "redirect:/produtos/" + idProduto
                    : "redirect:/produtos";
        }
    }

    @PostMapping("/mensagem")
    public String enviarMensagem(
            @RequestParam Long idConversa,
            @RequestParam String conteudo,
            HttpSession session) {

        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null) {
            return "redirect:/login";
        }

        conversaService.enviarMensagem(idConversa, conteudo, usuarioLogado);

        return "redirect:/conversas?id=" + idConversa;
    }

    @PostMapping("/{id}/excluir")
    public String excluirConversa(
            @PathVariable Long id,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Usuario usuarioLogado = (Usuario) session.getAttribute("usuarioLogado");
        if (usuarioLogado == null) return "redirect:/login";

        try {
            conversaService.excluirConversa(id, usuarioLogado);
            redirectAttributes.addFlashAttribute("sucessoConversa", "Conversa excluída com sucesso.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("erroConversa", e.getMessage());
        }
        return "redirect:/conversas";
    }
}
