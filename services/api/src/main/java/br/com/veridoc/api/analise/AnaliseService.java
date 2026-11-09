package br.com.veridoc.api.analise;

import br.com.veridoc.api.auditoria.AuditoriaService;
import br.com.veridoc.api.auditoria.TipoEventoAuditoria;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

/**
 * Guarda o resultado bruto da extração. Não sabe nada de decisão: quem usa este serviço é o
 * domínio decisao, e a dependência só existe nesse sentido.
 */
@Service
public class AnaliseService {

    private final AnaliseRepository repository;
    private final AuditoriaService auditoria;
    private final Clock clock;

    AnaliseService(AnaliseRepository repository, AuditoriaService auditoria, Clock clock) {
        this.repository = repository;
        this.auditoria = auditoria;
        this.clock = clock;
    }

    /** Roda na transação de quem chamou, para análise e decisão serem gravadas juntas. */
    @Transactional(propagation = Propagation.MANDATORY)
    public void registrar(UUID documentoId, DadosExtraidos dados) {
        repository.save(new Analise(documentoId, dados, Instant.now(clock)));
        auditoria.registrar(documentoId, TipoEventoAuditoria.ANALISE_REGISTRADA, "Resultado da extração registrado");
    }
}
