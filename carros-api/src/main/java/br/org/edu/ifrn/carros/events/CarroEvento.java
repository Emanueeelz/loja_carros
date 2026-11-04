package br.org.edu.ifrn.carros.events;

import java.math.BigDecimal;

public record CarroEvento(
        String eventoId,
        String acao,
        Long carroId,
        String modelo,
        Integer ano,
        BigDecimal preco,
        String ocorridoEm
) {
}