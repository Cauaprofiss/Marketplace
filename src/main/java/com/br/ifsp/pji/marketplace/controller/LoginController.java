package com.br.ifsp.pji.marketplace.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import com.br.ifsp.pji.marketplace.Service.TurmaService;
import com.br.ifsp.pji.marketplace.Service.UsuarioService;
import com.br.ifsp.pji.marketplace.model.Usuario;

@Controller
public class LoginController {

    @Autowired
    private UsuarioService service;

    @Autowired
    private TurmaService turmaService;

    @GetMapping("/cadastro")
    public String telaCadastro(Model model, Usuario usuario) {
        model.addAttribute("usuario", usuario);
        model.addAttribute("turmas", turmaService.listarTodas());
        return "teste";
    }

    @PostMapping("/cadastro")
    public String cadastrar(Usuario usuario) {
        service.cadastrar(usuario);
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String login() {
        return "teste";
    }
}