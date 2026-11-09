package br.com.veridoc.api.analise;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

/** Um campo lido do documento pelo agente, com a confiança da extração (0 a 1). */
public record CampoExtraido(
        @Schema(example = "MARIA FICTICIA DA SILVA") String valor,
        @NotNull @DecimalMin("0.0") @DecimalMax("1.0") @Schema(example = "0.97") Double confianca) {

    public boolean presente() {
        return valor != null && !valor.isBlank();
    }
}
