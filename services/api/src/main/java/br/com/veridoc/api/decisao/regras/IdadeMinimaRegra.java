package br.com.veridoc.api.decisao.regras;

import br.com.veridoc.api.decisao.Motivo;
import org.springframework.stereotype.Component;

import java.time.Period;
import java.util.Optional;

/** Titular abaixo da idade mínima é reprovado; data de nascimento ilegível ou futura vai para revisão. */
@Component
public class IdadeMinimaRegra implements RegraRisco {

    private final int idadeMinima;

    public IdadeMinimaRegra(RegrasRiscoProperties properties) {
        this.idadeMinima = properties.idadeMinima();
    }

    @Override
    public Optional<Motivo> avaliar(ContextoAvaliacao contexto) {
        var campo = contexto.dados().dataNascimento();
        if (Datas.malFormatada(campo)) {
            return Optional.of(Motivo.revisao("DATA_INVALIDA", "Data de nascimento em formato inválido"));
        }
        var nascimento = Datas.ler(campo);
        if (nascimento.isEmpty()) {
            return Optional.empty();
        }
        if (nascimento.get().isAfter(contexto.hoje())) {
            return Optional.of(Motivo.revisao("DATA_INVALIDA", "Data de nascimento no futuro"));
        }
        int idade = Period.between(nascimento.get(), contexto.hoje()).getYears();
        return idade < idadeMinima
                ? Optional.of(Motivo.reprovacao("MENOR_DE_IDADE",
                        "Titular tem %d anos; mínimo exigido é %d".formatted(idade, idadeMinima)))
                : Optional.empty();
    }
}
