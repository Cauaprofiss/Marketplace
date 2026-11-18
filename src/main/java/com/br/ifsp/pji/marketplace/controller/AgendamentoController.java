package com.br.ifsp.pji.marketplace.controller;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.br.ifsp.pji.marketplace.Dto.AgendamentoView;
import com.br.ifsp.pji.marketplace.Service.AgendamentoService;
import com.br.ifsp.pji.marketplace.model.Agendamento;
import com.br.ifsp.pji.marketplace.model.LocalRetirada;
import com.br.ifsp.pji.marketplace.model.Usuario;

import jakarta.servlet.http.HttpSession;

// Expoe as telas e acoes de agendamento de retirada para compradores e vendedores.
@Controller
@RequestMapping("/agendamentos")
public class AgendamentoController {

    private final AgendamentoService agendamentoService;

    public AgendamentoController(AgendamentoService agendamentoService) {
        this.agendamentoService = agendamentoService;
    }

    @GetMapping
    public String listar(
            @RequestParam(defaultValue = "false") boolean criado,
            HttpSession session,
            Model model) {

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
        if (usuario == null) {
            return "redirect:/login";
        }

        List<AgendamentoView> comoComprador = agendamentoService.listarComoComprador(usuario);
        List<AgendamentoView> comoVendedor = agendamentoService.listarComoVendedor(usuario);

        model.addAttribute("comoComprador", comoComprador);
        model.addAttribute("comoVendedor", comoVendedor);
        if (criado && !model.containsAttribute("sucessoAgendamento")) {
            model.addAttribute("sucessoAgendamento", "Agendamento criado com sucesso.");
        }

        return "agendamentos";
    }

    @GetMapping("/{id}/reagendar")
    public String telaReagendar(@PathVariable Long id, HttpSession session, Model model) {

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
        if (usuario == null) {
            return "redirect:/login";
        }

        Agendamento agendamento;
        try {
            agendamento = agendamentoService.buscarComPermissao(id, usuario);
        } catch (RuntimeException e) {
            return "redirect:/agendamentos";
        }

        boolean ehVendedor = agendamento.getVendedor().getIdUsuario().equals(usuario.getIdUsuario());

        model.addAttribute("agendamento", agendamento);
        model.addAttribute("disponibilidadeRetirada",
                agendamentoService.buscarDisponibilidadeRetirada(agendamento));
        model.addAttribute("ehVendedor", ehVendedor);
        model.addAttribute("dataMinima", LocalDate.now());

        return "agendamento-reagendar";
    }

    @PostMapping("/{id}/atualizar")
    public String atualizar(
            @PathVariable Long id,
            @RequestParam String dataAgendada,
            @RequestParam String horario,
            @RequestParam(required = false) LocalRetirada localRetirada,
            HttpSession session,
            RedirectAttributes redirect) {

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
        if (usuario == null) {
            return "redirect:/login";
        }

        try {
            LocalDate data = LocalDate.parse(dataAgendada);
            LocalTime hora = LocalTime.parse(horario);

            agendamentoService.atualizar(id, usuario, data, hora, localRetirada);

            redirect.addFlashAttribute("sucessoAgendamento", "Agendamento atualizado com sucesso.");
        } catch (RuntimeException e) {
            redirect.addFlashAttribute("erroAgendamento", e.getMessage());
            return "redirect:/agendamentos/" + id + "/reagendar";
        }

        return "redirect:/agendamentos";
    }

    @PostMapping("/{id}/cancelar")
    public String cancelar(
            @PathVariable Long id,
            HttpSession session,
            RedirectAttributes redirect) {

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
        if (usuario == null) {
            return "redirect:/login";
        }

        try {
            agendamentoService.cancelar(id, usuario);
            redirect.addFlashAttribute("sucessoAgendamento", "Agendamento cancelado com sucesso.");
        } catch (RuntimeException e) {
            redirect.addFlashAttribute("erroAgendamento", e.getMessage());
        }

        return "redirect:/agendamentos";
    }

    // NOVO: comprador confirma que recebeu o produto
    @PostMapping("/{id}/confirmar-retirada")
    public String confirmarRetirada(
            @PathVariable Long id,
            HttpSession session,
            RedirectAttributes redirect) {

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
        if (usuario == null) {
            return "redirect:/login";
        }

        try {
            agendamentoService.confirmarRetirada(id, usuario);
            redirect.addFlashAttribute("sucessoAgendamento", "Retirada confirmada. Obrigado!");
        } catch (RuntimeException e) {
            redirect.addFlashAttribute("erroAgendamento", e.getMessage());
        }

        return "redirect:/agendamentos";
    }

    // NOVO: comprador relata que não recebeu (abre denúncia)
    @PostMapping("/{id}/reportar-problema")
    public String reportarProblema(
            @PathVariable Long id,
            @RequestParam(required = false) String motivo,
            HttpSession session,
            RedirectAttributes redirect) {

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
        if (usuario == null) {
            return "redirect:/login";
        }

        try {
            agendamentoService.reportarProblema(id, usuario, motivo);
            redirect.addFlashAttribute("sucessoAgendamento",
                    "Registramos o ocorrido e abrimos uma denúncia para análise.");
        } catch (RuntimeException e) {
            redirect.addFlashAttribute("erroAgendamento", e.getMessage());
        }

        return "redirect:/agendamentos";
    }

    // NOVO: vendedor relata que o comprador não apareceu
    @PostMapping("/{id}/nao-compareceu")
    public String naoCompareceu(
            @PathVariable Long id,
            HttpSession session,
            RedirectAttributes redirect) {

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
        if (usuario == null) {
            return "redirect:/login";
        }

        try {
            agendamentoService.reportarNaoComparecimento(id, usuario);
            redirect.addFlashAttribute("sucessoAgendamento", "Agendamento encerrado.");
        } catch (RuntimeException e) {
            redirect.addFlashAttribute("erroAgendamento", e.getMessage());
        }

        return "redirect:/agendamentos";
    }
}
