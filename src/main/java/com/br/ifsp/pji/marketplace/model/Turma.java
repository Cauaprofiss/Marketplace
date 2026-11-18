package com.br.ifsp.pji.marketplace.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
// Turma academica a que um usuario pertence, incluindo a turma reservada do admin.
public class Turma {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idTurma;

    private String nomeTurma;
    private String descricao;

    public Long getIdTurma() { return idTurma; }
    public void setIdTurma(Long idTurma) { this.idTurma = idTurma; }

    public String getNomeTurma() { return nomeTurma; }
    public void setNomeTurma(String nomeTurma) { this.nomeTurma = nomeTurma; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
}