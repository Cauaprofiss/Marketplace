package com.br.ifsp.pji.marketplace.Dto;

import com.br.ifsp.pji.marketplace.model.Produto;
import com.br.ifsp.pji.marketplace.model.Usuario;

// Representa a conversa em formato resumido para a lista lateral da tela de mensagens.
public class ConversaResumo {

    private Long id;
    private Usuario outroUsuario;
    private Produto anuncio;
    private String ultimaMensagem;
    private String tempo;
    private boolean naoLida;
    private boolean online; // TODO: presença em tempo real (websocket) — por ora sempre false
    private String iniciais;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Usuario getOutroUsuario() { return outroUsuario; }
    public void setOutroUsuario(Usuario outroUsuario) { this.outroUsuario = outroUsuario; }

    public Produto getAnuncio() { return anuncio; }
    public void setAnuncio(Produto anuncio) { this.anuncio = anuncio; }

    public String getUltimaMensagem() { return ultimaMensagem; }
    public void setUltimaMensagem(String ultimaMensagem) { this.ultimaMensagem = ultimaMensagem; }

    public String getTempo() { return tempo; }
    public void setTempo(String tempo) { this.tempo = tempo; }

    public boolean isNaoLida() { return naoLida; }
    public void setNaoLida(boolean naoLida) { this.naoLida = naoLida; }

    public boolean isOnline() { return online; }
    public void setOnline(boolean online) { this.online = online; }

    public String getIniciais() { return iniciais; }
    public void setIniciais(String iniciais) { this.iniciais = iniciais; }
}