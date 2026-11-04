package br.org.edu.ifrn.carros.dto;
import jakarta.validation.constraints.*;
import br.org.edu.ifrn.carros.model.TipoMovimentacao;
import java.math.BigDecimal;
public record MovimentacaoRequest(
        @NotNull TipoMovimentacao tipo,
        @NotBlank @Size(max = 240) String descricao,
        @NotNull @DecimalMin("0.01") @Digits(integer = 10, fraction = 2) BigDecimal valor
) {}
