package com.br.ifsp.pji.marketplace.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;


//entity/Turma.java
@Entity
@Table(name = "Turma")
@NoArgsConstructor @AllArgsConstructor
public class Turma {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_turma")
    private Long idTurma;

    @Column(name = "nome_turma", length = 4, nullable = false)
    private String nomeTurma;

    private String descricao;
    
    /*Getters e Setters*/
    
    public Long getIdTurma() {
		return idTurma;
	}
    public void setIdTurma(Long idTurma) {
		this.idTurma = idTurma;
	}
    
    public String getNomeTurma() {
		return nomeTurma;
	}
    public void setNomeTurma(String nomeTurma) {
		this.nomeTurma = nomeTurma;
	}
    
    public String getDescricao() {
		return descricao;
	}
    public void setDescricao(String descricao) {
		this.descricao = descricao;
	}
}
