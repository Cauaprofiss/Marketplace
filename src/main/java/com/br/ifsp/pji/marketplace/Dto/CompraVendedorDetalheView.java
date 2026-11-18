package com.br.ifsp.pji.marketplace.Dto;

import java.math.BigDecimal;
import java.util.List;

import com.br.ifsp.pji.marketplace.model.Agendamento;
import com.br.ifsp.pji.marketplace.model.ItemCompra;
import com.br.ifsp.pji.marketplace.model.Pagamento;
import com.br.ifsp.pji.marketplace.model.Usuario;

// Agrupa os dados de uma parte do pedido pertencente a um vendedor especifico.
public class CompraVendedorDetalheView {

    private final Long idCompraVendedor;
    private final String codigo;
    private final Usuario comprador;
    private final List<ItemCompra> itens;
    private final BigDecimal total;
    private final Agendamento agendamento;
    private final Pagamento pagamento;

    public CompraVendedorDetalheView(Long idCompraVendedor, String codigo, Usuario comprador,
            List<ItemCompra> itens, BigDecimal total, Agendamento agendamento, Pagamento pagamento) {
        this.idCompraVendedor = idCompraVendedor;
        this.codigo = codigo;
        this.comprador = comprador;
        this.itens = List.copyOf(itens);
        this.total = total;
        this.agendamento = agendamento;
        this.pagamento = pagamento;
    }

    public Long getIdCompraVendedor() { return idCompraVendedor; }
    public String getCodigo() { return codigo; }
    public Usuario getComprador() { return comprador; }
    public List<ItemCompra> getItens() { return itens; }
    public BigDecimal getTotal() { return total; }
    public Agendamento getAgendamento() { return agendamento; }
    public Pagamento getPagamento() { return pagamento; }
}
