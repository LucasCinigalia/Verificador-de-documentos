package br.com.veridoc.api.decisao.regras;

import br.com.veridoc.api.decisao.Motivo;
import br.com.veridoc.api.decisao.ResultadoDecisao;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Roda todas as regras e combina os resultados: vence o mais grave
 * (REPROVADO > REVISAO_MANUAL > APROVADO). Todos os motivos são guardados,
 * não só o que decidiu, para a decisão ser explicável.
 */
@Component
public class MotorDeRisco {

    static final Motivo SEM_RESSALVAS =
            new Motivo("SEM_RESSALVAS", ResultadoDecisao.APROVADO, "Todas as regras de risco foram atendidas");

    private final List<RegraRisco> regras;

    public MotorDeRisco(List<RegraRisco> regras) {
        this.regras = List.copyOf(regras);
    }

    public AvaliacaoRisco avaliar(ContextoAvaliacao contexto) {
        List<Motivo> motivos = regras.stream()
                .map(regra -> regra.avaliar(contexto))
                .flatMap(Optional::stream)
                .toList();

        if (motivos.isEmpty()) {
            return new AvaliacaoRisco(ResultadoDecisao.APROVADO, List.of(SEM_RESSALVAS));
        }
        ResultadoDecisao resultado = motivos.stream()
                .map(Motivo::resultado)
                .reduce(ResultadoDecisao.APROVADO, ResultadoDecisao::maisGrave);
        return new AvaliacaoRisco(resultado, motivos);
    }
}
