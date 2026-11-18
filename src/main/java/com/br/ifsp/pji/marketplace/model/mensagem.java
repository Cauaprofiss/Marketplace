package com.br.ifsp.pji.marketplace.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Mensagem")
// Mensagem individual enviada por um participante de uma conversa.
public class Mensagem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_mensagem")
    private Long idMensagem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_conversa", nullable = false)
    private Conversa conversa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_remetente", nullable = false)
    private Usuario remetente;

    @Column(name = "texto_mensagem", nullable = false, columnDefinition = "TEXT")
    private String conteudo;

    @Column(name = "data_envio", nullable = false)
    private LocalDateTime dataEnvio;

    @Column(name = "lida", nullable = false)
    private boolean lida = false;

    public Long getIdMensagem() { return idMensagem; }
    public void setIdMensagem(Long idMensagem) { this.idMensagem = idMensagem; }

    public Conversa getConversa() { return conversa; }
    public void setConversa(Conversa conversa) { this.conversa = conversa; }

    public Usuario getRemetente() { return remetente; }
    public void setRemetente(Usuario remetente) { this.remetente = remetente; }

    public String getConteudo() { return conteudo; }
    public void setConteudo(String conteudo) { this.conteudo = conteudo; }

    public LocalDateTime getDataEnvio() { return dataEnvio; }
    public void setDataEnvio(LocalDateTime dataEnvio) { this.dataEnvio = dataEnvio; }

    public boolean isLida() { return lida; }
    public void setLida(boolean lida) { this.lida = lida; }
}