package com.br.ifsp.pji.marketplace.model;

// Faixas de disponibilidade informadas pelo usuario para combinar atendimentos.
public enum Disponibilidade {
    intervalo,
    tarde,
    manha;

    public String getLabel() {
        return switch (this) {
            case intervalo -> "Intervalo";
            case manha -> "Manhã";
            case tarde -> "Tarde";
        };
    }
}