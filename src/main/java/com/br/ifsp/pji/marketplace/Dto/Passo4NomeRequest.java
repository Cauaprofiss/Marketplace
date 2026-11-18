package com.br.ifsp.pji.marketplace.Dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Entrada validada para definir o nome final do anuncio durante o wizard.
public class Passo4NomeRequest {

    @NotBlank(message = "Informe o nome do anuncio")
    @Size(max = 45, message = "O nome deve ter no maximo 45 caracteres")
    private String nome;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }
}
