package com.br.ifsp.pji.marketplace.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.br.ifsp.pji.marketplace.Dto.AgendamentoView;
import com.br.ifsp.pji.marketplace.model.Agendamento;
import com.br.ifsp.pji.marketplace.model.Denuncia;
import com.br.ifsp.pji.marketplace.model.DisponibilidadeRetirada;
import com.br.ifsp.pji.marketplace.model.LocalRetirada;
import com.br.ifsp.pji.marketplace.model.StatusAgendamento;
import com.br.ifsp.pji.marketplace.model.StatusDenuncia;
import com.br.ifsp.pji.marketplace.model.Usuario;
import com.br.ifsp.pji.marketplace.repository.AgendamentoRepository;
import com.br.ifsp.pji.marketplace.repository.DenunciaRepository;
import com.br.ifsp.pji.marketplace.repository.ItemCompraRepository;

@Service
// Valida, cria, altera e conclui agendamentos de retirada entre as partes do pedido.
public class AgendamentoService {

    /*
     * REGRA: reagendamento só é permitido em até 10 dias a partir
     * da CRIAÇÃO do agendamento (não da última data reagendada —
     * a base de contagem nunca muda, mesmo em reagendamentos
     * sucessivos).
     */
    private static final int MAX_DIAS_REAGENDAMENTO = 10;

    private final AgendamentoRepository agendamentoRepository;
    private final DenunciaRepository denunciaRepository;
    private final NotificacaoService notificacaoService;
    private final ItemCompraRepository itemCompraRepository;

    public AgendamentoService(
            AgendamentoRepository agendamentoRepository,
            DenunciaRepository denunciaRepository,
            NotificacaoService notificacaoService,
            ItemCompraRepository itemCompraRepository) {
        this.agendamentoRepository = agendamentoRepository;
        this.denunciaRepository = denunciaRepository;
        this.notificacaoService = notificacaoService;
        this.itemCompraRepository = itemCompraRepository;
    }

    public List<AgendamentoView> listarComoComprador(Usuario usuario) {
        return agendamentoRepository
                .findByCompradorIdUsuarioOrderByDataAgendadaAscHorarioAsc(usuario.getIdUsuario())
                .stream()
                .map(this::paraView)
                .collect(Collectors.toList());
    }

    public List<AgendamentoView> listarComoVendedor(Usuario usuario) {
        return agendamentoRepository
                .findByVendedorIdUsuarioOrderByDataAgendadaAscHorarioAsc(usuario.getIdUsuario())
                .stream()
                .map(this::paraView)
                .collect(Collectors.toList());
    }

    private AgendamentoView paraView(Agendamento agendamento) {
        String produtos = agendamento.getCompraVendedor() == null ? "" : itemCompraRepository
                .findByCompraVendedorIdCompraVendedor(agendamento.getCompraVendedor().getIdCompraVendedor())
                .stream().map(item -> item.getProduto().getNome()).collect(Collectors.joining(", "));
        return new AgendamentoView(agendamento, produtos);
    }

    public Agendamento buscarComPermissao(Long idAgendamento, Usuario usuario) {

        Agendamento agendamento = agendamentoRepository.findById(idAgendamento)
                .orElseThrow(() -> new RuntimeException("Agendamento não encontrado."));

        if (!pertenceAoUsuario(agendamento, usuario)) {
            throw new RuntimeException("Você não tem permissão sobre este agendamento.");
        }

        return agendamento;
    }

    public DisponibilidadeRetirada buscarDisponibilidadeRetirada(Agendamento agendamento) {
        if (agendamento.getDisponibilidadeRetirada() != null) {
            return agendamento.getDisponibilidadeRetirada();
        }

        if (agendamento.getCompraVendedor() != null
                && agendamento.getCompraVendedor().getIdCompraVendedor() != null) {
            DisponibilidadeRetirada disponibilidade = itemCompraRepository
                    .findByCompraVendedorIdCompraVendedor(
                            agendamento.getCompraVendedor().getIdCompraVendedor())
                    .stream()
                    .map(item -> item.getProduto().getDisponibilidadeRetirada())
                    .filter(valor -> valor != null)
                    .findFirst()
                    .orElse(null);

            if (disponibilidade != null) {
                return disponibilidade;
            }
        }

        if (LocalTime.of(9, 40).equals(agendamento.getHorario())) {
            return DisponibilidadeRetirada.intervalo_manha;
        }
        if (LocalTime.of(15, 50).equals(agendamento.getHorario())) {
            return DisponibilidadeRetirada.intervalo_tarde;
        }

        return DisponibilidadeRetirada.intervalo_manha;
    }

    @Transactional
    public Agendamento atualizar(
            Long idAgendamento,
            Usuario usuario,
            LocalDate novaData,
            LocalTime novoHorario,
            LocalRetirada novoLocal) {

        Agendamento agendamento = buscarComPermissao(idAgendamento, usuario);

        if (agendamento.getStatus() == StatusAgendamento.cancelado
                || agendamento.getStatus() == StatusAgendamento.concluido) {
            throw new RuntimeException(
                    "Este agendamento não pode mais ser alterado (status atual: "
                            + agendamento.getStatus() + ")."
            );
        }

        DisponibilidadeRetirada disponibilidade = buscarDisponibilidadeRetirada(agendamento);
        LocalTime horarioAplicado = disponibilidade != null
                ? disponibilidade.getHorarioInicio()
                : novoHorario;

        if (novaData == null || horarioAplicado == null) {
            throw new RuntimeException("Informe data e horário válidos.");
        }

        /*
         * REGRA: não pode reagendar para o passado — nem data,
         * nem horário (quando a nova data é hoje).
         */
        if (novaData.isBefore(LocalDate.now())) {
            throw new RuntimeException("Escolha uma data de hoje em diante.");
        }

        if (novaData.equals(LocalDate.now()) && horarioAplicado.isBefore(LocalTime.now())) {
            throw new RuntimeException("Escolha um horário que ainda não passou.");
        }

        /*
         * REGRA: no máximo MAX_DIAS_REAGENDAMENTO dias a partir da
         * CRIAÇÃO do agendamento. A base de cálculo é sempre a
         * data de criação original — reagendar não "reseta" o
         * prazo, então não dá pra empurrar o pedido indefinidamente
         * reagendando em sequência.
         */
        LocalDate dataBaseCriacao = dataBaseParaLimiteDeReagendamento(agendamento);
        LocalDate limiteReagendamento = dataBaseCriacao.plusDays(MAX_DIAS_REAGENDAMENTO);

        if (novaData.isAfter(limiteReagendamento)) {
            throw new RuntimeException(
                    "O reagendamento só pode ser feito em até " + MAX_DIAS_REAGENDAMENTO
                            + " dias após a criação do pedido (data limite: "
                            + limiteReagendamento.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                            + ")."
            );
        }

        boolean ehVendedor = agendamento.getVendedor().getIdUsuario().equals(usuario.getIdUsuario());
        boolean tentandoMudarLocal = novoLocal != null && !novoLocal.equals(agendamento.getLocalRetirada());

        if (tentandoMudarLocal && !ehVendedor) {
            throw new RuntimeException("Somente o vendedor pode alterar o local de retirada.");
        }

        agendamento.setDataAgendada(novaData);
        agendamento.setHorario(horarioAplicado);
        agendamento.setDisponibilidadeRetirada(disponibilidade);

        if (tentandoMudarLocal) {
            agendamento.setLocalRetirada(novoLocal);
        }

        agendamento.setStatus(StatusAgendamento.confirmado);

        Agendamento salvo = agendamentoRepository.save(agendamento);

        Usuario outraParte = ehVendedor ? salvo.getComprador() : salvo.getVendedor();
        notificacaoService.notificarAgendamentoAtualizado(
                outraParte,
                usuario.getNome(),
                salvo.getIdAgendamento());

        return salvo;
    }

    /**
     * Data base usada para calcular o limite de reagendamento.
     * Usa a data de criação do agendamento (coluna data_criacao);
     * se estiver nula — caso de registros antigos, anteriores à
     * adição desse campo — cai para a data da compra como
     * aproximação segura, e em último caso para hoje (nunca deve
     * acontecer em registros normais).
     */
    private LocalDate dataBaseParaLimiteDeReagendamento(Agendamento agendamento) {

        if (agendamento.getDataCriacao() != null) {
            return agendamento.getDataCriacao().toLocalDate();
        }

        if (agendamento.getCompra() != null && agendamento.getCompra().getDataCompra() != null) {
            return agendamento.getCompra().getDataCompra();
        }

        return LocalDate.now();
    }

    @Transactional
    public void cancelar(Long idAgendamento, Usuario usuario) {

        Agendamento agendamento = buscarComPermissao(idAgendamento, usuario);

        if (agendamento.getStatus() == StatusAgendamento.cancelado
                || agendamento.getStatus() == StatusAgendamento.concluido) {
            throw new RuntimeException("Este agendamento não pode mais ser cancelado.");
        }

        agendamento.setStatus(StatusAgendamento.cancelado);
        agendamentoRepository.save(agendamento);

        boolean ehVendedor = agendamento.getVendedor().getIdUsuario().equals(usuario.getIdUsuario());
        Usuario outraParte = ehVendedor ? agendamento.getComprador() : agendamento.getVendedor();
        notificacaoService.notificarAgendamentoCancelado(
                outraParte,
                usuario.getNome(),
                agendamento.getIdAgendamento());
    }

    // ============================================================
    // NOVO: CONFIRMAÇÃO DE RETIRADA (só o comprador confirma)
    // ============================================================

    @Transactional
    public void confirmarRetirada(Long idAgendamento, Usuario usuario) {

        Agendamento agendamento = buscarComPermissao(idAgendamento, usuario);

        if (!agendamento.getComprador().getIdUsuario().equals(usuario.getIdUsuario())) {
            throw new RuntimeException("Somente o comprador pode confirmar a retirada.");
        }

        if (agendamento.getStatus() == StatusAgendamento.cancelado
                || agendamento.getStatus() == StatusAgendamento.concluido) {
            throw new RuntimeException("Este agendamento não pode mais ser confirmado.");
        }

        agendamento.setStatus(StatusAgendamento.concluido);
        agendamentoRepository.save(agendamento);

        notificacaoService.notificarRetiradaConfirmada(agendamento.getVendedor(), usuario.getNome());
    }

    // ============================================================
    // NOVO: COMPRADOR RELATA QUE NÃO RECEBEU O PRODUTO
    // Cancela o agendamento e abre uma denúncia contra o vendedor.
    // ============================================================

    @Transactional
    public void reportarProblema(Long idAgendamento, Usuario usuario, String motivo) {

        Agendamento agendamento = buscarComPermissao(idAgendamento, usuario);

        if (!agendamento.getComprador().getIdUsuario().equals(usuario.getIdUsuario())) {
            throw new RuntimeException("Somente o comprador pode relatar esse problema.");
        }

        if (agendamento.getStatus() == StatusAgendamento.cancelado
                || agendamento.getStatus() == StatusAgendamento.concluido) {
            throw new RuntimeException("Este agendamento não pode mais ser relatado.");
        }

        agendamento.setStatus(StatusAgendamento.cancelado);
        agendamentoRepository.save(agendamento);

        Denuncia denuncia = new Denuncia();
        denuncia.setDenunciante(usuario);
        denuncia.setUsuarioDenunciado(agendamento.getVendedor());
        denuncia.setMotivo("Retirada combinada não ocorreu");
        denuncia.setDescricao(
                motivo != null && !motivo.isBlank()
                        ? motivo
                        : "Comprador relatou que não recebeu o produto no horário combinado."
        );
        denuncia.setStatus(StatusDenuncia.pendente);
        denuncia.setDataDenuncia(LocalDateTime.now());

        denunciaRepository.save(denuncia);

        notificacaoService.notificarProblemaNaRetirada(agendamento.getVendedor(), usuario.getNome());
    }

    // ============================================================
    // NOVO: VENDEDOR RELATA QUE O COMPRADOR NÃO APARECEU
    // ============================================================

    @Transactional
    public void reportarNaoComparecimento(Long idAgendamento, Usuario usuario) {

        Agendamento agendamento = buscarComPermissao(idAgendamento, usuario);

        if (!agendamento.getVendedor().getIdUsuario().equals(usuario.getIdUsuario())) {
            throw new RuntimeException("Somente o vendedor pode relatar isso.");
        }

        if (agendamento.getStatus() == StatusAgendamento.cancelado
                || agendamento.getStatus() == StatusAgendamento.concluido) {
            throw new RuntimeException("Este agendamento não pode mais ser relatado.");
        }

        agendamento.setStatus(StatusAgendamento.cancelado);
        agendamentoRepository.save(agendamento);

        notificacaoService.notificarCompradorNaoCompareceu(agendamento.getComprador(), usuario.getNome());
    }

    private boolean pertenceAoUsuario(Agendamento agendamento, Usuario usuario) {
        Long id = usuario.getIdUsuario();
        return agendamento.getComprador().getIdUsuario().equals(id)
                || agendamento.getVendedor().getIdUsuario().equals(id);
    }
}