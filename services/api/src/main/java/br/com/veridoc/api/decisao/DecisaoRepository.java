package br.com.veridoc.api.decisao;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DecisaoRepository extends JpaRepository<Decisao, UUID> {

    Optional<Decisao> findByDocumentoId(UUID documentoId);
}
