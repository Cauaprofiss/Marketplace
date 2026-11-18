package com.br.ifsp.pji.marketplace.model;

import java.math.BigDecimal;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
// Linha persistida do pedido, preservando produto, quantidade e preco praticado na compra.
public class ItemCompra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idItemCompra;

    @ManyToOne
    @JoinColumn(name = "id_compra_vendedor")
    private CompraVendedor compraVendedor;

    @ManyToOne
    @JoinColumn(name = "id_produto")
    private Produto produto;

    private Integer quantidade;

    private BigDecimal precoUnitario;

    public ItemCompra() {
    }

    public Long getIdItemCompra() {
        return idItemCompra;
    }

    public void setIdItemCompra(Long idItemCompra) {
        this.idItemCompra = idItemCompra;
    }

    public CompraVendedor getCompraVendedor() {
        return compraVendedor;
    }

    public void setCompraVendedor(CompraVendedor compraVendedor) {
        this.compraVendedor = compraVendedor;
    }

    public Produto getProduto() {
        return produto;
    }

    public void setProduto(Produto produto) {
        this.produto = produto;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }

    public BigDecimal getPrecoUnitario() {
        return precoUnitario;
    }

    public void setPrecoUnitario(BigDecimal precoUnitario) {
        this.precoUnitario = precoUnitario;
    }
}