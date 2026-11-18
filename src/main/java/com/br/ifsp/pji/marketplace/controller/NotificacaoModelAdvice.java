package com.br.ifsp.pji.marketplace.controller;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.br.ifsp.pji.marketplace.Service.NotificacaoService;
import com.br.ifsp.pji.marketplace.model.Usuario;

import jakarta.servlet.http.HttpSession;

// Insere no modelo compartilhado os dados de notificacao usados pelo cabecalho das paginas.
@ControllerAdvice
public class NotificacaoModelAdvice {

    private final NotificacaoService notificacaoService;

    public NotificacaoModelAdvice(NotificacaoService notificacaoService) {
        this.notificacaoService = notificacaoService;
    }

    @ModelAttribute
    public void adicionarNotificacoesAoCabecalho(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
        if (usuario == null) {
            return;
        }

        model.addAttribute("notificacoes", notificacaoService.listarPorUsuario(usuario));
        model.addAttribute("quantidadeNotificacoesNaoLidas", notificacaoService.contarNaoLidas(usuario));
    }
}
