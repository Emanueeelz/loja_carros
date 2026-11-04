-- O trabalho anterior exigia carro_id NOT NULL. Agora outros modulos nao possuem carro_id.
-- Essa alteracao preserva os logs antigos e permite registros dos outros modulos.
ALTER TABLE log_operacao MODIFY COLUMN carro_id BIGINT NULL;
