package br.com.veridoc.api.decisao;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "decisao")
public class Decisao {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "documento_id", nullable = false, unique = true)
    private UUID documentoId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ResultadoDecisao resultado;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private List<Motivo> motivos;

    @Column(name = "decidida_em", nullable = false)
    private Instant decididaEm;

    protected Decisao() {
    }

    public Decisao(UUID documentoId, ResultadoDecisao resultado, List<Motivo> motivos, Instant decididaEm) {
        this.documentoId = documentoId;
        this.resultado = resultado;
        this.motivos = List.copyOf(motivos);
        this.decididaEm = decididaEm;
    }

    public UUID getId() { return id; }
    public UUID getDocumentoId() { return documentoId; }
    public ResultadoDecisao getResultado() { return resultado; }
    public List<Motivo> getMotivos() { return motivos; }
    public Instant getDecididaEm() { return decididaEm; }
}
