package com.br.ifsp.pji.marketplace.model;

import java.time.LocalDateTime;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

// Nome composto: tabela real é "TokenRedefinicaoSenha" (sem underscore)
@Entity
@Table(name = "`TokenRedefinicaoSenha`")
// Codigo temporario usado para validar a recuperacao de senha por e-mail.
public class TokenRedefinicaoSenha {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idRedefinicao;

    @ManyToOne
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    private String token;
    private LocalDateTime dataUsado;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataTermino;

    public TokenRedefinicaoSenha() {}

    public Long getIdRedefinicao() { return idRedefinicao; }
    public void setIdRedefinicao(Long idRedefinicao) { this.idRedefinicao = idRedefinicao; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public LocalDateTime getDataUsado() { return dataUsado; }
    public void setDataUsado(LocalDateTime dataUsado) { this.dataUsado = dataUsado; }

    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }

    public LocalDateTime getDataTermino() { return dataTermino; }
    public void setDataTermino(LocalDateTime dataTermino) { this.dataTermino = dataTermino; }
}