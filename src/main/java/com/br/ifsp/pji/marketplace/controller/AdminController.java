package com.br.ifsp.pji.marketplace.controller;

import com.br.ifsp.pji.marketplace.Service.AdminService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// Controla o painel administrativo, a moderacao de anuncios e o tratamento de denuncias.
@Controller
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    // ============================================================
    // VISÃO GERAL (DASHBOARD)
    // ============================================================

    @GetMapping("/dashboard")
    public String dashboard(Model model) {

        model.addAttribute("totalUsuariosAtivos", adminService.contarUsuariosAtivos());

        model.addAttribute("totalAnunciosAtivos", adminService.contarAnunciosAtivos());

        model.addAttribute("totalDenunciasAbertas", adminService.contarDenunciasAbertas());

        model.addAttribute("denunciasRecentes", adminService.denunciasRecentesPendentes(3));

        return "admin-dashboard";
    }
    
    @GetMapping("/moderacao")
    public String moderacao(Model model) {

        model.addAttribute("denunciasAbertas", adminService.contarDenunciasAbertas());
        model.addAttribute("anunciosEmAnalise", adminService.contarAnunciosEmAnalise());
        model.addAttribute("totalUsuariosAtivos", adminService.contarUsuariosAtivos());
        model.addAttribute("itensModeracao", adminService.listarItensModeracao());

        return "moderacao";
    }

    // ============================================================
    // LISTAGEM DE ANÚNCIOS
    // ============================================================

    @GetMapping("/anuncios")
    public String listarAnuncios(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String busca,
            Model model) {

        model.addAttribute(
                "produtos",
                adminService.listarAnuncios(status, busca)
        );

        model.addAttribute("statusSelecionado", status);

        model.addAttribute("buscaAtual", busca);

        return "anuncios-admin";
    }

    @GetMapping("/denuncias")
    public String listarDenuncias(@RequestParam(required = false) String status, Model model) {
        model.addAttribute("denuncias", adminService.listarDenuncias(status));
        model.addAttribute("statusSelecionado", status);
        return "denuncias-admin";
    }

    @PostMapping("/denuncias/{id}/revisar")
    public String revisarDenuncia(@PathVariable Long id, @RequestParam boolean aprovada,
            RedirectAttributes redirectAttributes) {
        try {
            adminService.revisarDenuncia(id, aprovada);
            redirectAttributes.addFlashAttribute("sucesso", aprovada
                    ? "Denúncia aprovada. O anúncio foi retirado da vitrine."
                    : "Denúncia reprovada. O anúncio continua disponível.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/admin/denuncias";
    }
}
