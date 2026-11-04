package br.org.edu.ifrn.carros.dto;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
public record TestDriveRequest(
        @NotNull @Positive Long carroId,
        @NotNull @Positive Long usuarioId,
        @NotNull LocalDateTime dataHora
) {}
