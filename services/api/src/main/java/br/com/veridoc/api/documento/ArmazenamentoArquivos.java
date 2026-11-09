package br.com.veridoc.api.documento;

import java.io.InputStream;
import java.util.UUID;

/**
 * Onde os arquivos ficam guardados. Hoje é o disco local; na Etapa 4/5 vira MinIO (S3),
 * sem mudar quem usa esta interface.
 */
public interface ArmazenamentoArquivos {

    ArquivoArmazenado salvar(UUID documentoId, InputStream conteudo);

    record ArquivoArmazenado(String caminho, long tamanhoBytes, String sha256) {
    }
}
