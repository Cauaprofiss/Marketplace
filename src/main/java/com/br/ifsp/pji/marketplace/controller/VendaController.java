package com.br.ifsp.pji.marketplace.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.br.ifsp.pji.marketplace.Dto.CompraVendedorDetalheView;
import com.br.ifsp.pji.marketplace.Dto.PedidoVendedorView;
import com.br.ifsp.pji.marketplace.Service.CheckoutService;
import com.br.ifsp.pji.marketplace.model.Agendamento;
import com.br.ifsp.pji.marketplace.model.CompraVendedor;
import com.br.ifsp.pji.marketplace.model.ItemCompra;
import com.br.ifsp.pji.marketplace.model.Pagamento;
import com.br.ifsp.pji.marketplace.model.StatusAgendamento;
import com.br.ifsp.pji.marketplace.model.Usuario;
import com.br.ifsp.pji.marketplace.repository.AgendamentoRepository;
import com.br.ifsp.pji.marketplace.repository.CompraVendedorRepository;
import com.br.ifsp.pji.marketplace.repository.ItemCompraRepository;
import com.br.ifsp.pji.marketplace.repository.PagamentoRepository;

import jakarta.servlet.http.HttpSession;

// Apresenta ao vendedor os pedidos recebidos e permite confirmar a entrega ao comprador.
@Controller
@RequestMapping("/vendas")
public class VendaController {

	private final CompraVendedorRepository compraVendedorRepository;
	private final ItemCompraRepository itemCompraRepository;
	private final AgendamentoRepository agendamentoRepository;
	private final PagamentoRepository pagamentoRepository;
	private final CheckoutService checkoutService;

	public VendaController(CompraVendedorRepository compraVendedorRepository,
	        ItemCompraRepository itemCompraRepository,
	        AgendamentoRepository agendamentoRepository,
	        PagamentoRepository pagamentoRepository,
	        com.br.ifsp.pji.marketplace.Service.CheckoutService checkoutService) {
	    this.compraVendedorRepository = compraVendedorRepository;
	    this.itemCompraRepository = itemCompraRepository;
	    this.agendamentoRepository = agendamentoRepository;
	    this.pagamentoRepository = pagamentoRepository;
	    this.checkoutService = checkoutService;
	}

    @GetMapping
    public String listar(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
        if (usuario == null) return "redirect:/login";
        model.addAttribute("pedidos", montarPedidos(usuario));
        return "gestao-vendas";
    }

    @GetMapping("/{idCompraVendedor}")
    public String detalhar(@PathVariable Long idCompraVendedor, HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
        if (usuario == null) return "redirect:/login";

        CompraVendedor grupo = compraVendedorRepository.findById(idCompraVendedor)
                .orElse(null);
        if (grupo == null || !grupo.getVendedor().getIdUsuario().equals(usuario.getIdUsuario())) {
            return "redirect:/vendas";
        }

        List<ItemCompra> itens = itemCompraRepository
                .findByCompraVendedorIdCompraVendedor(idCompraVendedor);
        Agendamento agendamento = agendamentoRepository
                .findByCompraVendedorIdCompraVendedor(idCompraVendedor).orElse(null);
        Pagamento pagamento = pagamentoRepository
                .findByCompraVendedorIdCompraVendedor(idCompraVendedor).orElse(null);

        model.addAttribute("pedidos", montarPedidos(usuario));
        model.addAttribute("detalhe", new CompraVendedorDetalheView(
                idCompraVendedor, codigo(idCompraVendedor), grupo.getCompra().getComprador(),
                itens, grupo.getValorTotal(), agendamento, pagamento));
        return "gestao-vendas";
    }
    
    @PostMapping("/{idCompraVendedor}/confirmar-entrega")
    public String confirmarEntrega(
            @PathVariable Long idCompraVendedor,
            HttpSession session,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirect) {

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
        if (usuario == null) return "redirect:/login";

        try {
            checkoutService.confirmarEntregaVendedor(idCompraVendedor, usuario);
            redirect.addFlashAttribute("sucessoAgendamento",
                    "Confirmação registrada. Assim que o comprador confirmar também, o pedido é concluído.");
        } catch (RuntimeException e) {
            redirect.addFlashAttribute("erroAgendamento", e.getMessage());
        }

        return "redirect:/vendas/" + idCompraVendedor;
    }

    private List<PedidoVendedorView> montarPedidos(Usuario vendedor) {
        List<PedidoVendedorView> resultado = new ArrayList<>();
        for (CompraVendedor grupo : compraVendedorRepository
                .findByVendedorIdUsuarioOrderByIdCompraVendedorDesc(vendedor.getIdUsuario())) {
            List<ItemCompra> itens = itemCompraRepository
                    .findByCompraVendedorIdCompraVendedor(grupo.getIdCompraVendedor());
            Agendamento agendamento = agendamentoRepository
                    .findByCompraVendedorIdCompraVendedor(grupo.getIdCompraVendedor()).orElse(null);

            String resumo = itens.isEmpty() ? "Pedido sem itens" : itens.get(0).getProduto().getNome();
            if (itens.size() > 1) resumo += " e mais " + (itens.size() - 1) + " item(ns)";
            int quantidade = itens.stream().mapToInt(ItemCompra::getQuantidade).sum();
            BigDecimal total = grupo.getValorTotal() != null ? grupo.getValorTotal() : BigDecimal.ZERO;
            String statusChave = "andamento";
            String statusLabel = "Em andamento";
            if (agendamento != null && agendamento.getStatus() == StatusAgendamento.concluido) {
                statusChave = "concluido";
                statusLabel = "Concluído";
            } else if (agendamento != null && agendamento.getStatus() == StatusAgendamento.cancelado) {
                statusChave = "cancelado";
                statusLabel = "Cancelado";
            }

            resultado.add(new PedidoVendedorView(grupo.getIdCompraVendedor(),
                    codigo(grupo.getIdCompraVendedor()), grupo.getCompra().getComprador().getNome(),
                    resumo, quantidade, total, statusChave, statusLabel,
                    agendamento == null ? null : agendamento.getDataAgendada(),
                    agendamento == null ? null : agendamento.getHorario(),
                    agendamento == null || agendamento.getLocalRetirada() == null
                            ? null : agendamento.getLocalRetirada().getLabel(),
                    itens.isEmpty() ? null : itens.get(0).getProduto().getFotoUrl()));
        }
        return resultado;
    }

    private String codigo(Long id) {
        return "CM-" + String.format("%04d", id);
    }
}
