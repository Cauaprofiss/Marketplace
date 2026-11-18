package com.br.ifsp.pji.marketplace.model;

import jakarta.persistence.*;

@Entity
@Table(name = "Conversa")
// Conversa entre comprador e vendedor, opcionalmente vinculada a um anuncio.
public class Conversa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_conversa")
    private Long idConversa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_comprador", nullable = false)
    private Usuario comprador;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_vendedor", nullable = false)
    private Usuario vendedor;

    // Pode ser nulo: conversa iniciada sem vínculo a um anúncio específico
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_produto")
    private Produto anuncio;

    public Long getIdConversa() { return idConversa; }
    public void setIdConversa(Long idConversa) { this.idConversa = idConversa; }

    public Usuario getComprador() { return comprador; }
    public void setComprador(Usuario comprador) { this.comprador = comprador; }

    public Usuario getVendedor() { return vendedor; }
    public void setVendedor(Usuario vendedor) { this.vendedor = vendedor; }

    public Produto getAnuncio() { return anuncio; }
    public void setAnuncio(Produto anuncio) { this.anuncio = anuncio; }
}