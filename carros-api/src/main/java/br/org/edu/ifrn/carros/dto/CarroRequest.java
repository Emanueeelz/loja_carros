package br.org.edu.ifrn.carros.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record CarroRequest(

        @NotBlank
        @Size(max = 120)
        String modelo,

        @NotNull
        @Min(1886)
        @Max(2100)
        Integer ano,

        @NotNull
        @DecimalMin("0.00")
        @Digits(integer = 10, fraction = 2)
        BigDecimal preco

) {
}