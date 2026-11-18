package com.br.ifsp.pji.marketplace.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.br.ifsp.pji.marketplace.Dto.PedidoDetalheView;
import com.br.ifsp.pji.marketplace.Dto.PedidoHistoricoView;
import com.br.ifsp.pji.marketplace.Dto.PedidoVendaView;
import com.br.ifsp.pji.marketplace.model.Agendamento;
import com.br.ifsp.pji.marketplace.model.Compra;
import com.br.ifsp.pji.marketplace.model.CompraVendedor;
import com.br.ifsp.pji.marketplace.model.ItemCarrinho;
import com.br.ifsp.pji.marketplace.model.ItemCompra;
import com.br.ifsp.pji.marketplace.model.LocalRetirada;
import com.br.ifsp.pji.marketplace.model.Pagamento;
import com.br.ifsp.pji.marketplace.model.Produto;
import com.br.ifsp.pji.marketplace.model.StatusAgendamento;
import com.br.ifsp.pji.marketplace.model.StatusCompra;
import com.br.ifsp.pji.marketplace.model.StatusPagamento;
import com.br.ifsp.pji.marketplace.model.TipoAgendamento;
import com.br.ifsp.pji.marketplace.model.TipoPagamento;
import com.br.ifsp.pji.marketplace.model.Usuario;
import com.br.ifsp.pji.marketplace.repository.AgendamentoRepository;
import com.br.ifsp.pji.marketplace.repository.CompraRepository;
import com.br.ifsp.pji.marketplace.repository.CompraVendedorRepository;
import com.br.ifsp.pji.marketplace.repository.ItemCarrinhoRepository;
import com.br.ifsp.pji.marketplace.repository.ItemCompraRepository;
import com.br.ifsp.pji.marketplace.repository.PagamentoRepository;

@Service
public class CheckoutService {

    private final CarrinhoService carrinhoService;
    private final ItemCarrinhoRepository itemCarrinhoRepository;
    private final CompraRepository compraRepository;
    private final CompraVendedorRepository compraVendedorRepository;
    private final ItemCompraRepository itemCompraRepository;
    private final AgendamentoRepository agendamentoRepository;
    private final PagamentoRepository pagamentoRepository;
    private final NotificacaoService notificacaoService;
    private static final LocalTime ABERTURA_CAMPUS = LocalTime.of(8, 0);
    private static final LocalTime FECHAMENTO_CAMPUS = LocalTime.of(18, 0);

    /*
     * REGRA: mesmo prazo máximo usado em
     * AgendamentoService.MAX_DIAS_REAGENDAMENTO — a retirada não
     * pode ser marcada para além desse número de dias a partir de
     * hoje (data da compra), já que é a mesma base de contagem do
     * limite de reagendamento depois.
     */
    private static final int MAX_DIAS_RETIRADA = 10;

    public CheckoutService(
            CarrinhoService carrinhoService,
            ItemCarrinhoRepository itemCarrinhoRepository,
            CompraRepository compraRepository,
            CompraVendedorRepository compraVendedorRepository,
            ItemCompraRepository itemCompraRepository,
            AgendamentoRepository agendamentoRepository,
            PagamentoRepository pagamentoRepository,
            NotificacaoService notificacaoService) {

        this.carrinhoService = carrinhoService;
        this.itemCarrinhoRepository = itemCarrinhoRepository;
        this.compraRepository = compraRepository;
        this.compraVendedorRepository = compraVendedorRepository;
        this.itemCompraRepository = itemCompraRepository;
        this.agendamentoRepository = agendamentoRepository;
        this.pagamentoRepository = pagamentoRepository;
        this.notificacaoService = notificacaoService;
    }

    // ============================================================
    // REGISTRO DA COMPRA
    //
    // Aqui já é criado o Agendamento inicial de cada grupo
    // (um por vendedor). Por isso as MESMAS regras de data/horário
    // aplicadas depois em AgendamentoService.atualizar (reagendamento)
    // valem também aqui, na criação: nunca deixamos nascer um
    // agendamento para uma data/horário que já passou.
    // ============================================================

    @Transactional
    public Compra registrarCompra(
            Usuario usuario,
            TipoPagamento tipo,
            LocalDate dataRetirada,
            LocalTime horario) {

        List<ItemCarrinho> itensCarrinho = carrinhoService.listarItens(usuario);

        // REGRA: não dá pra fazer checkout com o carrinho vazio.
        if (itensCarrinho.isEmpty()) {
            throw new RuntimeException("Seu carrinho está vazio.");
        }

        // REGRA: data e horário de retirada são obrigatórios.
        if (dataRetirada == null || horario == null) {
            throw new IllegalArgumentException("Informe data e horário de retirada.");
        }

        /*
         * REGRA: o horário de retirada escolhido precisa estar
         * dentro do expediente do campus (08:00–18:00).
         */
        if (horario.isBefore(ABERTURA_CAMPUS) || horario.isAfter(FECHAMENTO_CAMPUS)) {
            throw new RuntimeException(
                    "Escolha um horário de retirada entre 08:00 e 18:00, dentro do funcionamento do campus."
            );
        }

        /*
         * REGRA (mesma regra de AgendamentoService.atualizar):
         * não dá pra marcar retirada para uma data já passada —
         * nem para "hoje" com um horário que já passou.
         */
        if (dataRetirada.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Escolha uma data de hoje em diante para a retirada.");
        }

        if (dataRetirada.equals(LocalDate.now()) && horario.isBefore(LocalTime.now())) {
            throw new IllegalArgumentException("Escolha um horário que ainda não passou.");
        }

        /*
         * REGRA (mesmo prazo de AgendamentoService.MAX_DIAS_REAGENDAMENTO):
         * a retirada não pode ser agendada para mais de
         * MAX_DIAS_RETIRADA dias a partir de hoje. Como a compra é
         * a própria origem da contagem (dataCriacao do agendamento
         * é "agora"), esse limite aqui é equivalente ao limite de
         * reagendamento aplicado depois.
         */
        LocalDate limiteRetirada = LocalDate.now().plusDays(MAX_DIAS_RETIRADA);
        if (dataRetirada.isAfter(limiteRetirada)) {
            throw new IllegalArgumentException(
                    "A retirada deve ser agendada em até " + MAX_DIAS_RETIRADA
                            + " dias a partir de hoje (data limite: "
                            + limiteRetirada.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                            + ")."
            );
        }

        // REGRA: todo produto precisa ter local de retirada definido pelo vendedor.
        List<String> semLocal = itensCarrinho.stream()
                .filter(item -> item.getProduto().getLocalRetirada() == null)
                .map(item -> item.getProduto().getNome())
                .collect(Collectors.toList());

        if (!semLocal.isEmpty()) {
            throw new RuntimeException(
                    "O produto \"" + semLocal.get(0)
                            + "\" não possui local de retirada definido pelo vendedor."
            );
        }

        Map<SimpleEntry<Usuario, LocalRetirada>, List<ItemCarrinho>> itensPorGrupo =
                itensCarrinho.stream()
                        .collect(Collectors.groupingBy(item ->
                                new SimpleEntry<>(
                                        item.getProduto().getUsuario(),
                                        item.getProduto().getLocalRetirada()
                                )
                        ));

        // REGRA: pagamento via Pix exige que todo vendedor do grupo tenha chave Pix cadastrada.
        if (tipo == TipoPagamento.pix) {

            List<String> semChavePix = itensPorGrupo.keySet().stream()
                    .map(SimpleEntry::getKey)
                    .distinct()
                    .filter(vendedor ->
                            vendedor.getChavePix() == null
                                    || vendedor.getChavePix().isBlank())
                    .map(Usuario::getNome)
                    .collect(Collectors.toList());

            if (!semChavePix.isEmpty()) {
                throw new RuntimeException(
                        "Pagamento via Pix indisponível: "
                                + String.join(", ", semChavePix)
                                + " ainda não cadastrou uma chave Pix. "
                                + "Escolha pagamento presencial."
                );
            }
        }

        Compra compra = new Compra();

        compra.setComprador(usuario);
        compra.setDataCompra(LocalDate.now());
        compra.setStatus(StatusCompra.pendente);

        compra = compraRepository.save(compra);

        for (Map.Entry<SimpleEntry<Usuario, LocalRetirada>, List<ItemCarrinho>> entrada
                : itensPorGrupo.entrySet()) {

            Usuario vendedor = entrada.getKey().getKey();
            LocalRetirada local = entrada.getKey().getValue();
            List<ItemCarrinho> itensDoGrupo = entrada.getValue();

            BigDecimal totalGrupo = itensDoGrupo.stream()
                    .map(item ->
                            item.getProduto()
                                    .getPreco()
                                    .multiply(
                                            BigDecimal.valueOf(
                                                    item.getQuantidade()
                                            )
                                    )
                    )
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            CompraVendedor compraVendedor = new CompraVendedor();

            compraVendedor.setCompra(compra);
            compraVendedor.setVendedor(vendedor);
            compraVendedor.setValorTotal(totalGrupo);

            compraVendedor =
                    compraVendedorRepository.save(compraVendedor);

            for (ItemCarrinho itemCarrinho : itensDoGrupo) {

                Produto produto = itemCarrinho.getProduto();

                ItemCompra itemCompra = new ItemCompra();

                itemCompra.setCompraVendedor(compraVendedor);
                itemCompra.setProduto(produto);
                itemCompra.setQuantidade(itemCarrinho.getQuantidade());
                itemCompra.setPrecoUnitario(produto.getPreco());

                itemCompraRepository.save(itemCompra);
            }

            /*
             * REGRA: um mesmo vendedor não pode ter dois
             * agendamentos ativos (pendente ou confirmado) na
             * mesma data/horário — evita duas retiradas
             * marcadas ao mesmo tempo para o mesmo vendedor.
             */
            boolean conflito = agendamentoRepository
                    .findByVendedorIdUsuarioAndDataAgendadaAndHorarioAndStatusIn(
                            vendedor.getIdUsuario(),
                            dataRetirada,
                            horario,
                            List.of(StatusAgendamento.pendente, StatusAgendamento.confirmado)
                    )
                    .stream()
                    .findAny()
                    .isPresent();

            if (conflito) {
                throw new RuntimeException(
                        "O vendedor " + vendedor.getNome()
                                + " já possui um agendamento nesse mesmo horário. Escolha outro horário."
                );
            }

            // Cria o agendamento inicial do grupo já com data/horário validados acima.
            Agendamento agendamento = new Agendamento();

            agendamento.setCompraVendedor(compraVendedor);
            agendamento.setCompra(compra);
            agendamento.setComprador(usuario);
            agendamento.setVendedor(vendedor);
            agendamento.setDataAgendada(dataRetirada);
            agendamento.setHorario(horario);
            agendamento.setLocalRetirada(local);
            agendamento.setTipo(TipoAgendamento.retirada);
            agendamento.setStatus(StatusAgendamento.pendente);

            /*
             * dataCriacao é preenchida automaticamente pelo
             * @PrePersist de Agendamento (aoCriar()) no momento
             * deste save — é ela que vira a base do limite de
             * MAX_DIAS_REAGENDAMENTO em AgendamentoService.
             */
            agendamentoRepository.save(agendamento);

            notificacaoService.notificarAgendamentoCriado(
                    usuario,
                    vendedor.getNome()
            );

            notificacaoService.notificarAgendamentoCriado(
                    vendedor,
                    usuario.getNome()
            );

            Pagamento pagamento = new Pagamento();

            pagamento.setCompraVendedor(compraVendedor);
            pagamento.setCompra(compra);
            pagamento.setTipo(tipo);
            pagamento.setValor(totalGrupo);
            pagamento.setStatus(StatusPagamento.pendente);

            pagamentoRepository.save(pagamento);

            String nomesProdutosDoGrupo = itensDoGrupo.stream()
                    .map(item -> item.getProduto().getNome())
                    .collect(Collectors.joining(", "));

            notificacaoService.notificarNovaVenda(
                    vendedor,
                    nomesProdutosDoGrupo
            );
        }

        itemCarrinhoRepository.deleteAll(itensCarrinho);

        String codigoPedido =
                "CM-" + String.format("%04d", compra.getIdCompra());

        notificacaoService.notificarNovoPedido(
                usuario,
                codigoPedido
        );

        return compra;
    }

    // ============================================================
    // PAGAMENTO PRESENCIAL?
    //
    // true se QUALQUER grupo (vendedor) dessa compra tiver
    // escolhido pagamento presencial — usado pelo controller para
    // decidir o texto/tela exibida na confirmação do pedido.
    // ============================================================

    public boolean pagamentoPresencial(Long idCompra) {
        return listarGruposDaCompra(idCompra).stream()
                .map(grupo -> pagamentoRepository.findByCompraVendedorIdCompraVendedor(grupo.getIdCompraVendedor()))
                .flatMap(java.util.Optional::stream)
                .anyMatch(pagamento -> pagamento.getTipo() == TipoPagamento.presencial);
    }

    // ============================================================
    // CONFIRMAÇÃO DO PAGAMENTO PIX
    // ============================================================

    @Transactional
    public void confirmarPagamentoPix(Long idPagamento, Usuario usuario) {

        Pagamento pagamento = buscarPagamento(idPagamento, usuario);

        pagamento.setStatus(StatusPagamento.pago);
        pagamento.setDataPagamento(LocalDate.now());

        pagamentoRepository.save(pagamento);

        Compra compra =
                pagamento.getCompraVendedor().getCompra();

        List<CompraVendedor> grupos =
                compraVendedorRepository.findByCompraIdCompra(
                        compra.getIdCompra()
                );

        boolean todosPagos = grupos.stream().allMatch(grupo -> {

            Pagamento p = pagamentoRepository
                    .findByCompraVendedorIdCompraVendedor(
                            grupo.getIdCompraVendedor()
                    )
                    .orElse(null);

            return p != null
                    && p.getStatus() == StatusPagamento.pago;
        });

        if (todosPagos) {

            compra.setStatus(StatusCompra.concluida);

            compraRepository.save(compra);

            String codigoPedido =
                    "CM-" + String.format(
                            "%04d",
                            compra.getIdCompra()
                    );

            notificacaoService.notificarPagamentoConfirmado(
                    compra.getComprador(),
                    codigoPedido
            );
        }
    }

    // ============================================================
    // BUSCAR PAGAMENTO
    // ============================================================

    public Pagamento buscarPagamento(
            Long idPagamento,
            Usuario usuario) {

        Pagamento pagamento =
                pagamentoRepository.findById(idPagamento)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Pagamento não encontrado."
                                )
                        );

        Usuario comprador =
                pagamento
                        .getCompraVendedor()
                        .getCompra()
                        .getComprador();

        if (!comprador.getIdUsuario()
                .equals(usuario.getIdUsuario())) {

            throw new RuntimeException(
                    "Este pagamento não pertence à sua conta."
            );
        }

        return pagamento;
    }

    // ============================================================
    // BUSCAR COMPRA
    // ============================================================

    public Compra buscarCompra(
            Long idCompra,
            Usuario usuario) {

        Compra compra =
                compraRepository.findById(idCompra)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Compra não encontrada."
                                )
                        );

        if (!compra.getComprador()
                .getIdUsuario()
                .equals(usuario.getIdUsuario())) {

            throw new RuntimeException(
                    "Esta compra não pertence à sua conta."
            );
        }

        return compra;
    }

    // ============================================================
    // GRUPOS DA COMPRA
    // ============================================================

    public List<CompraVendedor> listarGruposDaCompra(
            Long idCompra) {

        return compraVendedorRepository
                .findByCompraIdCompra(idCompra);
    }

    // ============================================================
    // ITENS DO GRUPO
    // ============================================================

    public List<ItemCompra> listarItensDoGrupo(
            Long idCompraVendedor) {

        return itemCompraRepository
                .findByCompraVendedorIdCompraVendedor(
                        idCompraVendedor
                );
    }

    // ============================================================
    // PAGAMENTOS PIX PENDENTES
    // ============================================================

    public List<Pagamento> listarPagamentosPixPendentes(
            Long idCompra) {

        return listarGruposDaCompra(idCompra)
                .stream()
                .map(grupo ->
                        pagamentoRepository
                                .findByCompraVendedorIdCompraVendedor(
                                        grupo.getIdCompraVendedor()
                                )
                                .orElse(null)
                )
                .filter(p ->
                        p != null
                                && p.getTipo() == TipoPagamento.pix
                                && p.getStatus() == StatusPagamento.pendente
                )
                .collect(Collectors.toList());
    }

    // ============================================================
    // RF08 - HISTÓRICO DE COMPRAS (visão do COMPRADOR)
    // ============================================================

    public List<PedidoHistoricoView> listarHistorico(
            Usuario usuario) {

        List<Compra> compras =
                compraRepository
                        .findByCompradorIdUsuarioOrderByIdCompraDesc(
                                usuario.getIdUsuario()
                        );

        List<PedidoHistoricoView> resultado =
                new ArrayList<>();

        for (Compra compra : compras) {

            List<CompraVendedor> grupos =
                    listarGruposDaCompra(
                            compra.getIdCompra()
                    );

            for (CompraVendedor grupo : grupos) {
                resultado.add(
                        montarHistoricoView(grupo)
                );
            }
        }

        return resultado;
    }

    private PedidoHistoricoView montarHistoricoView(
            CompraVendedor grupo) {

        List<ItemCompra> itens =
                listarItensDoGrupo(
                        grupo.getIdCompraVendedor()
                );

        BigDecimal total = itens.stream()
                .map(item ->
                        item.getPrecoUnitario()
                                .multiply(
                                        BigDecimal.valueOf(
                                                item.getQuantidade()
                                        )
                                )
                )
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                );

        Produto produtoPrincipal =
                itens.isEmpty()
                        ? null
                        : itens.get(0).getProduto();

        int itensAdicionais =
                itens.size() > 1
                        ? itens.size() - 1
                        : 0;

        Agendamento agendamento =
                agendamentoRepository
                        .findByCompraVendedorIdCompraVendedor(
                                grupo.getIdCompraVendedor()
                        )
                        .orElse(null);

        String statusChave;
        String statusLabel;

        if (agendamento == null) {

            statusChave = "pendente";
            statusLabel = "Em andamento";

        } else {

            switch (agendamento.getStatus()) {

                case concluido:

                    statusChave = "concluida";
                    statusLabel = "Concluído";

                    break;

                case cancelado:

                    statusChave = "cancelada";
                    statusLabel = "Cancelado";

                    break;

                default:

                    /*
                     * Ainda não concluído. Se um dos lados já
                     * confirmou, deixa isso explícito no rótulo
                     * em vez de só "Em andamento".
                     */
                    if (agendamento.isConfirmadoComprador() || agendamento.isConfirmadoVendedor()) {
                        statusChave = "pendente";
                        statusLabel = "Aguardando confirmação";
                    } else {
                        statusChave = "pendente";
                        statusLabel = "Em andamento";
                    }

                    break;
            }
        }

        return new PedidoHistoricoView(
                grupo.getIdCompraVendedor(),
                produtoPrincipal,
                itensAdicionais,
                statusChave,
                statusLabel,
                total
        );
    }

    // ============================================================
    // RF - VENDAS (visão do VENDEDOR)
    // ============================================================

    public List<PedidoVendaView> listarVendas(Usuario vendedor) {

        List<CompraVendedor> grupos =
                compraVendedorRepository.findByVendedorIdUsuarioOrderByIdCompraVendedorDesc(
                        vendedor.getIdUsuario()
                );

        List<PedidoVendaView> resultado = new ArrayList<>();

        for (CompraVendedor grupo : grupos) {
            resultado.add(montarVendaView(grupo));
        }

        return resultado;
    }

    private PedidoVendaView montarVendaView(CompraVendedor grupo) {

        List<ItemCompra> itens = listarItensDoGrupo(grupo.getIdCompraVendedor());

        BigDecimal total = itens.stream()
                .map(item -> item.getPrecoUnitario().multiply(BigDecimal.valueOf(item.getQuantidade())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Produto produtoPrincipal = itens.isEmpty() ? null : itens.get(0).getProduto();
        int itensAdicionais = itens.size() > 1 ? itens.size() - 1 : 0;

        Agendamento agendamento = agendamentoRepository
                .findByCompraVendedorIdCompraVendedor(grupo.getIdCompraVendedor())
                .orElse(null);

        String statusChave;
        String statusLabel;
        boolean confirmadoComprador = agendamento != null && agendamento.isConfirmadoComprador();
        boolean confirmadoVendedor = agendamento != null && agendamento.isConfirmadoVendedor();

        if (agendamento == null) {
            statusChave = "pendente";
            statusLabel = "Em andamento";
        } else {
            switch (agendamento.getStatus()) {
                case concluido:
                    statusChave = "concluida";
                    statusLabel = "Concluído";
                    break;
                case cancelado:
                    statusChave = "cancelada";
                    statusLabel = "Cancelado";
                    break;
                default:
                    statusChave = "pendente";
                    statusLabel = confirmadoComprador
                            ? "Aguarda sua confirmação"
                            : "Em andamento";
                    break;
            }
        }

        return new PedidoVendaView(
                grupo.getIdCompraVendedor(),
                agendamento != null ? agendamento.getIdAgendamento() : null,
                produtoPrincipal,
                itensAdicionais,
                grupo.getCompra().getComprador(),
                statusChave,
                statusLabel,
                total,
                confirmadoComprador,
                confirmadoVendedor
        );
    }

    // ============================================================
    // RF - ACOMPANHAR PEDIDO (visão do COMPRADOR)
    // ============================================================

    public PedidoDetalheView buscarDetalhePedido(
            Long idCompraVendedor,
            Usuario usuario) {

        CompraVendedor grupo =
                compraVendedorRepository
                        .findById(idCompraVendedor)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Pedido não encontrado."
                                )
                        );

        if (!grupo.getCompra()
                .getComprador()
                .getIdUsuario()
                .equals(usuario.getIdUsuario())) {

            throw new RuntimeException(
                    "Este pedido não pertence à sua conta."
            );
        }

        List<ItemCompra> itens =
                listarItensDoGrupo(idCompraVendedor);

        Produto produtoPrincipal =
                itens.isEmpty()
                        ? null
                        : itens.get(0).getProduto();

        int quantidadeTotal =
                itens.stream()
                        .mapToInt(ItemCompra::getQuantidade)
                        .sum();

        Pagamento pagamento =
                pagamentoRepository
                        .findByCompraVendedorIdCompraVendedor(
                                idCompraVendedor
                        )
                        .orElse(null);

        boolean pagamentoConfirmado =
                pagamento != null
                        && pagamento.getStatus()
                                == StatusPagamento.pago;

        String formaPagamento = pagamento != null && pagamento.getTipo() != null
                ? (pagamento.getTipo() == TipoPagamento.pix ? "Pix" : "Pagamento presencial")
                : "A combinar";

        LocalDateTime dataPagamento =
                (pagamento != null
                        && pagamento.getDataPagamento() != null)
                        ? pagamento
                                .getDataPagamento()
                                .atStartOfDay()
                        : null;

        Agendamento agendamento =
                agendamentoRepository
                        .findByCompraVendedorIdCompraVendedor(
                                idCompraVendedor
                        )
                        .orElse(null);

        boolean retirado =
                agendamento != null;

        LocalDateTime dataRetirada =
                (agendamento != null
                        && agendamento.getDataAgendada() != null
                        && agendamento.getHorario() != null)
                        ? LocalDateTime.of(
                                agendamento.getDataAgendada(),
                                agendamento.getHorario()
                        )
                        : null;

        String localRetirada =
                (agendamento != null
                        && agendamento.getLocalRetirada() != null)
                        ? agendamento
                                .getLocalRetirada()
                                .getLabel()
                        : null;

        boolean entregue =
                agendamento != null
                        && agendamento.getStatus()
                                == StatusAgendamento.concluido;

        boolean confirmadoComprador =
                agendamento != null && agendamento.isConfirmadoComprador();

        boolean confirmadoVendedor =
                agendamento != null && agendamento.isConfirmadoVendedor();

        Usuario vendedorUsuario =
                grupo.getVendedor();

        PedidoDetalheView.VendedorResumo vendedor =
                new PedidoDetalheView.VendedorResumo(
                        vendedorUsuario.getIdUsuario(),
                        vendedorUsuario.getNome(),
                        vendedorUsuario.getImagem()
                );

        PedidoDetalheView.ProdutoResumo produto =
                produtoPrincipal != null
                        ? new PedidoDetalheView.ProdutoResumo(
                                produtoPrincipal.getIdProduto(),
                                produtoPrincipal.getNome(),
                                produtoPrincipal.getFotoUrl(),
                                produtoPrincipal.getPreco()
                        )
                        : null;

        String codigo =
                "CM-" + String.format(
                        "%04d",
                        idCompraVendedor
                );

        LocalDateTime dataCriacao =
                grupo.getCompra().getDataCompra() != null
                        ? grupo.getCompra()
                                .getDataCompra()
                                .atStartOfDay()
                        : null;

        return new PedidoDetalheView(
                idCompraVendedor,
                codigo,
                vendedor,
                produto,
                quantidadeTotal,
                dataCriacao,
                pagamentoConfirmado,
                dataPagamento,
                retirado,
                dataRetirada,
                localRetirada,
                entregue,
                agendamento != null ? agendamento.getIdAgendamento() : null,
                confirmadoComprador,
                confirmadoVendedor,
                formaPagamento,
                produtoPrincipal != null && produtoPrincipal.getDisponibilidadeRetirada() != null
                        ? produtoPrincipal.getDisponibilidadeRetirada().getLabel()
                        : null,
                dataRetirada != null && dataRetirada.toLocalDate().equals(LocalDate.now()),
                agendamento != null && agendamento.getStatus() == StatusAgendamento.cancelado
        );
    }

    // ============================================================
    // CONFIRMAÇÃO DUPLA DE ENTREGA
    //
    // O pedido só vira "concluido" quando comprador E vendedor
    // confirmarem. Isso evita fechar o pedido sem entrega/pagamento
    // terem realmente acontecido.
    // ============================================================

    /**
     * Lado do COMPRADOR: "Eu recebi o produto."
     */
    @Transactional
    public void confirmarEntrega(Long idCompraVendedor, Usuario usuario) {

        CompraVendedor grupo = compraVendedorRepository.findById(idCompraVendedor)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado."));

        if (!grupo.getCompra().getComprador().getIdUsuario().equals(usuario.getIdUsuario())) {
            throw new RuntimeException("Este pedido não pertence à sua conta.");
        }

        Agendamento agendamento = agendamentoRepository
                .findByCompraVendedorIdCompraVendedor(idCompraVendedor)
                .orElseThrow(() -> new RuntimeException("Nenhum agendamento associado a este pedido."));

        validarAgendamentoConfirmavel(agendamento);

        if (agendamento.isConfirmadoComprador()) {
            throw new RuntimeException("Você já confirmou o recebimento deste pedido.");
        }

        agendamento.setConfirmadoComprador(true);
        agendamentoRepository.save(agendamento);

        boolean concluiu = finalizarSeAmbosConfirmaram(agendamento);

        if (!concluiu) {
            notificacaoService.notificarAgendamentoAtualizado(grupo.getVendedor(), usuario.getNome());
        }
    }

    /**
     * Lado do VENDEDOR: "Eu entreguei o produto" — e, se o
     * pagamento for presencial, é aqui que ele confirma que
     * recebeu o dinheiro (é ele quem está com o dinheiro em mãos,
     * não o comprador).
     */
    @Transactional
    public void confirmarEntregaVendedor(Long idCompraVendedor, Usuario usuario) {

        CompraVendedor grupo = compraVendedorRepository.findById(idCompraVendedor)
                .orElseThrow(() -> new RuntimeException("Pedido não encontrado."));

        if (!grupo.getVendedor().getIdUsuario().equals(usuario.getIdUsuario())) {
            throw new RuntimeException("Este pedido não pertence à sua conta.");
        }

        Agendamento agendamento = agendamentoRepository
                .findByCompraVendedorIdCompraVendedor(idCompraVendedor)
                .orElseThrow(() -> new RuntimeException("Nenhum agendamento associado a este pedido."));

        validarAgendamentoConfirmavel(agendamento);

        if (agendamento.isConfirmadoVendedor()) {
            throw new RuntimeException("Você já confirmou a entrega deste pedido.");
        }

        agendamento.setConfirmadoVendedor(true);
        agendamentoRepository.save(agendamento);

        // Pagamento presencial: quem confirma o recebimento do dinheiro é o vendedor.
        pagamentoRepository.findByCompraVendedorIdCompraVendedor(idCompraVendedor)
                .ifPresent(pagamento -> {
                    if (pagamento.getTipo() != TipoPagamento.pix
                            && pagamento.getStatus() != StatusPagamento.pago) {
                        pagamento.setStatus(StatusPagamento.pago);
                        pagamento.setDataPagamento(LocalDate.now());
                        pagamentoRepository.save(pagamento);
                    }
                });

        boolean concluiu = finalizarSeAmbosConfirmaram(agendamento);

        if (!concluiu) {
            notificacaoService.notificarAgendamentoAtualizado(grupo.getCompra().getComprador(), usuario.getNome());
        }
    }

    private void validarAgendamentoConfirmavel(Agendamento agendamento) {

        if (agendamento.getStatus() == StatusAgendamento.cancelado) {
            throw new RuntimeException("Este pedido foi cancelado e não pode mais ser confirmado.");
        }

        if (agendamento.getStatus() == StatusAgendamento.concluido) {
            throw new RuntimeException("Este pedido já foi concluído.");
        }
    }

    /**
     * Se os dois lados já confirmaram, marca o agendamento (e,
     * consequentemente, o pedido) como concluído. Retorna true
     * se concluiu agora.
     */
    private boolean finalizarSeAmbosConfirmaram(Agendamento agendamento) {

        if (agendamento.isConfirmadoComprador() && agendamento.isConfirmadoVendedor()) {

            agendamento.setStatus(StatusAgendamento.concluido);
            agendamentoRepository.save(agendamento);

            notificacaoService.notificarRetiradaConfirmada(
                    agendamento.getVendedor(),
                    agendamento.getComprador().getNome()
            );

            notificacaoService.notificarRetiradaConfirmada(
                    agendamento.getComprador(),
                    agendamento.getVendedor().getNome()
            );

            return true;
        }

        return false;
    }

    // ============================================================
    // BUSCAR ID DO AGENDAMENTO
    // ============================================================

    public Long buscarIdAgendamento(
            Long idCompraVendedor) {

        return agendamentoRepository
                .findByCompraVendedorIdCompraVendedor(
                        idCompraVendedor
                )
                .map(Agendamento::getIdAgendamento)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Nenhum agendamento associado a este pedido."
                        )
                );
    }
}
