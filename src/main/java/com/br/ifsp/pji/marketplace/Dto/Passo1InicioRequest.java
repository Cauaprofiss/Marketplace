package com.br.ifsp.pji.marketplace.Dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Entrada validada do primeiro passo: nome provisao e tipo do anuncio.
public class Passo1InicioRequest {

    @NotBlank(message = "Informe um nome provisorio para o anuncio")
    @Size(max = 45, message = "O nome deve ter no maximo 45 caracteres")
    private String nome;

    @NotBlank(message = "Selecione o tipo de anuncio")
    private String tipo; // "PRODUTO" ou "SERVICO"

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
}
