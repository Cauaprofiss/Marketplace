package com.br.ifsp.pji.marketplace.Dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.br.ifsp.pji.marketplace.model.LocalRetirada;
import com.br.ifsp.pji.marketplace.model.DisponibilidadeRetirada;

// Mantem os dados temporarios do anuncio enquanto o vendedor percorre o wizard de publicacao.
public class AnuncioWizardDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idProduto;

    private Long idUsuario;

    private String nome;

    private String tipo;

    private List<String> urlsFotos = new ArrayList<>();

    private Long idCategoria;

    private String descricao;

    private Integer quantidade;

    private BigDecimal preco;

    // NOVO: local de retirada definido no passo 5
    private LocalRetirada localRetirada;
    private DisponibilidadeRetirada disponibilidadeRetirada;
    private String finalidade;

    public boolean passo1Completo() {

        return nome != null && !nome.isBlank()
                && tipo != null && !tipo.isBlank();
    }

    public boolean passo2Completo() {

        return urlsFotos != null && !urlsFotos.isEmpty();
    }

    public boolean passo3Completo() {

        return idCategoria != null;
    }

    public boolean passo4Completo() {

        return descricao != null
                && !descricao.isBlank()
                && quantidade != null
                && quantidade > 0;
    }

    public boolean passo5Completo() {
        return preco != null
                && preco.compareTo(BigDecimal.ZERO) > 0
                && quantidade != null
                && quantidade > 0
                && localRetirada != null
                && disponibilidadeRetirada != null;
    }

    public boolean completo() {

        return passo1Completo()
                && passo2Completo()
                && passo3Completo()
                && passo4Completo()
                && passo5Completo();
    }

    public Long getIdProduto() {
        return idProduto;
    }

    public void setIdProduto(Long idProduto) {
        this.idProduto = idProduto;
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public List<String> getUrlsFotos() {
        return urlsFotos;
    }

    public void setUrlsFotos(List<String> urlsFotos) {

        this.urlsFotos = urlsFotos != null
                ? urlsFotos
                : new ArrayList<>();
    }

    public Long getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(Long idCategoria) {
        this.idCategoria = idCategoria;
    }

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

    public BigDecimal getPreco() {
        return preco;
    }

    public void setPreco(BigDecimal preco) {
        this.preco = preco;
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
