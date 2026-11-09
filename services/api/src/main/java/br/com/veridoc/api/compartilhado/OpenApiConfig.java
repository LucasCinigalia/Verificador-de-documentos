package br.com.veridoc.api.compartilhado;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI veridocOpenApi() {
        return new OpenAPI().info(new Info()
                .title("VeriDoc API")
                .version("v1")
                .description("Verificação de documentos: envio, status e decisão (APROVADO, "
                        + "REVISAO_MANUAL ou REPROVADO) com os motivos de cada decisão. "
                        + "Use apenas documentos fictícios."));
    }
}
