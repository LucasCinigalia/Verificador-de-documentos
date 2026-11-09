package br.com.veridoc.api.analise;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

/** Resultado bruto da extração, guardado como veio (jsonb) para auditoria e reprocessamento. */
@Entity
@Table(name = "analise")
public class Analise {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "documento_id", nullable = false, unique = true)
    private UUID documentoId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private DadosExtraidos campos;

    @Column(name = "registrada_em", nullable = false)
    private Instant registradaEm;

    protected Analise() {
    }

    public Analise(UUID documentoId, DadosExtraidos campos, Instant registradaEm) {
        this.documentoId = documentoId;
        this.campos = campos;
        this.registradaEm = registradaEm;
    }

    public UUID getId() { return id; }
    public UUID getDocumentoId() { return documentoId; }
    public DadosExtraidos getCampos() { return campos; }
    public Instant getRegistradaEm() { return registradaEm; }
}
