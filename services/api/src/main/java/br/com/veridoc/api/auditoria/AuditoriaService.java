package br.com.veridoc.api.auditoria;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Service
public class AuditoriaService {

    private final EventoAuditoriaRepository repository;
    private final Clock clock;

    public AuditoriaService(EventoAuditoriaRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    /** Roda na transação de quem chamou: se a operação falhar, o evento também não fica. */
    @Transactional(propagation = Propagation.MANDATORY)
    public void registrar(UUID documentoId, TipoEventoAuditoria tipo, String detalhe) {
        repository.save(new EventoAuditoria(documentoId, tipo, detalhe, Instant.now(clock)));
    }
}
