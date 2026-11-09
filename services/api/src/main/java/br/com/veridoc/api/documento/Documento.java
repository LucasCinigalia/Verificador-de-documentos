package br.com.veridoc.api.documento;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "documento")
public class Documento {

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoDocumento tipo;

    @Column(name = "nome_arquivo", nullable = false)
    private String nomeArquivo;

    @Column(name = "content_type", nullable = false)
    private String contentType;

    @Column(name = "tamanho_bytes", nullable = false)
    private long tamanhoBytes;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(nullable = false, length = 64)
    private String sha256;

    @Column(name = "caminho_arquivo", nullable = false)
    private String caminhoArquivo;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "cpf_declarado", nullable = false, length = 11)
    private String cpfDeclarado;

    @Column(name = "nome_declarado", nullable = false)
    private String nomeDeclarado;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusDocumento status;

    /**
     * Lock otimista: o Hibernate inclui "where versao = ?" no UPDATE. Se outra transação mudou
     * o documento antes, nenhuma linha é atualizada e a escrita falha em vez de sobrescrever.
     * Wrapper (Long) para o Spring Data saber que versao == null significa entidade nova.
     */
    @Version
    @Column(nullable = false)
    private Long versao;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private Instant atualizadoEm;

    protected Documento() {
        // exigido pelo JPA
    }

    public Documento(UUID id, TipoDocumento tipo, String nomeArquivo, String contentType, long tamanhoBytes,
                     String sha256, String caminhoArquivo, String cpfDeclarado, String nomeDeclarado,
                     Instant agora) {
        this.id = id;
        this.tipo = tipo;
        this.nomeArquivo = nomeArquivo;
        this.contentType = contentType;
        this.tamanhoBytes = tamanhoBytes;
        this.sha256 = sha256;
        this.caminhoArquivo = caminhoArquivo;
        this.cpfDeclarado = cpfDeclarado;
        this.nomeDeclarado = nomeDeclarado;
        this.status = StatusDocumento.RECEBIDO;
        this.criadoEm = agora;
        this.atualizadoEm = agora;
    }

    public void alterarStatus(StatusDocumento destino, Instant agora) {
        if (!status.podeIrPara(destino)) {
            throw new TransicaoDeStatusInvalidaException(status, destino);
        }
        this.status = destino;
        this.atualizadoEm = agora;
    }

    public UUID getId() { return id; }
    public TipoDocumento getTipo() { return tipo; }
    public String getNomeArquivo() { return nomeArquivo; }
    public String getContentType() { return contentType; }
    public long getTamanhoBytes() { return tamanhoBytes; }
    public String getSha256() { return sha256; }
    public String getCaminhoArquivo() { return caminhoArquivo; }
    public String getCpfDeclarado() { return cpfDeclarado; }
    public String getNomeDeclarado() { return nomeDeclarado; }
    public StatusDocumento getStatus() { return status; }
    public Long getVersao() { return versao; }
    public Instant getCriadoEm() { return criadoEm; }
    public Instant getAtualizadoEm() { return atualizadoEm; }
}
