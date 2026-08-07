package com.br.ifsp.pji.marketplace.model;

import java.util.List;

public class produto {
	
	private int idProduto;
	private String nome;
	private String status;
	private String descricao;
	private float preco;
	private int quantidade;
	
	private categoria categoria;
    private Vendedor Vendedor;
    private List<produtoImagem> imagens;
}
