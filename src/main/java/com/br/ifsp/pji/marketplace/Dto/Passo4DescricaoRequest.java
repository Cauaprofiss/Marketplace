package com.br.ifsp.pji.marketplace.Dto;

import com.br.ifsp.pji.marketplace.model.LocalRetirada;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// Entrada validada do passo de descricao, estoque e local de retirada do anuncio.
public class Passo4DescricaoRequest {

    @NotBlank(message = "Descreva o produto")
    @Size(max = 250, message = "A descricao deve ter no maximo 250 caracteres")
    private String descricao;

    @NotNull(message = "Informe a quantidade disponivel")
    @Min(value = 1, message = "A quantidade deve ser maior que zero")
    private Integer quantidade;

    @NotNull(message = "Selecione o local de retirada")
    private LocalRetirada localRetirada;

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }

    public LocalRetirada getLocalRetirada() {
        return localRetirada;
    }

    public void setLocalRetirada(LocalRetirada localRetirada) {
        this.localRetirada = localRetirada;
    }
}
