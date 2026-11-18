package com.br.ifsp.pji.marketplace.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
// Anuncio de produto, com estoque, preco, imagens, status e dados de retirada.
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idProduto;

    @Column(nullable = false, length = 100)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusProduto status = StatusProduto.rascunho;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @ManyToOne
    @JoinColumn(name = "id_categoria")
    private Categoria categoria;

    private BigDecimal preco;

    private Integer quantidade;

    @Column(name = "finalidade", length = 20)
    private String finalidade = "formatura";

    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Column(name = "foto_url", length = 2048)
    private String fotoUrl;

    private LocalDateTime dataPublicacao;

    @Column(nullable = false)
    private Integer quantidadeDenuncias = 0;

    @Column(name = "codigo_produto", nullable = false, unique = true, length = 20)
    private String codigoProduto;

    @Column(name = "chave_produto", length = 255)
    private String chaveProduto;

    // NOVO: onde o produto pode ser retirado após a compra
    @Enumerated(EnumType.STRING)
    @Column(name = "local_retirada")
    private LocalRetirada localRetirada;

    @Enumerated(EnumType.STRING)
    @Column(name = "disponibilidade_retirada")
    private DisponibilidadeRetirada disponibilidadeRetirada;

    public Produto() {
    }

    public Long getIdProduto() {
        return idProduto;
    }

    public void setIdProduto(Long idProduto) {
        this.idProduto = idProduto;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public StatusProduto getStatus() {
        return status;
    }

    public void setStatus(StatusProduto status) {
        this.status = status;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

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

    public String getFinalidade() { return finalidade; }

    public void setFinalidade(String finalidade) { this.finalidade = finalidade; }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getFotoUrl() {
        return fotoUrl;
    }

    public void setFotoUrl(String fotoUrl) {
        this.fotoUrl = fotoUrl;
    }

    public LocalDateTime getDataPublicacao() {
        return dataPublicacao;
    }

    public void setDataPublicacao(LocalDateTime dataPublicacao) {
        this.dataPublicacao = dataPublicacao;
    }

    public Integer getQuantidadeDenuncias() {
        return quantidadeDenuncias;
    }

    public void setQuantidadeDenuncias(Integer quantidadeDenuncias) {
        this.quantidadeDenuncias = quantidadeDenuncias;
    }

    public String getCodigoProduto() {
        return codigoProduto;
    }

    public void setCodigoProduto(String codigoProduto) {
        this.codigoProduto = codigoProduto;
    }

    public String getChaveProduto() {
        return chaveProduto;
    }

    public void setChaveProduto(String chaveProduto) {
        this.chaveProduto = chaveProduto;
    }

    public LocalRetirada getLocalRetirada() {
        return localRetirada;
    }

    public void setLocalRetirada(LocalRetirada localRetirada) {
        this.localRetirada = localRetirada;
    }

    public DisponibilidadeRetirada getDisponibilidadeRetirada() {
        return disponibilidadeRetirada;
    }

    public void setDisponibilidadeRetirada(DisponibilidadeRetirada disponibilidadeRetirada) {
        this.disponibilidadeRetirada = disponibilidadeRetirada;
    }
}
