package com.br.ifsp.pji.marketplace.Dto;

import java.math.BigDecimal;
import com.br.ifsp.pji.marketplace.model.DisponibilidadeRetirada;
import com.br.ifsp.pji.marketplace.model.LocalRetirada;

// Dados editaveis de um anuncio ja publicado, separados da entidade Produto.
public class EditarAnuncioRequest {

    private String nome;
    private Long categoria;
    private String descricao;
    private BigDecimal valor;
    private Integer quantidade;
    private DisponibilidadeRetirada disponibilidadeRetirada;
    private LocalRetirada localRetirada;
    private String finalidade;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Long getCategoria() {
        return categoria;
    }

    public void setCategoria(Long categoria) {
        this.categoria = categoria;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }

    public DisponibilidadeRetirada getDisponibilidadeRetirada() { return disponibilidadeRetirada; }
    public void setDisponibilidadeRetirada(DisponibilidadeRetirada disponibilidadeRetirada) { this.disponibilidadeRetirada = disponibilidadeRetirada; }
    public LocalRetirada getLocalRetirada() { return localRetirada; }
    public void setLocalRetirada(LocalRetirada localRetirada) { this.localRetirada = localRetirada; }
    public String getFinalidade() { return finalidade; }
    public void setFinalidade(String finalidade) { this.finalidade = finalidade; }
}
