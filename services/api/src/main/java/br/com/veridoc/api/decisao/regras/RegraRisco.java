package br.com.veridoc.api.decisao.regras;

import br.com.veridoc.api.decisao.Motivo;

import java.util.Optional;

/** Uma regra de risco independente. Retorna um motivo quando a regra é violada. */
public interface RegraRisco {

    Optional<Motivo> avaliar(ContextoAvaliacao contexto);
}
