package br.org.edu.ifrn.carros.dto;

import jakarta.validation.constraints.*;

public record CadastroUsuarioRequest(
        @NotBlank @Size(max = 120)
        String nome,

        @NotBlank @Email @Size(max = 180)
        String email,

        @NotBlank @Size(min = 8, max = 72)
        String senha
) {
}
