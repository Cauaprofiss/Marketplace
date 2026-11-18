package com.br.ifsp.pji.marketplace.model;

// Locais permitidos para que comprador e vendedor realizem a retirada.
public enum LocalRetirada {
    patio_interno,
    patio_externo,
    quadra;

    public String getLabel() {
        return switch (this) {
            case patio_interno -> "Pátio Interno";
            case patio_externo -> "Pátio Externo";
            case quadra -> "Quadra";
        };
    }
}