package br.com.veridoc.api.analise;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record ResultadoAnaliseRequest(@NotNull(message = "é obrigatório") @Valid DadosExtraidos campos) {
}
