package com.br.ifsp.pji.marketplace.model;

import java.time.LocalTime;

// Janelas de horario oferecidas pelo vendedor para retirada dos produtos.
public enum DisponibilidadeRetirada {
    intervalo_manha("Intervalo Manhã", "9:40 - 10:00", LocalTime.of(9, 40)),
    intervalo_tarde("Intervalo Tarde", "15:50 - 16:10", LocalTime.of(15, 50));

    private final String label;
    private final String faixaHorario;
    private final LocalTime horarioInicio;

    DisponibilidadeRetirada(String label, String faixaHorario, LocalTime horarioInicio) {
        this.label = label;
        this.faixaHorario = faixaHorario;
        this.horarioInicio = horarioInicio;
    }

    public String getLabel() { return label; }
    public String getFaixaHorario() { return faixaHorario; }
    public LocalTime getHorarioInicio() { return horarioInicio; }
    public String getDescricao() { return label + " " + faixaHorario; }
}
