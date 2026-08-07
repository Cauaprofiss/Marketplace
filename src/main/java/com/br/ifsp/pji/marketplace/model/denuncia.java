package com.br.ifsp.pji.marketplace.model;

import java.time.LocalDateTime;

public class denuncia {

	private int idDenuncia;
    private String motivo;
    private String descricao;
    private String status;
    private LocalDateTime dataDenuncia;
    private LocalDateTime dataResolucao;
    private String acaoTomada;

    private Usuario denunciante;
    private Usuario denunciado;
    private Usuario moderador;
    private produto produto;
}

