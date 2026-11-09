package br.com.veridoc.api.documento;

public class TransicaoDeStatusInvalidaException extends RuntimeException {

    public TransicaoDeStatusInvalidaException(StatusDocumento atual, StatusDocumento destino) {
        super("Documento em %s não pode ir para %s".formatted(atual, destino));
    }
}
