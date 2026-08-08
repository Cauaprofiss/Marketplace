package com.br.ifsp.pji.marketplace.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;

//entity/Cliente.java
@Entity
@Table(name = "Cliente")
@NoArgsConstructor
public class Cliente {
	
	 @Id @Column(name = "id_usuario")
	 private Long id;
	
	 @OneToOne @MapsId @JoinColumn(name = "id_usuario")
	 private Usuario usuario;
	 
	 /*Getters e Setters*/
	 
	 public Long getId() {
		return id;
	}
	 public void setId(Long id) {
		this.id = id;
	}
	 
	public Usuario getUsuario() {
		return usuario;
	}
	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}
}
