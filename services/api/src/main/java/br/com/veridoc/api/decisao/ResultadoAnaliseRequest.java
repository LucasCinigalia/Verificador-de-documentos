package br.com.veridoc.api.decisao;

import br.com.veridoc.api.analise.DadosExtraidos;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record ResultadoAnaliseRequest(@NotNull(message = "é obrigatório") @Valid DadosExtraidos campos) {
}
