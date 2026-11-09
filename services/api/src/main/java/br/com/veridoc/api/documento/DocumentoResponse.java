package br.com.veridoc.api.documento;

import java.time.Instant;
import java.util.UUID;

public record DocumentoResponse(
        UUID id,
        TipoDocumento tipo,
        StatusDocumento status,
        String nomeArquivo,
        String contentType,
        long tamanhoBytes,
        String sha256,
        Instant criadoEm,
        Instant atualizadoEm) {

    public static DocumentoResponse de(Documento documento) {
        return new DocumentoResponse(documento.getId(), documento.getTipo(), documento.getStatus(),
                documento.getNomeArquivo(), documento.getContentType(), documento.getTamanhoBytes(),
                documento.getSha256(), documento.getCriadoEm(), documento.getAtualizadoEm());
    }
}
