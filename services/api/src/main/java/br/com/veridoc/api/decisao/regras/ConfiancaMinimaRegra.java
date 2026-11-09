package br.com.veridoc.api.decisao.regras;

import br.com.veridoc.api.decisao.Motivo;
import br.com.veridoc.api.documento.TipoDocumento.Campo;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.stream.Collectors;

/** Extração com baixa confiança em campo obrigatório não pode ser aprovada automaticamente. */
@Component
public class ConfiancaMinimaRegra implements RegraRisco {

    private final double confiancaMinima;

    public ConfiancaMinimaRegra(RegrasRiscoProperties properties) {
        this.confiancaMinima = properties.confiancaMinima();
    }

    @Override
    public Optional<Motivo> avaliar(ContextoAvaliacao contexto) {
        String baixos = contexto.tipo().camposObrigatorios().stream()
                .filter(campo -> {
                    var extraido = contexto.dados().campo(campo);
                    return extraido != null && extraido.presente()
                            && (extraido.confianca() == null || extraido.confianca() < confiancaMinima);
                })
                .map(Campo::name)
                .sorted()
                .collect(Collectors.joining(", "));
        return baixos.isEmpty()
                ? Optional.empty()
                : Optional.of(Motivo.revisao("BAIXA_CONFIANCA",
                        "Confiança abaixo de %.2f nos campos: %s".formatted(confiancaMinima, baixos)));
    }
}
