package br.com.veridoc.api.decisao.regras;

import br.com.veridoc.api.decisao.Motivo;
import br.com.veridoc.api.documento.TipoDocumento.Campo;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.stream.Collectors;

/** Campo obrigatório que o agente não encontrou vai para revisão humana. */
@Component
public class CamposObrigatoriosRegra implements RegraRisco {

    @Override
    public Optional<Motivo> avaliar(ContextoAvaliacao contexto) {
        String ausentes = contexto.tipo().camposObrigatorios().stream()
                .filter(campo -> {
                    var extraido = contexto.dados().campo(campo);
                    return extraido == null || !extraido.presente();
                })
                .map(Campo::name)
                .sorted()
                .collect(Collectors.joining(", "));
        return ausentes.isEmpty()
                ? Optional.empty()
                : Optional.of(Motivo.revisao("CAMPO_AUSENTE", "Campos obrigatórios não extraídos: " + ausentes));
    }
}
