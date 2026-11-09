package br.com.veridoc.api.decisao.regras;

import br.com.veridoc.api.analise.DadosExtraidos;
import br.com.veridoc.api.documento.TipoDocumento;

import java.time.LocalDate;

/** Tudo o que uma regra precisa para avaliar um documento, inclusive a data de "hoje". */
public record ContextoAvaliacao(
        TipoDocumento tipo,
        String cpfDeclarado,
        String nomeDeclarado,
        DadosExtraidos dados,
        LocalDate hoje) {
}
