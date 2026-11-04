package br.org.edu.ifrn.carros.dto;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
public record VendaRequest(
        @NotNull @Positive Long carroId,
        @NotNull @Positive Long usuarioId,
        @NotNull @DecimalMin("0.01") @Digits(integer = 10, fraction = 2) BigDecimal valor
) {}
