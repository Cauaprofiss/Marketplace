package com.br.ifsp.pji.marketplace.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Usuario") 
@NoArgsConstructor @AllArgsConstructor
public class Usuario {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long id;

    private String nome;
    private String sobrenome;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String senha;

    @Column(unique = true, nullable = false)
    private String telefone;

    private String imagem;
    private String descricao;

    @Column(name = "data_cadastro")
    private LocalDateTime dataCadastro;

    @ManyToOne @JoinColumn(name = "id_turma", nullable = false)
    private Turma turma;
    
    /*Getters e Setters*/
    
    public Long getId() {
		return id;
	}
    public void setId(Long id) {
		this.id = id;
	}
    
    public String getNome() {
		return nome;
	}
    public void setNome(String nome) {
		this.nome = nome;
	}
    
    public String getSobrenome() {
		return sobrenome;
	}
    public void setSobrenome(String sobrenome) {
		this.sobrenome = sobrenome;
	}
    
    public String getEmail() {
		return email;
	}
    public void setEmail(String email) {
		this.email = email;
	}
    
    public String getSenha() {
		return senha;
	}
    public void setSenha(String senha) {
		this.senha = senha;
	}
    
    public String getTelefone() {
		return telefone;
	}
    public void setTelefone(String telefone) {
		this.telefone = telefone;
	}
    
    public String getImagem() {
		return imagem;
	}
    public void setImagem(String imagem) {
		this.imagem = imagem;
	}
    
    public String getDescricao() {
		return descricao;
	}
    public void setDescricao(String descricao) {
		this.descricao = descricao;
	}
    
    public LocalDateTime getDataCadastro() {
		return dataCadastro;
	}
    public void setDataCadastro(LocalDateTime dataCadastro) {
		this.dataCadastro = dataCadastro;
	}
    
    public Turma getTurma() {
		return turma;
	}
    public void setTurma(Turma turma) {
		this.turma = turma;
	}
}