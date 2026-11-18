package com.br.ifsp.pji.marketplace.model;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Embeddable;

@Embeddable
// Chave composta do favorito: combina o id do usuario com o id do produto.
public class FavoritoId implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long idUsuario;
    private Long idProduto;

    public FavoritoId() {}

    public FavoritoId(Long idUsuario, Long idProduto) {
        this.idUsuario = idUsuario;
        this.idProduto = idProduto;
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
    }

    public Long getIdProduto() {
        return idProduto;
    }

    public void setIdProduto(Long idProduto) {
        this.idProduto = idProduto;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof FavoritoId)) return false;

        FavoritoId that = (FavoritoId) o;

        return Objects.equals(idUsuario, that.idUsuario)
                && Objects.equals(idProduto, that.idProduto);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idUsuario, idProduto);
    }
}