package com.br.ifsp.pji.marketplace.model;

import java.math.BigDecimal;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

/*
 * Representa a "fatia" de uma Compra referente a um único
 * vendedor. Uma Compra pode ter vários CompraVendedor (um
 * por vendedor presente no carrinho). É aqui que ItemCompra,
 * Agendamento e Pagamento se conectam, cada um relativo a
 * esse vendedor específico.
 */
@Entity
// Recorte do pedido que concentra itens, pagamento e retirada de um vendedor.
public class CompraVendedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCompraVendedor;

    @ManyToOne
    @JoinColumn(name = "id_compra")
    private Compra compra;

    @ManyToOne
    @JoinColumn(name = "id_vendedor")
    private Usuario vendedor;

    private BigDecimal valorTotal;

    public CompraVendedor() {
    }

    public Long getIdCompraVendedor() {
        return idCompraVendedor;
    }

    public void setIdCompraVendedor(Long idCompraVendedor) {
        this.idCompraVendedor = idCompraVendedor;
    }

    public Compra getCompra() {
        return compra;
    }

    public void setCompra(Compra compra) {
        this.compra = compra;
    }

    public Usuario getVendedor() {
        return vendedor;
    }

    public void setVendedor(Usuario vendedor) {
        this.vendedor = vendedor;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    }
}