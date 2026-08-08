package com.br.ifsp.pji.marketplace.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.br.ifsp.pji.marketplace.Service.UsuarioService;
import com.br.ifsp.pji.marketplace.model.Usuario;
import com.br.ifsp.pji.marketplace.repository.TurmaRepository;

@Controller
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final TurmaRepository turmaRepository;

    public UsuarioController(UsuarioService usuarioService, TurmaRepository turmaRepository) {

        this.usuarioService = usuarioService;
        this.turmaRepository = turmaRepository;
    }

    @GetMapping("/cadastro")
    public String cadastro(Model model) {

        model.addAttribute("turmas", turmaRepository.findAll());

        return "cadastro";
    }

    @PostMapping("/cadastro")
    public String cadastrar(Usuario usuario) {

        usuarioService.cadastrar(usuario);

        return "redirect:/login";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @PostMapping("/login")
    public String fazerLogin(@RequestParam String email, @RequestParam String senha, Model model) {

        Usuario usuario = usuarioService.login(email, senha);

        if (usuario != null) {
            model.addAttribute("usuario", usuario);
            return "home";
        }

        model.addAttribute("erro", "E-mail ou senha incorretos.");
        return "login";
    }
}