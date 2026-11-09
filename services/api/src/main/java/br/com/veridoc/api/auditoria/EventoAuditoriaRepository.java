package br.com.veridoc.api.auditoria;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/** Package-private: só o AuditoriaService acessa a tabela evento_auditoria. */
interface EventoAuditoriaRepository extends JpaRepository<EventoAuditoria, UUID> {
}
