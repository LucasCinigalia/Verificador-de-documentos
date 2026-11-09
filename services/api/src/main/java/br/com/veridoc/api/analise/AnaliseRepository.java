package br.com.veridoc.api.analise;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/** Package-private: só o AnaliseService acessa a tabela analise. */
interface AnaliseRepository extends JpaRepository<Analise, UUID> {
}
