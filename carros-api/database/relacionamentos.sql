DALTER TABLE test_drive
    ADD INDEX idx_test_drive_carro (carro_id),
    ADD INDEX idx_test_drive_usuario (usuario_id),
    ADD CONSTRAINT fk_test_drive_carro
        FOREIGN KEY (carro_id) REFERENCES carro(id),
    ADD CONSTRAINT fk_test_drive_usuario
        FOREIGN KEY (usuario_id) REFERENCES usuario(id);
