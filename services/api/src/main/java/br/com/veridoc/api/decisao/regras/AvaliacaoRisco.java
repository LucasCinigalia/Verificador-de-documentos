package br.com.veridoc.api.decisao.regras;

import br.com.veridoc.api.decisao.Motivo;
import br.com.veridoc.api.decisao.ResultadoDecisao;

import java.util.List;

public record AvaliacaoRisco(ResultadoDecisao resultado, List<Motivo> motivos) {
}
