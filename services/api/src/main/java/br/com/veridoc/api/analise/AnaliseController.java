package br.com.veridoc.api.analise;

import br.com.veridoc.api.decisao.DecisaoResponse;
import br.com.veridoc.api.decisao.DecisaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * TEMPORÁRIO (ADR 0004): simula o agente de IA enquanto não há Kafka. Na Etapa 4 este
 * endpoint é substituído pelo consumidor de documento.analisado, que chama o mesmo serviço.
 */
@Tag(name = "Simulação do agente (temporário)",
        description = "Registra o resultado da extração manualmente; será substituído pelo Kafka na Etapa 4")
@RestController
@RequestMapping("/api/v1/documentos/{documentoId}/analise")
public class AnaliseController {

    private final DecisaoService decisaoService;

    public AnaliseController(DecisaoService decisaoService) {
        this.decisaoService = decisaoService;
    }

    @Operation(summary = "Registra o resultado da extração e aplica as regras de risco")
    @ApiResponse(responseCode = "200", description = "Decisão tomada")
    @ApiResponse(responseCode = "404", description = "Documento não encontrado")
    @ApiResponse(responseCode = "409", description = "Documento já analisado ou alterado em paralelo")
    @PostMapping
    public DecisaoResponse registrar(@PathVariable UUID documentoId, @Valid @RequestBody ResultadoAnaliseRequest request) {
        return DecisaoResponse.de(decisaoService.registrarAnalise(documentoId, request.campos()));
    }
}
