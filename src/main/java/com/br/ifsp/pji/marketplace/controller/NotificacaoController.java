package com.br.ifsp.pji.marketplace.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.view.RedirectView;

import com.br.ifsp.pji.marketplace.Service.NotificacaoService;
import com.br.ifsp.pji.marketplace.model.Notificacao;
import com.br.ifsp.pji.marketplace.model.Usuario;

import jakarta.servlet.http.HttpSession;

// Exibe notificacoes e processa acoes de leitura, exclusao e limpeza da caixa do usuario.
@Controller
@RequestMapping("/notificacoes")
public class NotificacaoController {

    private final NotificacaoService notificacaoService;

    public NotificacaoController(NotificacaoService notificacaoService) {
        this.notificacaoService = notificacaoService;
    }

    @GetMapping
    public String listar(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
        if (usuario == null) {
            return "redirect:/login";
        }

        List<Notificacao> notificacoes = notificacaoService.listarPorUsuario(usuario);
        model.addAttribute("todasNotificacoes", notificacoes);
        return "notificacoes";
    }

    @PostMapping("/{id}/marcar-lida")
    public String marcarComoLida(
            @PathVariable Long id,
            HttpSession session) {

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
        if (usuario == null) {
            return "redirect:/login";
        }

        notificacaoService.marcarComoLida(id, usuario);
        return "redirect:/notificacoes";
    }

    @PostMapping("/marcar-todas-lidas")
    public String marcarTodasComoLidas(HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
        if (usuario == null) {
            return "redirect:/login";
        }

        notificacaoService.marcarTodasComoLidas(usuario);
        return "redirect:/notificacoes";
    }

    @PostMapping("/ler-todas")
    public ResponseEntity<Void> lerTodas(HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
        if (usuario == null) {
            return ResponseEntity.status(401).build();
        }

        notificacaoService.marcarTodasComoLidas(usuario);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/abrir")
    public Object abrir(@PathVariable Long id, HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
        if (usuario == null) {
            return new RedirectView("/login");
        }

        Notificacao notificacao = notificacaoService.buscarComPermissao(id, usuario);
        notificacaoService.marcarComoLida(id, usuario);
        return new RedirectView(notificacao.getDestino());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id, HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
        if (usuario == null) {
            return ResponseEntity.status(401).build();
        }

        notificacaoService.excluir(id, usuario);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> limpar(HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
        if (usuario == null) {
            return ResponseEntity.status(401).build();
        }

        notificacaoService.limparTodas(usuario);
        return ResponseEntity.noContent().build();
    }
}
