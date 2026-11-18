package com.br.ifsp.pji.marketplace.Dto;

import java.math.BigDecimal;

import com.br.ifsp.pji.marketplace.model.Produto;

// Resumo de um pedido usado na listagem do historico do comprador.
public class PedidoHistoricoView {

    private final Long idCompraVendedor;
    private final Produto produtoPrincipal;
    private final int itensAdicionais;
    private final String statusChave;
    private final String statusLabel;
    private final BigDecimal total;

    public PedidoHistoricoView(
            Long idCompraVendedor,
            Produto produtoPrincipal,
            int itensAdicionais,
            String statusChave,
            String statusLabel,
            BigDecimal total) {

        this.idCompraVendedor = idCompraVendedor;
        this.produtoPrincipal = produtoPrincipal;
        this.itensAdicionais = itensAdicionais;
        this.statusChave = statusChave;
        this.statusLabel = statusLabel;
        this.total = total;
    }

    public Long getIdCompraVendedor() {
        return idCompraVendedor;
    }

    public Long getIdPedido() {
        return idCompraVendedor;
    }

    public Produto getProdutoPrincipal() {
        return produtoPrincipal;
    }

    public Produto getProduto() {
        return produtoPrincipal;
    }

    public int getItensAdicionais() {
        return itensAdicionais;
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

    /*
     * Resumo pronto para exibir no template, ex:
     * "Bolo de chocolate e mais 2 itens"
     */
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
