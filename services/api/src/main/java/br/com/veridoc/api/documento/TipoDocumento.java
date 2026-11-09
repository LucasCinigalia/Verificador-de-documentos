package br.com.veridoc.api.documento;

import java.util.Set;

/** Tipos de documento aceitos e os campos que cada um precisa ter. */
public enum TipoDocumento {
    RG(Set.of(Campo.NOME, Campo.CPF, Campo.DATA_NASCIMENTO)),
    CNH(Set.of(Campo.NOME, Campo.CPF, Campo.DATA_NASCIMENTO, Campo.DATA_VALIDADE));

    private final Set<Campo> camposObrigatorios;

    TipoDocumento(Set<Campo> camposObrigatorios) {
        this.camposObrigatorios = camposObrigatorios;
    }

    public Set<Campo> camposObrigatorios() {
        return camposObrigatorios;
    }

    public enum Campo { NOME, CPF, DATA_NASCIMENTO, DATA_VALIDADE }
}
