package com.br.ifsp.pji.marketplace.model;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "ProdutoImagem")
// Imagem adicional vinculada a um anuncio; a primeira tambem pode ser a foto principal.
public class ProdutoImagem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idImagem;

    @ManyToOne
    @JoinColumn(name = "id_produto")
    private Produto produto;

    private String urlImagem;

    public ProdutoImagem() {}

    // construtor usado pelo AnuncioService no passo 6 (publicar)
    public ProdutoImagem(Produto produto, String urlImagem) {
        this.produto = produto;
        this.urlImagem = urlImagem;
    }

    public Long getIdImagem() { return idImagem; }
    public void setIdImagem(Long idImagem) { this.idImagem = idImagem; }
    public Produto getProduto() { return produto; }
    public void setProduto(Produto produto) { this.produto = produto; }
    public String getUrlImagem() { return urlImagem; }
    public void setUrlImagem(String urlImagem) { this.urlImagem = urlImagem; }
}