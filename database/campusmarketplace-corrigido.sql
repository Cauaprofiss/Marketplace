-- =============================================================
-- CAMPUS MARKETPLACE
-- Atualizado em 06/10/2026
-- OQ ATUALIZOU: ULTIMA VERSÃO EXCLUI ID CATEGORIA 1 , 2 
-- 
-- =============================================================


CREATE DATABASE campusmarketplace
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
USE campusmarketplace;

-- =============================================================
-- TURMA
-- =============================================================

CREATE TABLE Turma (
    id_turma BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome_turma VARCHAR(4) NOT NULL,
    descricao VARCHAR(255)
);

-- =============================================================
-- USUARIO
-- Dados de acesso, perfil, turma, Pix e disponibilidade.
-- =============================================================

CREATE TABLE Usuario (
    id_usuario BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    sobrenome VARCHAR(150),
    email VARCHAR(40) NOT NULL UNIQUE,
    senha VARCHAR(60) NOT NULL,
    telefone VARCHAR(15) UNIQUE,
    imagem VARCHAR(255),
    descricao VARCHAR(255),
    chave_pix VARCHAR(255),
    disponibilidade ENUM('intervalo','tarde','manha'),
    data_cadastro DATETIME DEFAULT CURRENT_TIMESTAMP,
    id_turma BIGINT NOT NULL,
    tipo_usuario ENUM('usuario','admin') NOT NULL DEFAULT 'usuario',
    bloqueado BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_usuario_turma FOREIGN KEY (id_turma) REFERENCES Turma(id_turma)
);

-- =============================================================
-- CLIENTE
-- Especializacao opcional do usuario como comprador.
-- =============================================================

CREATE TABLE Cliente (
    id_usuario BIGINT PRIMARY KEY,
    CONSTRAINT fk_cliente_usuario FOREIGN KEY (id_usuario) REFERENCES Usuario(id_usuario) ON DELETE CASCADE
);

-- =============================================================
-- VENDEDOR
-- Especializacao opcional do usuario como vendedor.
-- =============================================================

CREATE TABLE Vendedor (
    id_usuario BIGINT PRIMARY KEY,
    horario VARCHAR(255),
    status ENUM('online','offline','disponivel') NOT NULL DEFAULT 'offline',
    CONSTRAINT fk_vendedor_usuario FOREIGN KEY (id_usuario) REFERENCES Usuario(id_usuario) ON DELETE CASCADE
);

-- =============================================================
-- TOKEN DE REDEFINICAO DE SENHA
-- O nome da tabela segue exatamente o mapeamento da entidade.
-- =============================================================

CREATE TABLE TokenRedefinicaoSenha (
    id_redefinicao BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_usuario BIGINT NOT NULL,
    token TEXT NOT NULL,
    data_usado DATETIME,
    data_criacao DATETIME DEFAULT CURRENT_TIMESTAMP,
    data_termino DATETIME NOT NULL,
    CONSTRAINT fk_token_usuario FOREIGN KEY (id_usuario) REFERENCES Usuario(id_usuario) ON DELETE CASCADE
);

-- =============================================================
-- CATEGORIA
-- =============================================================

CREATE TABLE Categoria (
    id_categoria BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(50) NOT NULL UNIQUE
);

-- =============================================================
-- PRODUTO / ANUNCIO
-- =============================================================

CREATE TABLE Produto (
    id_produto BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    status ENUM('rascunho','em_revisao','disponivel','indisponivel','reprovado') NOT NULL DEFAULT 'rascunho',
    descricao TEXT,
    id_categoria BIGINT,
    preco DECIMAL(10,2),
    quantidade INT,
    id_usuario BIGINT NOT NULL,
    foto_url VARCHAR(2048),
    data_publicacao DATETIME,
    disponibilidade_retirada ENUM('intervalo_manha','intervalo_tarde') DEFAULT 'intervalo_manha',
    finalidade VARCHAR(20) NOT NULL DEFAULT 'formatura',
    quantidade_denuncias INT NOT NULL DEFAULT 0,
    codigo_produto VARCHAR(20) NOT NULL UNIQUE,
    chave_produto VARCHAR(255),
    local_retirada ENUM('patio_interno','patio_externo','quadra'),
    CONSTRAINT chk_produto_quantidade CHECK (quantidade IS NULL OR quantidade >= 0),
    CONSTRAINT chk_produto_preco CHECK (preco IS NULL OR preco >= 0),
    CONSTRAINT fk_produto_categoria FOREIGN KEY (id_categoria) REFERENCES Categoria(id_categoria),
    CONSTRAINT fk_produto_usuario FOREIGN KEY (id_usuario) REFERENCES Usuario(id_usuario)
);
CREATE INDEX idx_produto_chave ON Produto(chave_produto);
CREATE INDEX idx_produto_status_publicacao ON Produto(status, data_publicacao);

-- =============================================================
-- IMAGENS DO PRODUTO
-- Um produto pode possuir varias imagens.
-- =============================================================

CREATE TABLE produto_imagem (
    id_imagem BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_produto BIGINT NOT NULL,
    url_imagem VARCHAR(255),
    CONSTRAINT fk_imagem_produto FOREIGN KEY (id_produto) REFERENCES Produto(id_produto) ON DELETE CASCADE
);

-- =============================================================
-- CARRINHO
-- Cada usuario possui seu proprio carrinho.
-- =============================================================

CREATE TABLE Carrinho (
    id_carrinho BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_usuario BIGINT NOT NULL UNIQUE,
    data_criacao DATETIME DEFAULT CURRENT_TIMESTAMP,
    data_atualizacao DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_carrinho_usuario FOREIGN KEY (id_usuario) REFERENCES Usuario(id_usuario) ON DELETE CASCADE
);

-- =============================================================
-- ITENS DO CARRINHO
-- =============================================================

CREATE TABLE item_carrinho (
    id_item_carrinho BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_carrinho BIGINT NOT NULL,
    id_produto BIGINT NOT NULL,
    quantidade INT NOT NULL DEFAULT 1,
    CONSTRAINT chk_itemcarrinho_quantidade CHECK (quantidade > 0),
    CONSTRAINT uk_itemcarrinho_produto UNIQUE (id_carrinho,id_produto),
    CONSTRAINT fk_itemcarrinho_carrinho FOREIGN KEY (id_carrinho) REFERENCES Carrinho(id_carrinho) ON DELETE CASCADE,
    CONSTRAINT fk_itemcarrinho_produto FOREIGN KEY (id_produto) REFERENCES Produto(id_produto) ON DELETE CASCADE
);

-- =============================================================
-- COMPRA
-- Uma compra representa um checkout completo do comprador.
-- =============================================================

CREATE TABLE Compra (
    id_compra BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_comprador BIGINT NOT NULL,
    data_compra DATE NOT NULL,
    status ENUM('pendente','concluida','cancelada') NOT NULL DEFAULT 'pendente',
    CONSTRAINT fk_compra_comprador FOREIGN KEY (id_comprador) REFERENCES Usuario(id_usuario)
);

-- =============================================================
-- COMPRA POR VENDEDOR
-- Divide uma compra por vendedor. Cada grupo possui seus proprios
-- itens, pagamento e agendamento.
-- =============================================================

CREATE TABLE compra_vendedor (
    id_compra_vendedor BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_compra BIGINT NOT NULL,
    id_vendedor BIGINT NOT NULL,
    valor_total DECIMAL(10,2) NOT NULL,
    CONSTRAINT fk_compravendedor_compra FOREIGN KEY (id_compra) REFERENCES Compra(id_compra) ON DELETE CASCADE,
    CONSTRAINT fk_compravendedor_vendedor FOREIGN KEY (id_vendedor) REFERENCES Usuario(id_usuario)
);

-- =============================================================
-- ITENS DA COMPRA
-- Produtos vinculados ao respectivo grupo de vendedor.
-- =============================================================

CREATE TABLE item_compra (
    id_item_compra BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_compra_vendedor BIGINT NOT NULL,
    id_produto BIGINT NOT NULL,
    quantidade INT NOT NULL,
    preco_unitario DECIMAL(10,2) NOT NULL,
    CONSTRAINT chk_itemcompra_quantidade CHECK (quantidade > 0),
    CONSTRAINT fk_itemcompra_compravendedor FOREIGN KEY (id_compra_vendedor) REFERENCES compra_vendedor(id_compra_vendedor) ON DELETE CASCADE,
    CONSTRAINT fk_itemcompra_produto FOREIGN KEY (id_produto) REFERENCES Produto(id_produto)
);

-- =============================================================
-- AGENDAMENTO
-- Um agendamento para cada grupo de compra/vendedor.
-- CORRECAO PRINCIPAL: id_compra foi mantido porque o back-end
-- tambem possui essa associacao obrigatoria na entidade.
-- =============================================================

CREATE TABLE Agendamento (
    id_agendamento BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_compra_vendedor BIGINT NOT NULL UNIQUE,
    id_compra BIGINT NOT NULL,
    id_comprador BIGINT NOT NULL,
    id_vendedor BIGINT NOT NULL,
    data_agendada DATE NOT NULL,
    horario TIME NOT NULL,
    local_retirada ENUM('patio_interno','patio_externo','quadra') NOT NULL,
    tipo ENUM('retirada','entrega','combinacao') NOT NULL DEFAULT 'retirada',
    status ENUM('pendente','confirmado','cancelado','concluido') NOT NULL DEFAULT 'pendente',
    confirmado_comprador BOOLEAN NOT NULL DEFAULT FALSE,
    confirmado_vendedor BOOLEAN NOT NULL DEFAULT FALSE,
    observacao VARCHAR(255),
    data_criacao DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_agendamento_compravendedor FOREIGN KEY (id_compra_vendedor) REFERENCES compra_vendedor(id_compra_vendedor) ON DELETE CASCADE,
    CONSTRAINT fk_agendamento_compra FOREIGN KEY (id_compra) REFERENCES Compra(id_compra) ON DELETE CASCADE,
    CONSTRAINT fk_agendamento_comprador FOREIGN KEY (id_comprador) REFERENCES Usuario(id_usuario),
    CONSTRAINT fk_agendamento_vendedor FOREIGN KEY (id_vendedor) REFERENCES Usuario(id_usuario)
);

-- =============================================================
-- PAGAMENTO
-- Cada vendedor possui seu proprio pagamento no checkout.
-- CORRECOES PRINCIPAIS: id_compra e data_pagamento.
-- =============================================================

CREATE TABLE Pagamento (
    id_pagamento BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_compra_vendedor BIGINT NOT NULL UNIQUE,
    id_compra BIGINT NOT NULL,
    tipo ENUM('pix','presencial') NOT NULL,
    valor DECIMAL(10,2) NOT NULL,
    status ENUM('pago','pendente') NOT NULL DEFAULT 'pendente',
    data_pagamento DATE,
    CONSTRAINT fk_pagamento_compravendedor FOREIGN KEY (id_compra_vendedor) REFERENCES compra_vendedor(id_compra_vendedor) ON DELETE CASCADE,
    CONSTRAINT fk_pagamento_compra FOREIGN KEY (id_compra) REFERENCES Compra(id_compra) ON DELETE CASCADE
);

-- =============================================================
-- CONVERSA
-- Conversa entre comprador e vendedor, podendo estar vinculada
-- a um produto especifico.
-- CORRECAO PRINCIPAL: id_produto.
-- =============================================================

CREATE TABLE Conversa (
    id_conversa BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_comprador BIGINT NOT NULL,
    id_vendedor BIGINT NOT NULL,
    id_produto BIGINT,
    CONSTRAINT fk_conversa_comprador FOREIGN KEY (id_comprador) REFERENCES Usuario(id_usuario),
    CONSTRAINT fk_conversa_vendedor FOREIGN KEY (id_vendedor) REFERENCES Usuario(id_usuario),
    CONSTRAINT fk_conversa_produto FOREIGN KEY (id_produto) REFERENCES Produto(id_produto) ON DELETE SET NULL
);
CREATE INDEX idx_conversa_participantes ON Conversa(id_comprador,id_vendedor);

-- =============================================================
-- MENSAGEM
-- Mensagens trocadas dentro de uma conversa.
-- CORRECAO PRINCIPAL: campo lida.
-- =============================================================

CREATE TABLE Mensagem (
    id_mensagem BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_conversa BIGINT NOT NULL,
    id_remetente BIGINT NOT NULL,
    texto_mensagem TEXT NOT NULL,
    data_envio DATETIME NOT NULL,
    lida BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_mensagem_conversa FOREIGN KEY (id_conversa) REFERENCES Conversa(id_conversa) ON DELETE CASCADE,
    CONSTRAINT fk_mensagem_remetente FOREIGN KEY (id_remetente) REFERENCES Usuario(id_usuario)
);

-- =============================================================
-- NOTIFICACAO
-- Notificacoes individuais exibidas no sininho da aplicacao.
-- CORRECAO PRINCIPAL: campo tipo, que elimina o erro HTTP 500
-- "Unknown column n1_0.tipo".
-- =============================================================

CREATE TABLE Notificacao (
    id_notificacao BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_usuario BIGINT NOT NULL,
    mensagem TEXT NOT NULL,
    tipo ENUM('mensagem','pedido','venda','pagamento','agendamento','anuncio','denuncia','sistema') NOT NULL DEFAULT 'sistema',
    lida BOOLEAN NOT NULL DEFAULT FALSE,
    data DATETIME NOT NULL,
    CONSTRAINT fk_notificacao_usuario FOREIGN KEY (id_usuario) REFERENCES Usuario(id_usuario) ON DELETE CASCADE
);
CREATE INDEX idx_notificacao_usuario_data ON Notificacao(id_usuario,data);

-- =============================================================
-- FAVORITO
-- Produtos salvos por cada usuario.
-- =============================================================

CREATE TABLE Favorito (
    id_usuario BIGINT NOT NULL,
    id_produto BIGINT NOT NULL,
    PRIMARY KEY (id_usuario,id_produto),
    CONSTRAINT fk_favorito_usuario FOREIGN KEY (id_usuario) REFERENCES Usuario(id_usuario) ON DELETE CASCADE,
    CONSTRAINT fk_favorito_produto FOREIGN KEY (id_produto) REFERENCES Produto(id_produto) ON DELETE CASCADE
);

-- =============================================================
-- AVALIACAO
-- Avaliacao vinculada a um item realmente comprado.
-- CORRECAO PRINCIPAL: um item so pode ser avaliado uma vez.
-- =============================================================

CREATE TABLE Avaliacao (
    id_avaliacao BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_item_compra BIGINT NOT NULL,
    id_compra_vendedor BIGINT,
    tipo VARCHAR(20) NOT NULL DEFAULT 'produto',
    id_avaliador BIGINT NOT NULL,
    id_avaliado BIGINT NOT NULL,
    nota TINYINT NOT NULL,
    comentario TEXT,
    data_avaliacao DATETIME NOT NULL,
    CONSTRAINT uk_avaliacao_item_tipo UNIQUE (id_item_compra, tipo),
    CONSTRAINT chk_avaliacao_nota CHECK (nota BETWEEN 1 AND 5),
    CONSTRAINT fk_avaliacao_itemcompra FOREIGN KEY (id_item_compra) REFERENCES item_compra(id_item_compra),
    CONSTRAINT fk_avaliacao_compravendedor FOREIGN KEY (id_compra_vendedor) REFERENCES compra_vendedor(id_compra_vendedor),
    CONSTRAINT fk_avaliacao_avaliador FOREIGN KEY (id_avaliador) REFERENCES Usuario(id_usuario),
    CONSTRAINT fk_avaliacao_avaliado FOREIGN KEY (id_avaliado) REFERENCES Usuario(id_usuario)
);

-- =============================================================
-- DENUNCIA
-- Pode estar relacionada a um produto ou diretamente a um usuario.
-- =============================================================

CREATE TABLE Denuncia (
    id_denuncia BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_denunciante BIGINT NOT NULL,
    id_produto BIGINT,
    id_usuario_denunciado BIGINT,
    motivo VARCHAR(250) NOT NULL,
    descricao TEXT,
    status ENUM('pendente','resolvida','arquivada') NOT NULL DEFAULT 'pendente',
    data_denuncia DATETIME NOT NULL,
    id_moderador BIGINT,
    data_resolucao DATETIME,
    acao_tomada TEXT,
    CONSTRAINT fk_denuncia_denunciante FOREIGN KEY (id_denunciante) REFERENCES Usuario(id_usuario),
    CONSTRAINT fk_denuncia_produto FOREIGN KEY (id_produto) REFERENCES Produto(id_produto) ON DELETE SET NULL,
    CONSTRAINT fk_denuncia_usuario FOREIGN KEY (id_usuario_denunciado) REFERENCES Usuario(id_usuario),
    CONSTRAINT fk_denuncia_moderador FOREIGN KEY (id_moderador) REFERENCES Usuario(id_usuario)
);

-- =============================================================
-- DADOS INICIAIS
-- Categorias basicas para facilitar o primeiro uso da aplicacao.
-- =============================================================

-- Categorias iniciais opcionais. Remova se preferir cadastrar pela aplicacao.
INSERT INTO Categoria (nome) VALUES
('Alimentos'),('Bebidas'),('Eletrônicos'),('Livros'),('Serviços'),('Outros');
