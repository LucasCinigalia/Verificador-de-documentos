package br.com.veridoc.api.decisao.regras;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/** Parâmetros das regras, configuráveis em application.yml (veridoc.regras.*). */
@Validated
@ConfigurationProperties("veridoc.regras")
public record RegrasRiscoProperties(
        @DecimalMin("0.0") @DecimalMax("1.0") double confiancaMinima,
        @Min(0) int idadeMinima) {
}
