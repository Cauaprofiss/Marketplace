package com.br.ifsp.pji.marketplace.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class agendamento {

	private int idAgendamento;
    private LocalDate dataAgendada;
    private String horario;
    private String status;
    private String observacao;
    private LocalDateTime dataCriacao;

    private compra compra;
    private Cliente cliente;
    private Vendedor Vendedor;
}
