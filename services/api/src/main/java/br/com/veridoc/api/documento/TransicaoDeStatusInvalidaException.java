package br.com.veridoc.api.documento;

import br.com.veridoc.api.compartilhado.CategoriaErro;
import br.com.veridoc.api.compartilhado.ErroDeNegocioException;

public class TransicaoDeStatusInvalidaException extends ErroDeNegocioException {

    public TransicaoDeStatusInvalidaException(StatusDocumento atual, StatusDocumento destino) {
        super(CategoriaErro.CONFLITO, "transicao-de-status-invalida", "Transição de status inválida",
                "Documento em %s não pode ir para %s".formatted(atual, destino));
    }
}
