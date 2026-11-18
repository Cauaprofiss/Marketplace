package com.br.ifsp.pji.marketplace.controller;

import com.br.ifsp.pji.marketplace.Service.UsuarioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// Conduz as etapas de solicitacao, validacao do codigo e redefinicao de senha.
@Controller
public class RecuperacaoSenhaController {

    private final UsuarioService usuarioService;

    public RecuperacaoSenhaController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // ---------- PASSO 1: tela "esqueci minha senha" ----------

    @GetMapping("/recuperar-senha")
    public String exibirFormularioRecuperacao() {
        return "recuperar-senha";
    }

    @PostMapping("/recuperar-senha")
    public String solicitarRecuperacao(@RequestParam String email, Model model, RedirectAttributes redirectAttributes) {
        try {
            String codigo = usuarioService.solicitarRecuperacaoSenha(email);
            usuarioService.enviarCodigoRecuperacao(email.trim().toLowerCase(), codigo);

            redirectAttributes.addFlashAttribute("sucesso", "Enviamos um código de recuperação para o seu e-mail.");
            redirectAttributes.addFlashAttribute("email", email);
            return "redirect:/redefinir-senha";
        } catch (RuntimeException ex) {
            model.addAttribute("erro", ex.getMessage());
            model.addAttribute("email", email);
            return "recuperar-senha";
        }
    }

    // ---------- PASSO 2: usuario digita email + codigo + nova senha ----------

    @GetMapping("/redefinir-senha")
    public String exibirFormularioRedefinicao() {
        return "redefinir-senha";
    }

    @PostMapping("/redefinir-senha/verificar")
    public String verificarCodigo(@RequestParam String email,
                                  @RequestParam String codigo,
                                  Model model) {
        try {
            usuarioService.validarCodigoRecuperacao(email, codigo);
            model.addAttribute("etapaNovaSenha", true);
            model.addAttribute("email", email);
            model.addAttribute("codigo", codigo);
        } catch (RuntimeException ex) {
            model.addAttribute("erro", ex.getMessage());
            model.addAttribute("email", email);
            model.addAttribute("codigo", codigo);
        }
        return "redefinir-senha";
    }

    @PostMapping("/redefinir-senha")
    public String redefinirSenha(@RequestParam String email,
                                  @RequestParam String codigo,
                                  @RequestParam String novaSenha,
                                  @RequestParam String confirmacaoSenha,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        try {
            usuarioService.redefinirSenhaComCodigo(email, codigo, novaSenha, confirmacaoSenha);
            redirectAttributes.addFlashAttribute("sucesso", "Senha redefinida com sucesso! Faça login com sua nova senha.");
            return "redirect:/login";
        } catch (RuntimeException ex) {
            model.addAttribute("erro", ex.getMessage());
            model.addAttribute("email", email);
            model.addAttribute("codigo", codigo);
            model.addAttribute("etapaNovaSenha", true);
            return "redefinir-senha";
        }
    }
}
