package br.com.veridoc.api.decisao;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record DecisaoResponse(UUID documentoId, ResultadoDecisao resultado, List<Motivo> motivos, Instant decididaEm) {

    public static DecisaoResponse de(Decisao decisao) {
        return new DecisaoResponse(decisao.getDocumentoId(), decisao.getResultado(),
                decisao.getMotivos(), decisao.getDecididaEm());
    }
}
