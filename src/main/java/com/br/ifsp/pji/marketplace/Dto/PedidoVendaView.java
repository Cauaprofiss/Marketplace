package com.br.ifsp.pji.marketplace.Dto;

import java.math.BigDecimal;

import com.br.ifsp.pji.marketplace.model.Produto;
import com.br.ifsp.pji.marketplace.model.Usuario;

public class PedidoVendaView {

    private final Long idCompraVendedor;
    private final Long idAgendamento;
    private final Produto produtoPrincipal;
    private final int itensAdicionais;
    private final Usuario comprador;
    private final String statusChave;
    private final String statusLabel;
    private final BigDecimal total;
    private final boolean confirmadoComprador;
    private final boolean confirmadoVendedor;

    public PedidoVendaView(
            Long idCompraVendedor,
            Long idAgendamento,
            Produto produtoPrincipal,
            int itensAdicionais,
            Usuario comprador,
            String statusChave,
            String statusLabel,
            BigDecimal total,
            boolean confirmadoComprador,
            boolean confirmadoVendedor) {

        this.idCompraVendedor = idCompraVendedor;
        this.idAgendamento = idAgendamento;
        this.produtoPrincipal = produtoPrincipal;
        this.itensAdicionais = itensAdicionais;
        this.comprador = comprador;
        this.statusChave = statusChave;
        this.statusLabel = statusLabel;
        this.total = total;
        this.confirmadoComprador = confirmadoComprador;
        this.confirmadoVendedor = confirmadoVendedor;
    }

    public Long getIdCompraVendedor() {
        return idCompraVendedor;
    }

    public Long getIdAgendamento() {
        return idAgendamento;
    }

    public Produto getProdutoPrincipal() {
        return produtoPrincipal;
    }

    public int getItensAdicionais() {
        return itensAdicionais;
    }

    public Usuario getComprador() {
        return comprador;
    }

    public String getStatusChave() {
        return statusChave;
    }

    public String getStatusLabel() {
        return statusLabel;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public boolean isConfirmadoComprador() {
        return confirmadoComprador;
    }

    public boolean isConfirmadoVendedor() {
        return confirmadoVendedor;
    }

    /** Ex: "Bolo de chocolate e mais 2 itens" */
    public String getProdutoResumo() {

        if (produtoPrincipal == null) {
            return "Nenhum item encontrado";
        }

        if (itensAdicionais <= 0) {
            return produtoPrincipal.getNome();
        }

        return produtoPrincipal.getNome()
                + " e mais " + itensAdicionais
                + (itensAdicionais > 1 ? " itens" : " item");
    }
}