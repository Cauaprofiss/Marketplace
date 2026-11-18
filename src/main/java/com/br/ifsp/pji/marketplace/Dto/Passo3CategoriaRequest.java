package com.br.ifsp.pji.marketplace.Dto;

import jakarta.validation.constraints.NotNull;

// Entrada validada usada para associar o anuncio a uma categoria.
public class Passo3CategoriaRequest {

    @NotNull(message = "Selecione uma categoria")
    private Long idCategoria;

    public Long getIdCategoria() { return idCategoria; }
    public void setIdCategoria(Long idCategoria) { this.idCategoria = idCategoria; }
}