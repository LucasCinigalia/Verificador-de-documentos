package br.com.veridoc.api.documento;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/** Package-private: só o DocumentoService acessa a tabela documento. */
interface DocumentoRepository extends JpaRepository<Documento, UUID> {
}
