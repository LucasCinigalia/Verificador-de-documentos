package br.com.veridoc.api.documento;

import br.com.veridoc.api.auditoria.AuditoriaService;
import br.com.veridoc.api.auditoria.TipoEventoAuditoria;
import br.com.veridoc.api.compartilhado.RecursoNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class DocumentoService {

    private final DocumentoRepository repository;
    private final ArmazenamentoArquivos armazenamento;
    private final AuditoriaService auditoria;
    private final Clock clock;

    public DocumentoService(DocumentoRepository repository, ArmazenamentoArquivos armazenamento,
                            AuditoriaService auditoria, Clock clock) {
        this.repository = repository;
        this.armazenamento = armazenamento;
        this.auditoria = auditoria;
        this.clock = clock;
    }

    @Transactional
    public Documento enviar(EnvioDocumentoForm form) {
        var arquivo = form.arquivo();
        if (arquivo.isEmpty()) {
            throw new ArquivoInvalidoException("O arquivo enviado está vazio");
        }

        UUID id = UUID.randomUUID();
        ArmazenamentoArquivos.ArquivoArmazenado armazenado;
        try (var conteudo = arquivo.getInputStream()) {
            armazenado = armazenamento.salvar(id, conteudo);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        var documento = new Documento(id, form.tipo(),
                nomeSeguro(arquivo.getOriginalFilename()),
                Optional.ofNullable(arquivo.getContentType()).orElse("application/octet-stream"),
                armazenado.tamanhoBytes(), armazenado.sha256(), armazenado.caminho(),
                form.cpfDeclarado(), form.nomeDeclarado().trim(), Instant.now(clock));
        repository.save(documento);
        auditoria.registrar(id, TipoEventoAuditoria.DOCUMENTO_RECEBIDO,
                "Documento %s recebido (%d bytes)".formatted(form.tipo(), armazenado.tamanhoBytes()));
        return documento;
    }

    @Transactional(readOnly = true)
    public Documento buscar(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Documento %s não encontrado".formatted(id)));
    }

    /** Guarda só o nome do arquivo (sem diretórios) e limita o tamanho à coluna. */
    private static String nomeSeguro(String original) {
        String nome = StringUtils.getFilename(StringUtils.cleanPath(Optional.ofNullable(original).orElse("")));
        if (!StringUtils.hasText(nome)) {
            return "arquivo";
        }
        return nome.length() > 255 ? nome.substring(nome.length() - 255) : nome;
    }
}
