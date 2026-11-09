package br.com.veridoc.api.auditoria;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/** Registro imutável do que aconteceu com um documento. */
@Entity
@Table(name = "evento_auditoria")
public class EventoAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "documento_id", nullable = false)
    private UUID documentoId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoEventoAuditoria tipo;

    @Column(nullable = false)
    private String detalhe;

    @Column(name = "ocorrido_em", nullable = false)
    private Instant ocorridoEm;

    protected EventoAuditoria() {
    }

    public EventoAuditoria(UUID documentoId, TipoEventoAuditoria tipo, String detalhe, Instant ocorridoEm) {
        this.documentoId = documentoId;
        this.tipo = tipo;
        this.detalhe = detalhe;
        this.ocorridoEm = ocorridoEm;
    }

    public UUID getId() { return id; }
    public UUID getDocumentoId() { return documentoId; }
    public TipoEventoAuditoria getTipo() { return tipo; }
    public String getDetalhe() { return detalhe; }
    public Instant getOcorridoEm() { return ocorridoEm; }
}
