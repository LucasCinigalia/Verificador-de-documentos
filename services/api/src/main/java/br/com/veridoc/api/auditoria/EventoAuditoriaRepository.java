package br.com.veridoc.api.auditoria;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EventoAuditoriaRepository extends JpaRepository<EventoAuditoria, UUID> {
}
