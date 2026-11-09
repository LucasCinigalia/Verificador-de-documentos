package br.com.veridoc.api.analise;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AnaliseRepository extends JpaRepository<Analise, UUID> {
}
