package br.com.veridoc.api.documento;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.UUID;

/**
 * Grava o arquivo em disco com o id do documento como nome (nunca o nome enviado pelo
 * cliente, para evitar path traversal) e calcula o SHA-256 enquanto grava.
 */
@Component
public class ArmazenamentoLocal implements ArmazenamentoArquivos {

    private final Path diretorio;

    public ArmazenamentoLocal(@Value("${veridoc.armazenamento.diretorio}") Path diretorio) {
        this.diretorio = diretorio.toAbsolutePath().normalize();
    }

    @Override
    public ArquivoArmazenado salvar(UUID documentoId, InputStream conteudo) {
        Path destino = diretorio.resolve(documentoId.toString());
        try {
            Files.createDirectories(diretorio);
            var digest = MessageDigest.getInstance("SHA-256");
            long tamanho;
            try (var entrada = new DigestInputStream(conteudo, digest)) {
                tamanho = Files.copy(entrada, destino);
            }
            return new ArquivoArmazenado(destino.toString(), tamanho, HexFormat.of().formatHex(digest.digest()));
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao gravar arquivo do documento " + documentoId, e);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponível na JVM", e);
        }
    }
}
