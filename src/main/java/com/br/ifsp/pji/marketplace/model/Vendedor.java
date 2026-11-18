package com.br.ifsp.pji.marketplace.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;

@Entity
// Perfil especializado de vendedor ligado ao mesmo id do usuario principal.
public class Vendedor {

    @Id
    private Long idUsuario;

    @OneToOne
    @MapsId
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    private String horario;

    @Enumerated(EnumType.STRING)
    private StatusVendedor status = StatusVendedor.offline;

    public Vendedor() {}

    public Long getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Long idUsuario) { this.idUsuario = idUsuario; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public String getHorario() { return horario; }
    public void setHorario(String horario) { this.horario = horario; }

    public StatusVendedor getStatus() { return status; }
    public void setStatus(StatusVendedor status) { this.status = status; }
}