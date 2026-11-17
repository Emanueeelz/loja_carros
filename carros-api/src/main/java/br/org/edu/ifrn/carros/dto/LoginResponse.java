package br.org.edu.ifrn.carros.dto;

public record LoginResponse(
        String token,
        String tipo
) {
}
