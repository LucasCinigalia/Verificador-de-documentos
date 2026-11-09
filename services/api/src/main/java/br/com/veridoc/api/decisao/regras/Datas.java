package br.com.veridoc.api.decisao.regras;

import br.com.veridoc.api.analise.CampoExtraido;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Optional;

final class Datas {

    private Datas() {
    }

    /** Converte um campo ISO (yyyy-MM-dd) em data; vazio se ausente ou mal formatado. */
    static Optional<LocalDate> ler(CampoExtraido campo) {
        if (campo == null || !campo.presente()) {
            return Optional.empty();
        }
        try {
            return Optional.of(LocalDate.parse(campo.valor().trim()));
        } catch (DateTimeParseException e) {
            return Optional.empty();
        }
    }

    static boolean malFormatada(CampoExtraido campo) {
        return campo != null && campo.presente() && ler(campo).isEmpty();
    }
}
