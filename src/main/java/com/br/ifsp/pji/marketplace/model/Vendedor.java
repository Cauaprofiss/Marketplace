package com.br.ifsp.pji.marketplace.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.NoArgsConstructor;

//entity/Vendedor.java
@Entity
@Table(name = "Vendedor")
@NoArgsConstructor
public class Vendedor {
	 @Id @Column(name = "id_usuario")
	 private Long id;
	
	 @OneToOne @MapsId @JoinColumn(name = "id_usuario")
	 private Usuario usuario;
	
	 private String horario;
	
	 @Enumerated(EnumType.STRING)
	 private StatusVendedor status = StatusVendedor.offline;
	 
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
	
	public String getHorario() {
		return horario;
	}
	public void setHorario(String horario) {
		this.horario = horario;
	}
	
	public StatusVendedor getStatus() {
		return status;
	}
	public void setStatus(StatusVendedor status) {
		this.status = status;
	}
}
