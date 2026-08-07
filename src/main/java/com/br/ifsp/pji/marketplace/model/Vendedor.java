package com.br.ifsp.pji.marketplace.model;

import java.time.LocalTime;

import jakarta.persistence.Entity;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;

@Entity
public class Vendedor extends Usuario{

	@OneToOne
	@MapsId
	private Usuario usuario;
	private LocalTime horario;
    private statusVendedor status;

    public LocalTime getHorario() {
        return horario;
    }

    public void setHorario(LocalTime horario) {
        this.horario = horario;
    }

    public statusVendedor getStatus() {
        return status;
    }

    public void setStatus(statusVendedor status) {
        this.status = status;
    }
    
    public Usuario getUsuario() {
		return usuario;
	}
    public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}
    
}
