package com.br.ifsp.pji.marketplace.Dto;

import com.br.ifsp.pji.marketplace.model.Produto;

import java.math.BigDecimal;

// Resposta enxuta usada para devolver ao cliente os dados principais de um anuncio publicado.
public class AnuncioPublicadoResponse {

    private Long idProduto;
    private String nome;
    private String status;
    private BigDecimal preco;
    private String fotoUrl;

    public static AnuncioPublicadoResponse of(Produto produto) {
        AnuncioPublicadoResponse dto = new AnuncioPublicadoResponse();
        dto.idProduto = produto.getIdProduto();
        dto.nome = produto.getNome();
        dto.status = produto.getStatus().name();
        dto.preco = produto.getPreco();
        dto.fotoUrl = produto.getFotoUrl();
        return dto;
    }

    public Long getIdProduto() { return idProduto; }
    public String getNome() { return nome; }
    public String getStatus() { return status; }
    public BigDecimal getPreco() { return preco; }
    public String getFotoUrl() { return fotoUrl; }
}