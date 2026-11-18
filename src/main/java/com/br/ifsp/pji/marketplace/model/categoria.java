package com.br.ifsp.pji.marketplace.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
// Categoria usada para organizar e filtrar os anuncios publicados.
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCategoria;

    private String nome;

    public Categoria() {}

    public Long getIdCategoria() { return idCategoria; }
    public void setIdCategoria(Long idCategoria) { this.idCategoria = idCategoria; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
}