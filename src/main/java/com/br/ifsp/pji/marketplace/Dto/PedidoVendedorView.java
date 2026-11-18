package com.br.ifsp.pji.marketplace.Dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

// Resumo de pedido formatado para a area de gestao de vendas do vendedor.
public class PedidoVendedorView {

    private final Long idCompraVendedor;
    private final String codigo;
    private final String compradorNome;
    private final String produtoResumo;
    private final int quantidadeTotal;
    private final BigDecimal total;
    private final String statusChave;
    private final String statusLabel;
    private final LocalDate dataRetirada;
    private final LocalTime horarioRetirada;
    private final String localRetirada;
    private final String fotoUrl;

    public PedidoVendedorView(Long idCompraVendedor, String codigo, String compradorNome,
            String produtoResumo, int quantidadeTotal, BigDecimal total, String statusChave,
            String statusLabel, LocalDate dataRetirada, LocalTime horarioRetirada,
            String localRetirada, String fotoUrl) {
        this.idCompraVendedor = idCompraVendedor;
        this.codigo = codigo;
        this.compradorNome = compradorNome;
        this.produtoResumo = produtoResumo;
        this.quantidadeTotal = quantidadeTotal;
        this.total = total;
        this.statusChave = statusChave;
        this.statusLabel = statusLabel;
        this.dataRetirada = dataRetirada;
        this.horarioRetirada = horarioRetirada;
        this.localRetirada = localRetirada;
        this.fotoUrl = fotoUrl;
    }

    public Long getIdCompraVendedor() { return idCompraVendedor; }
    public String getCodigo() { return codigo; }
    public String getCompradorNome() { return compradorNome; }
    public String getProdutoResumo() { return produtoResumo; }
    public int getQuantidadeTotal() { return quantidadeTotal; }
    public BigDecimal getTotal() { return total; }
    public String getStatusChave() { return statusChave; }
    public String getStatusLabel() { return statusLabel; }
    public LocalDate getDataRetirada() { return dataRetirada; }
    public LocalTime getHorarioRetirada() { return horarioRetirada; }
    public String getLocalRetirada() { return localRetirada; }
    public String getFotoUrl() { return fotoUrl; }
}
