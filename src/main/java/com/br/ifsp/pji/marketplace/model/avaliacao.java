package com.br.ifsp.pji.marketplace.model;

import java.time.LocalDateTime;

public class avaliacao {

	private int idAvaliacao;
    private int nota;
    private String comentario;
    private LocalDateTime dataAvaliacao;

    private Usuario avaliador;
    private Usuario avaliado;
    private itemCompra itemCompra;
}
