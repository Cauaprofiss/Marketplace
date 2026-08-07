package com.br.ifsp.pji.marketplace.model;

import java.time.LocalDateTime;

public class tokenRedefinicaoSenha {

	private int idRedefinicao;
    private String token;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataTermino;
    private LocalDateTime dataUsado;

    private Usuario Usuario;
}
