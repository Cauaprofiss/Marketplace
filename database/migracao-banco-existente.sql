-- =============================================================
-- MIGRACAO DO BANCO ENVIADO PELO USUARIO (PRESERVA OS DADOS)
-- Execute uma unica vez sobre o banco campusmarketplace atual.
-- =============================================================
USE campusmarketplace;

-- Garante as turmas exibidas no formulario de cadastro.
INSERT INTO Turma (nome_turma, descricao)
SELECT 'INI1', 'Informática para Internet, primeiro ano'
WHERE NOT EXISTS (SELECT 1 FROM Turma WHERE UPPER(nome_turma) = 'INI1');
INSERT INTO Turma (nome_turma, descricao)
SELECT 'INI2', 'Informática para Internet, segundo ano'
WHERE NOT EXISTS (SELECT 1 FROM Turma WHERE UPPER(nome_turma) = 'INI2');
INSERT INTO Turma (nome_turma, descricao)
SELECT 'INI3', 'Informática para Internet, terceiro ano'
WHERE NOT EXISTS (SELECT 1 FROM Turma WHERE UPPER(nome_turma) = 'INI3');
INSERT INTO Turma (nome_turma, descricao)
SELECT 'INI4', 'Informática para Internet, quarto ano'
WHERE NOT EXISTS (SELECT 1 FROM Turma WHERE UPPER(nome_turma) = 'INI4');
INSERT INTO Turma (nome_turma, descricao)
SELECT 'PAV1', 'Produção de Áudio e Vídeo, primeiro ano'
WHERE NOT EXISTS (SELECT 1 FROM Turma WHERE UPPER(nome_turma) = 'PAV1');
INSERT INTO Turma (nome_turma, descricao)
SELECT 'PAV2', 'Produção de Áudio e Vídeo, segundo ano'
WHERE NOT EXISTS (SELECT 1 FROM Turma WHERE UPPER(nome_turma) = 'PAV2');
INSERT INTO Turma (nome_turma, descricao)
SELECT 'PAV3', 'Produção de Áudio e Vídeo, terceiro ano'
WHERE NOT EXISTS (SELECT 1 FROM Turma WHERE UPPER(nome_turma) = 'PAV3');
INSERT INTO Turma (nome_turma, descricao)
SELECT 'PAV4', 'Produção de Áudio e Vídeo, quarto ano'
WHERE NOT EXISTS (SELECT 1 FROM Turma WHERE UPPER(nome_turma) = 'PAV4');
INSERT INTO Turma (nome_turma, descricao)
SELECT 'DEG1', 'Design Gráfico, primeiro ano'
WHERE NOT EXISTS (SELECT 1 FROM Turma WHERE UPPER(nome_turma) = 'DEG1');
INSERT INTO Turma (nome_turma, descricao)
SELECT 'DEG2', 'Design Gráfico, segundo ano'
WHERE NOT EXISTS (SELECT 1 FROM Turma WHERE UPPER(nome_turma) = 'DEG2');

-- Garante todas as categorias exibidas na criacao de anuncios.
INSERT INTO Categoria (nome)
SELECT 'Doces e alimentos'
WHERE NOT EXISTS (SELECT 1 FROM Categoria WHERE LOWER(nome) = LOWER('Doces e alimentos'));
INSERT INTO Categoria (nome)
SELECT 'Materiais escolares'
WHERE NOT EXISTS (SELECT 1 FROM Categoria WHERE LOWER(nome) = LOWER('Materiais escolares'));
INSERT INTO Categoria (nome)
SELECT 'Artesanato'
WHERE NOT EXISTS (SELECT 1 FROM Categoria WHERE LOWER(nome) = LOWER('Artesanato'));
INSERT INTO Categoria (nome)
SELECT 'Eletrônicos'
WHERE NOT EXISTS (SELECT 1 FROM Categoria WHERE LOWER(nome) = LOWER('Eletrônicos'));
INSERT INTO Categoria (nome)
SELECT 'Roupas e acessórios'
WHERE NOT EXISTS (SELECT 1 FROM Categoria WHERE LOWER(nome) = LOWER('Roupas e acessórios'));
INSERT INTO Categoria (nome)
SELECT 'Livros e apostilas'
WHERE NOT EXISTS (SELECT 1 FROM Categoria WHERE LOWER(nome) = LOWER('Livros e apostilas'));

-- A entidade usa explicitamente o nome TokenRedefinicaoSenha.
RENAME TABLE token_redefinicao_senha TO TokenRedefinicaoSenha;

-- Corrige o erro HTTP 500: Unknown column 'n1_0.tipo'.
ALTER TABLE Notificacao
    ADD COLUMN tipo ENUM('mensagem','pedido','venda','pagamento','agendamento','anuncio','denuncia','sistema')
    NOT NULL DEFAULT 'sistema' AFTER mensagem;

-- Campos que ja existem nas entidades de conversa.
ALTER TABLE Conversa ADD COLUMN id_produto BIGINT NULL AFTER id_vendedor;
ALTER TABLE Conversa ADD CONSTRAINT fk_conversa_produto
    FOREIGN KEY (id_produto) REFERENCES Produto(id_produto) ON DELETE SET NULL;
ALTER TABLE Mensagem ADD COLUMN lida BOOLEAN NOT NULL DEFAULT FALSE AFTER data_envio;

-- Mantem as colunas de compatibilidade exigidas pelo back-end atual.
ALTER TABLE Agendamento ADD COLUMN id_compra BIGINT NULL AFTER id_compra_vendedor;
UPDATE Agendamento a
JOIN compra_vendedor cv ON cv.id_compra_vendedor = a.id_compra_vendedor
SET a.id_compra = cv.id_compra;
ALTER TABLE Agendamento MODIFY COLUMN id_compra BIGINT NOT NULL;
ALTER TABLE Agendamento ADD CONSTRAINT fk_agendamento_compra
    FOREIGN KEY (id_compra) REFERENCES Compra(id_compra) ON DELETE CASCADE;

ALTER TABLE Pagamento ADD COLUMN id_compra BIGINT NULL AFTER id_compra_vendedor;
UPDATE Pagamento p
JOIN compra_vendedor cv ON cv.id_compra_vendedor = p.id_compra_vendedor
SET p.id_compra = cv.id_compra;
ALTER TABLE Pagamento MODIFY COLUMN id_compra BIGINT NOT NULL;
ALTER TABLE Pagamento ADD CONSTRAINT fk_pagamento_compra
    FOREIGN KEY (id_compra) REFERENCES Compra(id_compra) ON DELETE CASCADE;

-- Impede que o mesmo item seja avaliado duas vezes.
ALTER TABLE Avaliacao ADD CONSTRAINT uk_avaliacao_item UNIQUE (id_item_compra);

-- Tabelas mapeadas pelas entidades legadas e ausentes no script recebido.
CREATE TABLE IF NOT EXISTS Cliente (
    id_usuario BIGINT PRIMARY KEY,
    CONSTRAINT fk_cliente_usuario FOREIGN KEY (id_usuario) REFERENCES Usuario(id_usuario) ON DELETE CASCADE
);
CREATE TABLE IF NOT EXISTS Vendedor (
    id_usuario BIGINT PRIMARY KEY,
    horario VARCHAR(255),
    status ENUM('online','offline','disponivel') NOT NULL DEFAULT 'offline',
    CONSTRAINT fk_vendedor_usuario FOREIGN KEY (id_usuario) REFERENCES Usuario(id_usuario) ON DELETE CASCADE
);
