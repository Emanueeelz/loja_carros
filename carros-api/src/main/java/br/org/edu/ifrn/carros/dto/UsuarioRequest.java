package br.org.edu.ifrn.carros.dto;
import jakarta.validation.constraints.*;

public record UsuarioRequest(
        @NotBlank @Size(max = 120) String nome,
        @NotBlank @Email @Size(max = 180) String email
) {}
