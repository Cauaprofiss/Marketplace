package com.br.ifsp.pji.marketplace.Dto;

// Dados enviados pelo formulario quando um usuario denuncia um anuncio.
public class DenunciaRequest {

    private Long idProduto;
    private String motivo;
    private String detalhes;

    public Long getIdProduto() {
        return idProduto;
    }

    public void setIdProduto(Long idProduto) {
        this.idProduto = idProduto;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public String getDetalhes() {
        return detalhes;
    }

    public void setDetalhes(String detalhes) {
        this.detalhes = detalhes;
    }
}
