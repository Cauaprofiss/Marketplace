package com.br.ifsp.pji.marketplace.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Turma {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_turma")
	private long idTurma;
    private String nomeTurma;
    private String descricao;
    
    public long getIdTurma() {
		return idTurma;
	}
    public void setIdTurma(long idTurma) {
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
