package com.br.ifsp.pji.marketplace.Dto;

// Item resumido usado pelo painel para exibir pendencias de moderacao.
public class ItensModeracao {
    private String tipo;      // "Denúncia", "Anúncio", "Usuário"
    private String codigo;    // "D-084", "A-241"...
    private String descricao;
    private String prioridade; // "Alta", "Média", "Baixa"
    private Long id;

    public ItensModeracao(String tipo, String codigo, String descricao, String prioridade, Long id) {
        this.tipo = tipo;
        this.codigo = codigo;
        this.descricao = descricao;
        this.prioridade = prioridade;
        this.id = id;
    }

    public String getTipo() { return tipo; }
    public String getCodigo() { return codigo; }
    public String getDescricao() { return descricao; }
    public String getPrioridade() { return prioridade; }
    public Long getId() { return id; }
}