package com.br.ifsp.pji.marketplace.model;

import java.time.LocalDateTime;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
@jakarta.persistence.Table(uniqueConstraints = @jakarta.persistence.UniqueConstraint(
        name = "uk_avaliacao_item_tipo", columnNames = {"id_item_compra", "tipo"}))
// Avaliacao feita sobre um produto comprado ou sobre o vendedor daquele pedido.
public class Avaliacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idAvaliacao;

    @ManyToOne
    @JoinColumn(name = "id_item_compra")
    private ItemCompra itemCompra;

    @ManyToOne
    @JoinColumn(name = "id_compra_vendedor")
    private CompraVendedor compraVendedor;

    @ManyToOne
    @JoinColumn(name = "id_avaliador")
    private Usuario avaliador;

    @ManyToOne
    @JoinColumn(name = "id_avaliado")
    private Usuario avaliado;

    private Byte nota;

    @Enumerated(EnumType.STRING)
    private TipoAvaliacao tipo;

    @jakarta.persistence.Column(length = 1000)
    private String comentario;
    private LocalDateTime dataAvaliacao;

    public Avaliacao() {}

    public Long getIdAvaliacao() { return idAvaliacao; }
    public void setIdAvaliacao(Long idAvaliacao) { this.idAvaliacao = idAvaliacao; }

    public ItemCompra getItemCompra() { return itemCompra; }
    public void setItemCompra(ItemCompra itemCompra) { this.itemCompra = itemCompra; }

    public CompraVendedor getCompraVendedor() { return compraVendedor; }
    public void setCompraVendedor(CompraVendedor compraVendedor) { this.compraVendedor = compraVendedor; }

    public Usuario getAvaliador() { return avaliador; }
    public void setAvaliador(Usuario avaliador) { this.avaliador = avaliador; }

    public Usuario getAvaliado() { return avaliado; }
    public void setAvaliado(Usuario avaliado) { this.avaliado = avaliado; }

    public Byte getNota() { return nota; }
    public void setNota(Byte nota) { this.nota = nota; }

    public TipoAvaliacao getTipo() { return tipo; }
    public void setTipo(TipoAvaliacao tipo) { this.tipo = tipo; }

    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }

    public LocalDateTime getDataAvaliacao() { return dataAvaliacao; }
    public void setDataAvaliacao(LocalDateTime dataAvaliacao) { this.dataAvaliacao = dataAvaliacao; }
}
