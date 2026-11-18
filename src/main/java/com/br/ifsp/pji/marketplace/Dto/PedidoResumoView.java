package com.br.ifsp.pji.marketplace.Dto;

import java.math.BigDecimal;

// Projecao curta de pedido para tabelas e listas de acompanhamento.
public class PedidoResumoView {

    private final Long idCompra;
    private final String codigo;
    private final String itemResumo;
    private final BigDecimal total;
    private final String vendedorNome;

    public PedidoResumoView(Long idCompra, String codigo, String itemResumo, BigDecimal total, String vendedorNome) {
        this.idCompra = idCompra;
        this.codigo = codigo;
        this.itemResumo = itemResumo;
        this.total = total;
        this.vendedorNome = vendedorNome;
    }

    public Long getIdPedido() {
        return idCompra;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getItemResumo() {
        return itemResumo;
    }

    public BigDecimal getTotal() {
        return total;
    }
    
    public String getVendedorNome() { 
    	return vendedorNome; 
    }
}