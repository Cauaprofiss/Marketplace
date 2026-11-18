package com.br.ifsp.pji.marketplace.Dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PedidoDetalheView {

    public static class VendedorResumo {

        private final Long idUsuario;
        private final String nome;
        private final String imagem;

        public VendedorResumo(Long idUsuario, String nome, String imagem) {
            this.idUsuario = idUsuario;
            this.nome = nome;
            this.imagem = imagem;
        }

        public Long getIdUsuario() {
            return idUsuario;
        }

        public String getNome() {
            return nome;
        }

        public String getImagem() {
            return imagem;
        }
    }

    public static class ProdutoResumo {

        private final Long idProduto;
        private final String nome;
        private final String fotoUrl;
        private final BigDecimal preco;

        public ProdutoResumo(Long idProduto, String nome, String fotoUrl, BigDecimal preco) {
            this.idProduto = idProduto;
            this.nome = nome;
            this.fotoUrl = fotoUrl;
            this.preco = preco;
        }

        public Long getIdProduto() {
            return idProduto;
        }

        public String getNome() {
            return nome;
        }

        public String getFotoUrl() {
            return fotoUrl;
        }

        public BigDecimal getPreco() {
            return preco;
        }
    }

    private final Long idPedido; // = idCompraVendedor
    private final String codigo;
    private final VendedorResumo vendedor;
    private final ProdutoResumo produto;
    private final Integer quantidade;
    private final LocalDateTime dataCriacao;
    private final boolean pagamentoConfirmado;
    private final LocalDateTime dataPagamento;
    private final boolean retirado;
    private final LocalDateTime dataRetirada;
    private final String localRetirada;
    private final boolean entregue;
    private final Long idAgendamento;
    private final boolean confirmadoComprador;
    private final boolean confirmadoVendedor;
    private final String formaPagamento;
    private final String disponibilidadeRetirada;
    private final boolean retiradaHoje;
    private final boolean cancelado;

    public PedidoDetalheView(
            Long idPedido,
            String codigo,
            VendedorResumo vendedor,
            ProdutoResumo produto,
            Integer quantidade,
            LocalDateTime dataCriacao,
            boolean pagamentoConfirmado,
            LocalDateTime dataPagamento,
            boolean retirado,
            LocalDateTime dataRetirada,
            String localRetirada,
            boolean entregue,
            Long idAgendamento,
            boolean confirmadoComprador,
            boolean confirmadoVendedor,
            String formaPagamento,
            String disponibilidadeRetirada,
            boolean retiradaHoje,
            boolean cancelado) {

        this.idPedido = idPedido;
        this.codigo = codigo;
        this.vendedor = vendedor;
        this.produto = produto;
        this.quantidade = quantidade;
        this.dataCriacao = dataCriacao;
        this.pagamentoConfirmado = pagamentoConfirmado;
        this.dataPagamento = dataPagamento;
        this.retirado = retirado;
        this.dataRetirada = dataRetirada;
        this.localRetirada = localRetirada;
        this.entregue = entregue;
        this.idAgendamento = idAgendamento;
        this.confirmadoComprador = confirmadoComprador;
        this.confirmadoVendedor = confirmadoVendedor;
        this.formaPagamento = formaPagamento;
        this.disponibilidadeRetirada = disponibilidadeRetirada;
        this.retiradaHoje = retiradaHoje;
        this.cancelado = cancelado;
    }

    public Long getIdPedido() {
        return idPedido;
    }

    public String getCodigo() {
        return codigo;
    }

    public VendedorResumo getVendedor() {
        return vendedor;
    }

    public ProdutoResumo getProduto() {
        return produto;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public boolean isPagamentoConfirmado() {
        return pagamentoConfirmado;
    }

    public LocalDateTime getDataPagamento() {
        return dataPagamento;
    }

    public boolean isRetirado() {
        return retirado;
    }

    public LocalDateTime getDataRetirada() {
        return dataRetirada;
    }

    public String getLocalRetirada() {
        return localRetirada;
    }

    public boolean isEntregue() {
        return entregue;
    }

    public Long getIdAgendamento() {
        return idAgendamento;
    }

    public boolean isConfirmadoComprador() {
        return confirmadoComprador;
    }

    public boolean isConfirmadoVendedor() {
        return confirmadoVendedor;
    }

    public String getFormaPagamento() { return formaPagamento; }
    public String getDisponibilidadeRetirada() { return disponibilidadeRetirada; }
    public boolean isRetiradaHoje() { return retiradaHoje; }
    public boolean isCancelado() { return cancelado; }
}
