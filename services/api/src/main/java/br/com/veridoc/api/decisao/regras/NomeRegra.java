package br.com.veridoc.api.decisao.regras;

import br.com.veridoc.api.decisao.Motivo;
import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Optional;

/**
 * Nome lido diferente do declarado vai para revisão (pode ser abreviação ou nome de casada),
 * mas a comparação ignora acentos, caixa e espaços extras.
 */
@Component
public class NomeRegra implements RegraRisco {

    @Override
    public Optional<Motivo> avaliar(ContextoAvaliacao contexto) {
        var nome = contexto.dados().nome();
        if (nome == null || !nome.presente()) {
            return Optional.empty();
        }
        if (!normalizar(nome.valor()).equals(normalizar(contexto.nomeDeclarado()))) {
            return Optional.of(Motivo.revisao("NOME_DIVERGENTE", "Nome extraído é diferente do nome declarado"));
        }
        return Optional.empty();
    }

    static String normalizar(String nome) {
        if (nome == null) {
            return "";
        }
        return Normalizer.normalize(nome, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toUpperCase(Locale.ROOT)
                .trim()
                .replaceAll("\\s+", " ");
    }
}
