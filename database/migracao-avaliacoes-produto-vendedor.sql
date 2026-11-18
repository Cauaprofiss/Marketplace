-- MySQL: executar no banco existente antes de testar as duas avaliações.
-- Preserva registros legados, que representam avaliações do produto.
SET @sql = IF(EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='Avaliacao' AND column_name='tipo'),
 'SELECT 1', 'ALTER TABLE Avaliacao ADD COLUMN tipo VARCHAR(20) NULL');
PREPARE migration FROM @sql; EXECUTE migration; DEALLOCATE PREPARE migration;
SET @sql = IF(EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='Avaliacao' AND column_name='id_compra_vendedor'),
 'SELECT 1', 'ALTER TABLE Avaliacao ADD COLUMN id_compra_vendedor BIGINT NULL');
PREPARE migration FROM @sql; EXECUTE migration; DEALLOCATE PREPARE migration;
UPDATE Avaliacao SET tipo='produto' WHERE tipo IS NULL;
ALTER TABLE Avaliacao MODIFY COLUMN comentario TEXT;
SET @sql = IF(EXISTS(SELECT 1 FROM information_schema.statistics WHERE table_schema=DATABASE() AND table_name='Avaliacao' AND index_name='uk_avaliacao_item_tipo'),
 'SELECT 1', 'ALTER TABLE Avaliacao ADD CONSTRAINT uk_avaliacao_item_tipo UNIQUE(id_item_compra,tipo)');
PREPARE migration FROM @sql; EXECUTE migration; DEALLOCATE PREPARE migration;
-- O índice composto acima continua atendendo à chave estrangeira do item.
SET @sql = IF(EXISTS(SELECT 1 FROM information_schema.statistics WHERE table_schema=DATABASE() AND table_name='Avaliacao' AND index_name='uk_avaliacao_item'),
 'ALTER TABLE Avaliacao DROP INDEX uk_avaliacao_item', 'SELECT 1');
PREPARE migration FROM @sql; EXECUTE migration; DEALLOCATE PREPARE migration;
