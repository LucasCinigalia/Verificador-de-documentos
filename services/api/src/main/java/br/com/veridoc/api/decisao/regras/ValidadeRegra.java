package br.com.veridoc.api.decisao.regras;

import br.com.veridoc.api.decisao.Motivo;
import org.springframework.stereotype.Component;

import java.util.Optional;

/** Documento vencido é reprovado; data de validade ilegível vai para revisão. */
@Component
public class ValidadeRegra implements RegraRisco {

    @Override
    public Optional<Motivo> avaliar(ContextoAvaliacao contexto) {
        var campo = contexto.dados().dataValidade();
        if (Datas.malFormatada(campo)) {
            return Optional.of(Motivo.revisao("DATA_INVALIDA", "Data de validade em formato inválido"));
        }
        return Datas.ler(campo)
                .filter(validade -> validade.isBefore(contexto.hoje()))
                .map(validade -> Motivo.reprovacao("DOCUMENTO_VENCIDO", "Documento venceu em " + validade));
    }
}
