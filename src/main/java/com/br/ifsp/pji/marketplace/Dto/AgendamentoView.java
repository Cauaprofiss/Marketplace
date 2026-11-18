package com.br.ifsp.pji.marketplace.Dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.br.ifsp.pji.marketplace.model.Agendamento;
import com.br.ifsp.pji.marketplace.model.CompraVendedor;
import com.br.ifsp.pji.marketplace.model.LocalRetirada;
import com.br.ifsp.pji.marketplace.model.StatusAgendamento;
import com.br.ifsp.pji.marketplace.model.TipoAgendamento;
import com.br.ifsp.pji.marketplace.model.Usuario;

/*
 * Envolve o Agendamento com um campo calculado (horarioPassou),
 * pra nao ter que fazer aritmetica de data direto no Thymeleaf.
 * Delega os getters que a tela ja usava, pra nao precisar
 * reescrever o HTML inteiro.
 */
// Modelo de apresentacao que facilita mostrar um agendamento no Thymeleaf.
public class AgendamentoView {

    private final Agendamento agendamento;
    private final boolean horarioPassou;
    private final String produtosResumo;

    public AgendamentoView(Agendamento agendamento) {
        this(agendamento, "");
    }

    public AgendamentoView(Agendamento agendamento, String produtosResumo) {
        this.agendamento = agendamento;
        this.produtosResumo = produtosResumo;
        this.horarioPassou = agendamento.getDataAgendada() != null
                && agendamento.getHorario() != null
                && LocalDateTime.of(agendamento.getDataAgendada(), agendamento.getHorario())
                        .isBefore(LocalDateTime.now());
    }

    public Long getIdAgendamento() { return agendamento.getIdAgendamento(); }
    public StatusAgendamento getStatus() { return agendamento.getStatus(); }
    public Usuario getComprador() { return agendamento.getComprador(); }
    public Usuario getVendedor() { return agendamento.getVendedor(); }
    public TipoAgendamento getTipo() { return agendamento.getTipo(); }
    public LocalDate getDataAgendada() { return agendamento.getDataAgendada(); }
    public LocalTime getHorario() { return agendamento.getHorario(); }
    public LocalRetirada getLocalRetirada() { return agendamento.getLocalRetirada(); }
    public CompraVendedor getCompraVendedor() { return agendamento.getCompraVendedor(); }
    public boolean isHorarioPassou() { return horarioPassou; }
    public String getProdutosResumo() { return produtosResumo; }
}
