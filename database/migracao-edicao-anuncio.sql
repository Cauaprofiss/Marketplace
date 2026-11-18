-- Atualiza anuncios existentes para guardar horario de retirada e finalidade.
USE campusmarketplace;

SET @sql = IF(
  EXISTS(SELECT 1 FROM information_schema.columns
         WHERE table_schema=DATABASE() AND table_name='Produto'
           AND column_name='disponibilidade_retirada'),
  'SELECT 1',
  'ALTER TABLE Produto ADD COLUMN disponibilidade_retirada ENUM(''intervalo_manha'',''intervalo_tarde'') DEFAULT ''intervalo_manha'''
);
PREPARE migration FROM @sql; EXECUTE migration; DEALLOCATE PREPARE migration;

SET @sql = IF(
  EXISTS(SELECT 1 FROM information_schema.columns
         WHERE table_schema=DATABASE() AND table_name='Produto'
           AND column_name='finalidade'),
  'SELECT 1',
  'ALTER TABLE Produto ADD COLUMN finalidade VARCHAR(20) NOT NULL DEFAULT ''formatura'''
);
PREPARE migration FROM @sql; EXECUTE migration; DEALLOCATE PREPARE migration;

UPDATE Produto SET finalidade='formatura' WHERE finalidade IS NULL OR finalidade='';
