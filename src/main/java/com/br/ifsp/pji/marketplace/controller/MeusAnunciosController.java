package com.br.ifsp.pji.marketplace.controller;

import com.br.ifsp.pji.marketplace.Dto.EditarAnuncioRequest;
import com.br.ifsp.pji.marketplace.Service.AnuncioService;
import com.br.ifsp.pji.marketplace.Service.CategoriaService;
import com.br.ifsp.pji.marketplace.model.Produto;
import com.br.ifsp.pji.marketplace.model.StatusProduto;
import com.br.ifsp.pji.marketplace.model.Usuario;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// Permite ao vendedor consultar, editar, alterar o status e excluir seus anuncios.
@Controller
@RequestMapping("/anuncios")
public class MeusAnunciosController {

    private final AnuncioService anuncioService;
    private final CategoriaService categoriaService;

    public MeusAnunciosController(
            AnuncioService anuncioService,
            CategoriaService categoriaService) {

        this.anuncioService = anuncioService;
        this.categoriaService = categoriaService;
    }

    // ============================================================
    // LISTAGEM (MEUS ANÚNCIOS)
    // ============================================================

    @GetMapping
    public String listar(HttpSession session, Model model) {

        Usuario usuario = obterUsuarioLogado(session);
        if (usuario == null) {
            return "redirect:/login";
        }

        model.addAttribute(
                "anuncios",
                anuncioService.listarMeusAnuncios(usuario.getIdUsuario())
        );

        return "meus-anuncios";
    }

    // ============================================================
    // EDITAR
    // ============================================================

    @GetMapping("/editar/{id}")
    public String exibirEdicao(
            @PathVariable Long id,
            HttpSession session,
            Model model) {

        Usuario usuario = obterUsuarioLogado(session);
        if (usuario == null) {
            return "redirect:/login";
        }

        model.addAttribute("anuncio", anuncioService.buscarPorId(id));
        model.addAttribute("categorias", categoriaService.listarTodas());

        return "editar-anuncio";
    }

    @PostMapping("/editar/{id}")
    public String salvarEdicao(
            @PathVariable Long id,
            @ModelAttribute EditarAnuncioRequest req,
            @RequestParam(value = "imagem", required = false) MultipartFile imagem,
            @RequestParam(value = "fotoSecundaria1", required = false) MultipartFile foto1,
            @RequestParam(value = "fotoSecundaria2", required = false) MultipartFile foto2,
            @RequestParam(value = "fotoSecundaria3", required = false) MultipartFile foto3,
            @RequestParam(value = "urlSecundaria1", defaultValue = "") String url1,
            @RequestParam(value = "urlSecundaria2", defaultValue = "") String url2,
            @RequestParam(value = "urlSecundaria3", defaultValue = "") String url3,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Usuario usuario = obterUsuarioLogado(session);
        if (usuario == null) {
            return "redirect:/login";
        }

        try {
            anuncioService.editarPublicado(id, usuario.getIdUsuario(), req, imagem,
                    java.util.Arrays.asList(foto1, foto2, foto3), java.util.List.of(url1, url2, url3));
            redirectAttributes.addFlashAttribute("sucesso", "Anuncio atualizado com sucesso.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }

        return "redirect:/anuncios";
    }

    // ============================================================
    // ALTERAR STATUS
    // ============================================================

    @PostMapping("/{id}/status")
    public String alterarStatus(
            @PathVariable Long id,
            @RequestParam StatusProduto status,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Usuario usuario = obterUsuarioLogado(session);
        if (usuario == null) {
            return "redirect:/login";
        }

        try {
            anuncioService.alterarStatusPublicado(id, usuario.getIdUsuario(), status);
            redirectAttributes.addFlashAttribute("sucesso", "Status atualizado.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }

        return "redirect:/anuncios";
    }

    // ============================================================
    // EXCLUIR
    // ============================================================

    @PostMapping("/{id}/deletar")
    public String excluir(
            @PathVariable Long id,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Usuario usuario = obterUsuarioLogado(session);
        if (usuario == null) {
            return "redirect:/login";
        }

        try {
            anuncioService.excluirPublicado(id, usuario.getIdUsuario());
            redirectAttributes.addFlashAttribute("sucesso", "Anuncio excluido.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }

        return "redirect:/anuncios";
    }

    // ============================================================
    // HELPER
    // ============================================================

    private Usuario obterUsuarioLogado(HttpSession session) {
        return (Usuario) session.getAttribute("usuarioLogado");
    }
}
