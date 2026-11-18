package com.br.ifsp.pji.marketplace.model;

import java.time.LocalDateTime;
import jakarta.persistence.Entity;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
// Evento destinado a um usuario, exibido como lido ou nao lido no cabecalho.
public class Notificacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idNotificacao;

    @ManyToOne
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    private String mensagem;

    @Column(length = 500)
    private String destino;

    @Enumerated(EnumType.STRING)
    private TipoNotificacao tipo = TipoNotificacao.sistema;

    private Boolean lida = false;
    private LocalDateTime data;

    public Notificacao() {}

    public Long getIdNotificacao() { return idNotificacao; }
    public void setIdNotificacao(Long idNotificacao) { this.idNotificacao = idNotificacao; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public String getMensagem() { return mensagem; }
    public void setMensagem(String mensagem) { this.mensagem = mensagem; }

    public String getDestino() {
        if (destino != null && destino.startsWith("/") && !destino.startsWith("//")) {
            return destino;
        }

        return switch (getTipoExibicao()) {
            case mensagem -> "/conversas";
            case pedido, pagamento -> "/compras";
            case venda -> "/vendas";
            case agendamento -> "/agendamentos";
            case anuncio -> "/anuncios";
            case denuncia, sistema -> "/notificacoes";
        };
    }

    public void setDestino(String destino) { this.destino = destino; }

    public TipoNotificacao getTipo() { return tipo; }
    public void setTipo(TipoNotificacao tipo) { this.tipo = tipo; }

    public String getTitulo() {
        TipoNotificacao tipoAtual = tipo != null ? tipo : inferirTipoLegado();

        return switch (tipoAtual) {
            case mensagem -> "Nova mensagem";
            case pedido -> "Pedido atualizado";
            case venda -> "Nova venda";
            case pagamento -> "Pagamento confirmado";
            case agendamento -> "Agendamento atualizado";
            case anuncio -> "Anúncio atualizado";
            case denuncia -> "Denúncia recebida";
            case sistema -> "Notificação";
        };
    }

    public TipoNotificacao getTipoExibicao() {
        return tipo != null ? tipo : inferirTipoLegado();
    }

    private TipoNotificacao inferirTipoLegado() {
        String texto = mensagem != null ? mensagem.toLowerCase() : "";
        if (texto.contains("mensagem") || texto.contains("respondeu")) return TipoNotificacao.mensagem;
        if (texto.contains("vendeu") || texto.contains("venda")) return TipoNotificacao.venda;
        if (texto.contains("pedido")) return TipoNotificacao.pedido;
        if (texto.contains("pagamento")) return TipoNotificacao.pagamento;
        if (texto.contains("agendamento") || texto.contains("horário")) return TipoNotificacao.agendamento;
        if (texto.contains("anúncio")) return TipoNotificacao.anuncio;
        if (texto.contains("denúncia")) return TipoNotificacao.denuncia;
        return TipoNotificacao.sistema;
    }

    public Boolean getLida() { return lida; }
    public void setLida(Boolean lida) { this.lida = lida; }

    public LocalDateTime getData() { return data; }
    public void setData(LocalDateTime data) { this.data = data; }
}
