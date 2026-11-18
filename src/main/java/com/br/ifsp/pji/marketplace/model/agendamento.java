package com.br.ifsp.pji.marketplace.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;

@Entity
// Entidade que registra data, horario, local e confirmacoes da retirada de um pedido.
public class Agendamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idAgendamento;

    @OneToOne
    @JoinColumn(name = "id_compra_vendedor")
    private CompraVendedor compraVendedor;

    /*
     * Compatibilidade com a estrutura original do banco, na qual
     * agendamento.id_compra é obrigatório. A associação detalhada
     * continua sendo feita por compraVendedor.
     */
    @ManyToOne
    @JoinColumn(name = "id_compra", nullable = false)
    private Compra compra;

    @ManyToOne
    @JoinColumn(name = "id_comprador")
    private Usuario comprador;

    @ManyToOne
    @JoinColumn(name = "id_vendedor")
    private Usuario vendedor;

    private LocalDate dataAgendada;
    private LocalTime horario;

    /*
     * Data/hora em que o agendamento foi CRIADO originalmente.
     * Usada como base de cálculo do prazo máximo de reagendamento
     * (ver AgendamentoService.MAX_DIAS_REAGENDAMENTO) — essa base
     * nunca muda, mesmo que o agendamento seja reagendado várias
     * vezes depois. Preenchida automaticamente no primeiro save.
     */
    @Column(name = "data_criacao", updatable = false)
    private LocalDateTime dataCriacao;

    @Enumerated(EnumType.STRING)
    @Column(name = "disponibilidade_retirada")
    private DisponibilidadeRetirada disponibilidadeRetirada;

    @Enumerated(EnumType.STRING)
    private LocalRetirada localRetirada;

    @Enumerated(EnumType.STRING)
    private TipoAgendamento tipo = TipoAgendamento.retirada;

    @Enumerated(EnumType.STRING)
    private StatusAgendamento status = StatusAgendamento.pendente;

    @Column(name = "confirmado_comprador", nullable = false)
    private boolean confirmadoComprador = false;

    @Column(name = "confirmado_vendedor", nullable = false)
    private boolean confirmadoVendedor = false;

    public Agendamento() {
    }

    @PrePersist
    private void aoCriar() {
        if (dataCriacao == null) {
            dataCriacao = LocalDateTime.now();
        }
    }

    public Long getIdAgendamento() { return idAgendamento; }
    public void setIdAgendamento(Long idAgendamento) { this.idAgendamento = idAgendamento; }

    public CompraVendedor getCompraVendedor() { return compraVendedor; }
    public void setCompraVendedor(CompraVendedor compraVendedor) { this.compraVendedor = compraVendedor; }

    public Compra getCompra() { return compra; }
    public void setCompra(Compra compra) { this.compra = compra; }

    public Usuario getComprador() { return comprador; }
    public void setComprador(Usuario comprador) { this.comprador = comprador; }

    public Usuario getVendedor() { return vendedor; }
    public void setVendedor(Usuario vendedor) { this.vendedor = vendedor; }

    public LocalDate getDataAgendada() { return dataAgendada; }
    public void setDataAgendada(LocalDate dataAgendada) { this.dataAgendada = dataAgendada; }

    public LocalTime getHorario() { return horario; }
    public void setHorario(LocalTime horario) { this.horario = horario; }

    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }

    public DisponibilidadeRetirada getDisponibilidadeRetirada() { return disponibilidadeRetirada; }
    public void setDisponibilidadeRetirada(DisponibilidadeRetirada disponibilidadeRetirada) {
        this.disponibilidadeRetirada = disponibilidadeRetirada;
    }

    public LocalRetirada getLocalRetirada() { return localRetirada; }
    public void setLocalRetirada(LocalRetirada localRetirada) { this.localRetirada = localRetirada; }

    public TipoAgendamento getTipo() { return tipo; }
    public void setTipo(TipoAgendamento tipo) { this.tipo = tipo; }

    public StatusAgendamento getStatus() { return status; }
    public void setStatus(StatusAgendamento status) { this.status = status; }

    public boolean isConfirmadoComprador() { return confirmadoComprador; }
    public void setConfirmadoComprador(boolean confirmadoComprador) { this.confirmadoComprador = confirmadoComprador; }

    public boolean isConfirmadoVendedor() { return confirmadoVendedor; }
    public void setConfirmadoVendedor(boolean confirmadoVendedor) { this.confirmadoVendedor = confirmadoVendedor; }
}