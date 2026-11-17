
package br.org.edu.ifrn.carros.dto;

import br.org.edu.ifrn.carros.model.SituacaoCarro;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record CarroRequest(
        @NotBlank @Size(max = 80)
        String marca,

        @NotBlank @Size(max = 120)
        String modelo,

        @NotNull @Min(1886) @Max(2100)
        Integer anoFabricacao,

        @NotNull @Min(1886) @Max(2100)
        Integer anoModelo,

        @NotBlank @Size(max = 60)
        String cor,

        @NotNull @PositiveOrZero
        Integer quilometragem,

        @NotBlank @Size(max = 40)
        String combustivel,

        @NotBlank @Size(max = 40)
        String transmissao,

        @NotNull @DecimalMin("0.01")
        @Digits(integer = 10, fraction = 2)
        BigDecimal preco,

        @NotBlank @Size(max = 2000)
        String descricao,

        @NotNull
        SituacaoCarro situacao
) {
}
