package br.com.veridoc.api.decisao;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Decisões", description = "Resultado da verificação e seus motivos")
@RestController
@RequestMapping("/api/v1/documentos/{documentoId}/decisao")
public class DecisaoController {

    private final DecisaoService service;

    public DecisaoController(DecisaoService service) {
        this.service = service;
    }

    @Operation(summary = "Consulta a decisão de um documento")
    @ApiResponse(responseCode = "200", description = "Decisão encontrada")
    @ApiResponse(responseCode = "404", description = "Documento não existe ou ainda não foi decidido")
    @GetMapping
    public DecisaoResponse buscar(@PathVariable UUID documentoId) {
        return DecisaoResponse.de(service.buscarPorDocumento(documentoId));
    }
}
