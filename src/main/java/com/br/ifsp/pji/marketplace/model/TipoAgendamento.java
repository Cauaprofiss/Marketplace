package com.br.ifsp.pji.marketplace.model;

// Tipo de encontro associado a uma compra; atualmente o fluxo principal e retirada.
public enum TipoAgendamento {
    retirada,
    entrega,
    combinacao;

    public String getLabel() {
        return switch (this) {
            case retirada -> "Retirada";
            case entrega -> "Entrega";
            case combinacao -> "Combinação";
        };
    }
}