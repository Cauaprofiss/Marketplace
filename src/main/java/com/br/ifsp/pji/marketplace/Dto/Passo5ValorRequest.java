package com.br.ifsp.pji.marketplace.Dto;

import java.math.BigDecimal;

import com.br.ifsp.pji.marketplace.model.LocalRetirada;
import com.br.ifsp.pji.marketplace.model.DisponibilidadeRetirada;

// Entrada do ultimo passo: preco, quantidade e janela de retirada do produto.
public class Passo5ValorRequest {

    private BigDecimal preco;

    private Integer quantidade;

    private LocalRetirada localRetirada;
    private DisponibilidadeRetirada disponibilidadeRetirada;
    private String finalidade;

    public BigDecimal getPreco() {
        return preco;
    }

    public void setPreco(BigDecimal preco) {
        this.preco = preco;
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

    public DisponibilidadeRetirada getDisponibilidadeRetirada() { return disponibilidadeRetirada; }
    public void setDisponibilidadeRetirada(DisponibilidadeRetirada disponibilidadeRetirada) { this.disponibilidadeRetirada = disponibilidadeRetirada; }
    public String getFinalidade() { return finalidade; }
    public void setFinalidade(String finalidade) { this.finalidade = finalidade; }
}
