package com.br.ifsp.pji.marketplace.model;

import java.time.LocalDateTime;
import java.util.List;

public class compra {

	private int idCompra;
    private LocalDateTime dataCompra;
    private String status;

    private Cliente cliente;
    private List<itemCompra> itens;
    private pagamento pagamento;
    private agendamento agendamento;
}
