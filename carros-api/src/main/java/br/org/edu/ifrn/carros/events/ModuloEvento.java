package br.org.edu.ifrn.carros.events;

public record ModuloEvento(
        String eventoId,
        String modulo,
        String acao,
        Long entidadeId,
        String descricao,
        String ocorridoEm
) {}
