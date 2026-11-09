package br.com.veridoc.api.decisao.regras;

import br.com.veridoc.api.decisao.Motivo;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * CPF lido do documento precisa ser válido e igual ao CPF declarado pela empresa.
 * Qualquer uma das falhas indica fraude ou documento de outra pessoa: reprova.
 */
@Component
public class CpfRegra implements RegraRisco {

    @Override
    public Optional<Motivo> avaliar(ContextoAvaliacao contexto) {
        var cpf = contexto.dados().cpf();
        if (cpf == null || !cpf.presente()) {
            return Optional.empty(); // ausência é tratada por CamposObrigatoriosRegra
        }
        if (!Cpf.valido(cpf.valor())) {
            return Optional.of(Motivo.reprovacao("CPF_INVALIDO", "CPF extraído tem dígitos verificadores inválidos"));
        }
        if (!Cpf.somenteDigitos(cpf.valor()).equals(Cpf.somenteDigitos(contexto.cpfDeclarado()))) {
            return Optional.of(Motivo.reprovacao("CPF_DIVERGENTE", "CPF extraído é diferente do CPF declarado"));
        }
        return Optional.empty();
    }
}
