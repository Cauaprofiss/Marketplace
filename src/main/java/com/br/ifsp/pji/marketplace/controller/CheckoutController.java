package com.br.ifsp.pji.marketplace.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.br.ifsp.pji.marketplace.Dto.PedidoHistoricoView;
import com.br.ifsp.pji.marketplace.Dto.PedidoResumoView;
import com.br.ifsp.pji.marketplace.Service.AgendamentoService;
import com.br.ifsp.pji.marketplace.Service.CarrinhoService;
import com.br.ifsp.pji.marketplace.Service.CheckoutService;
import com.br.ifsp.pji.marketplace.model.Compra;
import com.br.ifsp.pji.marketplace.model.CompraVendedor;
import com.br.ifsp.pji.marketplace.model.ItemCarrinho;
import com.br.ifsp.pji.marketplace.model.ItemCompra;
import com.br.ifsp.pji.marketplace.model.Pagamento;
import com.br.ifsp.pji.marketplace.model.TipoPagamento;
import com.br.ifsp.pji.marketplace.model.Usuario;

import jakarta.servlet.http.HttpSession;

// Coordena as telas do checkout, o registro da compra e a consulta do historico de pedidos.
@Controller
public class CheckoutController {

    private final CarrinhoService carrinhoService;
    private final CheckoutService checkoutService;
    private final AgendamentoService agendamentoService;

    public CheckoutController(
            CarrinhoService carrinhoService,
            CheckoutService checkoutService,
            AgendamentoService agendamentoService) {

        this.carrinhoService = carrinhoService;
        this.checkoutService = checkoutService;
        this.agendamentoService = agendamentoService;
    }

    @GetMapping("/checkout")
    public String checkout(HttpSession session, Model model) {

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");

        if (usuario == null) {
            return "redirect:/login";
        }

        List<ItemCarrinho> itens = carrinhoService.listarItens(usuario);

        if (itens.isEmpty()) {
            return "redirect:/carrinho";
        }

        BigDecimal total = itens.stream()
                .map(item -> item.getProduto().getPreco()
                        .multiply(BigDecimal.valueOf(item.getQuantidade())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Verifica se todos os vendedores dos produtos possuem chave Pix
        boolean pixDisponivel = itens.stream()
                .map(item -> item.getProduto().getUsuario())
                .distinct()
                .allMatch(vendedor ->
                        vendedor.getChavePix() != null
                                && !vendedor.getChavePix().isBlank()
                );

        model.addAttribute("itens", itens);
        model.addAttribute("total", total);
        model.addAttribute("pixDisponivel", pixDisponivel);
        model.addAttribute("dataMinima", LocalDate.now());
        model.addAttribute("locaisRetirada", itens.stream()
                .map(item -> item.getProduto().getLocalRetirada())
                .filter(java.util.Objects::nonNull)
                .map(local -> local.getLabel()).distinct().collect(Collectors.joining(" • ")));
        List<com.br.ifsp.pji.marketplace.model.DisponibilidadeRetirada> disponibilidadesRetirada = itens.stream()
                .map(item -> item.getProduto().getDisponibilidadeRetirada() != null
                        ? item.getProduto().getDisponibilidadeRetirada()
                        : com.br.ifsp.pji.marketplace.model.DisponibilidadeRetirada.intervalo_manha)
                .distinct()
                .collect(Collectors.toList());
        model.addAttribute("disponibilidadesRetirada", disponibilidadesRetirada);
        model.addAttribute("disponibilidadesRetiradaTexto", disponibilidadesRetirada.stream()
                .map(disponibilidade -> disponibilidade.getDescricao())
                .collect(Collectors.joining(" • ")));
        model.addAttribute("horarioRetiradaPadrao", disponibilidadesRetirada.isEmpty()
                ? "09:40"
                : disponibilidadesRetirada.get(0).getHorarioInicio().toString());
        if (!model.containsAttribute("formaPagamento")) {
            model.addAttribute("formaPagamento", pixDisponivel ? TipoPagamento.pix : TipoPagamento.presencial);
        }

        return "pagamento-retirada";
    }

    @PostMapping("/compras/registrar")
    public String registrar(
            @RequestParam("formaPagamento") TipoPagamento formaPagamento,
            @RequestParam(value = "dataRetirada", defaultValue = "") String dataRetirada,
            @RequestParam(value = "horarioRetirada", defaultValue = "") String horarioRetirada,
            HttpSession session,
            RedirectAttributes redirect) {

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");

        if (usuario == null) {
            return "redirect:/login";
        }

        Compra compra;

        redirect.addFlashAttribute("dataRetirada", dataRetirada);
        redirect.addFlashAttribute("horarioRetirada", horarioRetirada);
        redirect.addFlashAttribute("formaPagamento", formaPagamento);

        try {
            if (dataRetirada.isBlank() || horarioRetirada.isBlank()) {
                throw new IllegalArgumentException("Escolha a data e o horário de retirada.");
            }
            LocalDate data = LocalDate.parse(dataRetirada);
            LocalTime horario = LocalTime.parse(horarioRetirada);

            compra = checkoutService.registrarCompra(
                    usuario,
                    formaPagamento,
                    data,
                    horario
            );

        } catch (DateTimeParseException e) {
            redirect.addFlashAttribute("erroCheckout", "Informe uma data e um horário válidos.");
            return "redirect:/checkout";
        } catch (RuntimeException e) {
            redirect.addFlashAttribute("erroCheckout", e.getMessage());
            return "redirect:/checkout";
        }

        redirect.getFlashAttributes().clear();

        if (formaPagamento == TipoPagamento.pix) {
            return "redirect:/compras/pix/" + compra.getIdCompra();
        }

        return "redirect:/compras/concluido/" + compra.getIdCompra();
    }

    /*
     * RF08 - Histórico de pedidos
     */
    @GetMapping("/compras")
    public String historico(
            HttpSession session,
            Model model) {

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");

        if (usuario == null) {
            return "redirect:/login";
        }

        List<PedidoHistoricoView> pedidos =
                checkoutService.listarHistorico(usuario);

        model.addAttribute("pedidos", pedidos);

        return "historico-compras";
    }

    /*
     * Tela "Acompanhar pedido"
     *
     * O ID recebido aqui é o idCompraVendedor,
     * e não o idCompra.
     */
    @GetMapping("/compras/{idCompraVendedor}")
    public String verPedido(
            @PathVariable("idCompraVendedor") Long idCompraVendedor,
            HttpSession session,
            Model model) {

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");

        if (usuario == null) {
            return "redirect:/login";
        }

        try {
            model.addAttribute(
                    "pedido",
                    checkoutService.buscarDetalhePedido(
                            idCompraVendedor,
                            usuario
                    )
            );

            return "meu-pedido";

        } catch (RuntimeException e) {
            return "redirect:/compras";
        }
    }

    /*
     * Confirmação da entrega pelo comprador
     */
    @PostMapping("/compras/{idCompraVendedor}/confirmar-entrega")
    public String confirmarEntrega(
            @PathVariable("idCompraVendedor") Long idCompraVendedor,
            HttpSession session,
            RedirectAttributes redirect) {

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");

        if (usuario == null) {
            return "redirect:/login";
        }

        try {

            checkoutService.confirmarEntrega(
                    idCompraVendedor,
                    usuario
            );

            redirect.addFlashAttribute(
                    "sucesso",
                    "Entrega confirmada. Obrigado!"
            );

        } catch (RuntimeException e) {

            redirect.addFlashAttribute(
                    "erro",
                    e.getMessage()
            );
        }

        return "redirect:/compras/" + idCompraVendedor;
    }

    /*
     * Reportar problema no pedido
     */
    @PostMapping("/compras/{idCompraVendedor}/reportar-problema")
    public String reportarProblema(
            @PathVariable("idCompraVendedor") Long idCompraVendedor,
            @RequestParam(value = "motivo", required = false) String motivo,
            HttpSession session,
            RedirectAttributes redirect) {

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");

        if (usuario == null) {
            return "redirect:/login";
        }

        try {

            Long idAgendamento =
                    checkoutService.buscarIdAgendamento(
                            idCompraVendedor
                    );

            agendamentoService.reportarProblema(
                    idAgendamento,
                    usuario,
                    motivo
            );

            redirect.addFlashAttribute(
                    "sucesso",
                    "Registramos o ocorrido e abrimos uma denúncia para análise."
            );

        } catch (RuntimeException e) {

            redirect.addFlashAttribute(
                    "erro",
                    e.getMessage()
            );
        }

        return "redirect:/compras/" + idCompraVendedor;
    }

    /*
     * Tela de pagamento via Pix
     */
    @GetMapping("/compras/pix/{idCompra}")
    public String telaPix(
            @PathVariable("idCompra") Long idCompra,
            HttpSession session,
            Model model) {

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");

        if (usuario == null) {
            return "redirect:/login";
        }

        // Garante que a compra pertence ao usuário logado
        checkoutService.buscarCompra(idCompra, usuario);

        List<Pagamento> pendentes =
                checkoutService.listarPagamentosPixPendentes(idCompra);

        if (pendentes.isEmpty()) {
            return "redirect:/compras/concluido/" + idCompra;
        }

        /*
         * Um código Pix "copia e cola" por pagamento,
         * somente para exibição.
         * NÃO é salvo no banco.
         */
        List<PagamentoPixView> views = pendentes.stream()
                .map(p -> new PagamentoPixView(
                        p.getIdPagamento(),
                        p.getCompraVendedor()
                                .getVendedor()
                                .getNome(),
                        p.getValor(),
                        p.getCompraVendedor()
                                .getVendedor()
                                .getChavePix()
                ))
                .collect(Collectors.toList());

        BigDecimal total = pendentes.stream()
                .map(Pagamento::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("pagamentos", views);
        model.addAttribute("idCompra", idCompra);
        model.addAttribute("total", total);

        return "pix";
    }

    /*
     * Confirmação do pagamento Pix
     */
    @PostMapping("/compras/pix/confirmar")
    public String confirmarPix(
            @RequestParam("idPagamento") Long idPagamento,
            HttpSession session) {

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");

        if (usuario == null) {
            return "redirect:/login";
        }

        Pagamento pagamento =
                checkoutService.buscarPagamento(
                        idPagamento,
                        usuario
                );

        Long idCompra =
                pagamento.getCompraVendedor()
                        .getCompra()
                        .getIdCompra();

        checkoutService.confirmarPagamentoPix(
                idPagamento,
                usuario
        );

        List<Pagamento> aindaPendentes =
                checkoutService.listarPagamentosPixPendentes(
                        idCompra
                );

        if (!aindaPendentes.isEmpty()) {
            return "redirect:/compras/pix/" + idCompra;
        }

        return "redirect:/compras/concluido/" + idCompra;
    }

    /*
     * Tela de pedido concluído
     */
    @GetMapping("/compras/concluido/{idCompra}")
    public String concluido(
            @PathVariable("idCompra") Long idCompra,
            HttpSession session,
            Model model) {

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");

        if (usuario == null) {
            return "redirect:/login";
        }

        Compra compra =
                checkoutService.buscarCompra(
                        idCompra,
                        usuario
                );

        List<CompraVendedor> grupos =
                checkoutService.listarGruposDaCompra(idCompra);

        List<ItemCompra> todosOsItens = grupos.stream()
                .flatMap(grupo ->
                        checkoutService
                                .listarItensDoGrupo(
                                        grupo.getIdCompraVendedor()
                                )
                                .stream()
                )
                .collect(Collectors.toList());

        BigDecimal total = todosOsItens.stream()
                .map(item ->
                        item.getPrecoUnitario()
                                .multiply(
                                        BigDecimal.valueOf(
                                                item.getQuantidade()
                                        )
                                )
                )
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        /*
         * Monta o resumo dos itens.
         *
         * IMPORTANTE:
         * Primeiro verifica se a lista está vazia,
         * evitando o erro:
         * Index 0 out of bounds for length 0
         */
        String itemResumo;

        if (todosOsItens.isEmpty()) {

            itemResumo = "Nenhum item encontrado";

        } else if (todosOsItens.size() == 1) {

            ItemCompra unico = todosOsItens.get(0);

            itemResumo =
                    unico.getProduto().getNome()
                    + " • "
                    + unico.getQuantidade()
                    + (
                        unico.getQuantidade() > 1
                            ? " unidades"
                            : " unidade"
                    );

        } else {

            itemResumo =
                    todosOsItens.get(0)
                            .getProduto()
                            .getNome()
                    + " e mais "
                    + (todosOsItens.size() - 1)
                    + (
                        (todosOsItens.size() - 1) > 1
                            ? " itens"
                            : " item"
                    );
        }

        String codigo = "CM-" + String.format(
                "%04d",
                compra.getIdCompra()
        );

        /*
         * Nome do vendedor para exibição
         * na tela de pedido concluído.
         */
        String vendedorNome;

        if (grupos.isEmpty()) {

            vendedorNome = null;

        } else if (grupos.size() == 1) {

            vendedorNome =
                    grupos.get(0)
                            .getVendedor()
                            .getNome();

        } else {

            vendedorNome =
                    grupos.get(0)
                            .getVendedor()
                            .getNome()
                    + " e outros vendedores";
        }

        PedidoResumoView pedido =
                new PedidoResumoView(
                        compra.getIdCompra(),
                        codigo,
                        itemResumo,
                        total,
                        vendedorNome
                );

        model.addAttribute("pedido", pedido);
        model.addAttribute("pagamentoPresencial", checkoutService.pagamentoPresencial(idCompra));

        return "pedido-concluido";
    }

    /*
     * Objeto simples utilizado somente para
     * exibir cada card Pix na tela.
     *
     * Não é persistido no banco.
     */
    public static class PagamentoPixView {

        private final Long idPagamento;
        private final String nomeVendedor;
        private final BigDecimal valor;
        private final String codigoPix;

        public PagamentoPixView(
                Long idPagamento,
                String nomeVendedor,
                BigDecimal valor,
                String codigoPix) {

            this.idPagamento = idPagamento;
            this.nomeVendedor = nomeVendedor;
            this.valor = valor;
            this.codigoPix = codigoPix;
        }

        public Long getIdPagamento() {
            return idPagamento;
        }

        public String getNomeVendedor() {
            return nomeVendedor;
        }

        public BigDecimal getValor() {
            return valor;
        }

        public String getCodigoPix() {
            return codigoPix;
        }
    }
}
