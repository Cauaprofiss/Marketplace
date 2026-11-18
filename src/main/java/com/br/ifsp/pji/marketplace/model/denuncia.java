package com.br.ifsp.pji.marketplace.model;

import java.time.LocalDateTime;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
// Registro de uma denuncia e do ciclo de moderacao aplicado ao anuncio denunciado.
public class Denuncia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idDenuncia;

    @ManyToOne
    @JoinColumn(name = "id_denunciante")
    private Usuario denunciante;

    @ManyToOne
    @JoinColumn(name = "id_produto")
    private Produto produto;

    @ManyToOne
    @JoinColumn(name = "id_usuario_denunciado")
    private Usuario usuarioDenunciado;

    private String motivo;
    private String descricao;

    @Enumerated(EnumType.STRING)
    private StatusDenuncia status = StatusDenuncia.pendente;

    private LocalDateTime dataDenuncia;

    @ManyToOne
    @JoinColumn(name = "id_moderador")
    private Usuario moderador;

    private LocalDateTime dataResolucao;
    private String acaoTomada;

    public Denuncia() {}

    public Long getIdDenuncia() { return idDenuncia; }
    public void setIdDenuncia(Long idDenuncia) { this.idDenuncia = idDenuncia; }

    public Usuario getDenunciante() { return denunciante; }
    public void setDenunciante(Usuario denunciante) { this.denunciante = denunciante; }

    public Produto getProduto() { return produto; }
    public void setProduto(Produto produto) { this.produto = produto; }

    public Usuario getUsuarioDenunciado() { return usuarioDenunciado; }
    public void setUsuarioDenunciado(Usuario usuarioDenunciado) { this.usuarioDenunciado = usuarioDenunciado; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public StatusDenuncia getStatus() { return status; }
    public void setStatus(StatusDenuncia status) { this.status = status; }

    public LocalDateTime getDataDenuncia() { return dataDenuncia; }
    public void setDataDenuncia(LocalDateTime dataDenuncia) { this.dataDenuncia = dataDenuncia; }

    public Usuario getModerador() { return moderador; }
    public void setModerador(Usuario moderador) { this.moderador = moderador; }

    public LocalDateTime getDataResolucao() { return dataResolucao; }
    public void setDataResolucao(LocalDateTime dataResolucao) { this.dataResolucao = dataResolucao; }

    public String getAcaoTomada() { return acaoTomada; }
    public void setAcaoTomada(String acaoTomada) { this.acaoTomada = acaoTomada; }
}