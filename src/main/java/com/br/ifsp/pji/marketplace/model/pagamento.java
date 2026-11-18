package com.br.ifsp.pji.marketplace.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;

@Entity
// Registro da forma e do estado de pagamento de uma parte do pedido.
public class Pagamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPagamento;

    @OneToOne
    @JoinColumn(name = "id_compra_vendedor")
    private CompraVendedor compraVendedor;

    /* Compatibilidade com a coluna obrigatória da estrutura original. */
    @ManyToOne
    @JoinColumn(name = "id_compra", nullable = false)
    private Compra compra;

    @Enumerated(EnumType.STRING)
    private TipoPagamento tipo;

    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    private StatusPagamento status = StatusPagamento.pendente;

    private LocalDate dataPagamento;

    public Pagamento() {
    }

    public Long getIdPagamento() {
        return idPagamento;
    }

    public void setIdPagamento(Long idPagamento) {
        this.idPagamento = idPagamento;
    }

    public CompraVendedor getCompraVendedor() {
        return compraVendedor;
    }

    public void setCompraVendedor(CompraVendedor compraVendedor) {
        this.compraVendedor = compraVendedor;
    }

    public Compra getCompra() {
        return compra;
    }

    public void setCompra(Compra compra) {
        this.compra = compra;
    }

    public TipoPagamento getTipo() {
        return tipo;
    }

    public void setTipo(TipoPagamento tipo) {
        this.tipo = tipo;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public StatusPagamento getStatus() {
        return status;
    }

    public void setStatus(StatusPagamento status) {
        this.status = status;
    }

    public LocalDate getDataPagamento() {
        return dataPagamento;
    }

    public void setDataPagamento(LocalDate dataPagamento) {
        this.dataPagamento = dataPagamento;
    }
}
