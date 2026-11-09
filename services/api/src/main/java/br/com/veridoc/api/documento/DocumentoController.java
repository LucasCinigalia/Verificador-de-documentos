package br.com.veridoc.api.documento;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.UUID;

@Tag(name = "Documentos", description = "Envio de documentos e consulta de status")
@RestController
@RequestMapping("/api/v1/documentos")
public class DocumentoController {

    private final DocumentoService service;

    public DocumentoController(DocumentoService service) {
        this.service = service;
    }

    @Operation(summary = "Envia um documento para verificação",
            description = "O processamento é assíncrono: a resposta traz o id e o status RECEBIDO.")
    @ApiResponse(responseCode = "202", description = "Documento recebido")
    @ApiResponse(responseCode = "400", description = "Dados inválidos")
    @ApiResponse(responseCode = "413", description = "Arquivo maior que o permitido")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentoResponse> enviar(@Valid @ModelAttribute EnvioDocumentoForm form) {
        var documento = service.enviar(form);
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(documento.getId()).toUri();
        return ResponseEntity.accepted().location(location).body(DocumentoResponse.de(documento));
    }

    @Operation(summary = "Consulta o status de um documento")
    @ApiResponse(responseCode = "200", description = "Documento encontrado")
    @ApiResponse(responseCode = "404", description = "Documento não encontrado")
    @GetMapping("/{id}")
    public DocumentoResponse buscar(@PathVariable UUID id) {
        return DocumentoResponse.de(service.buscar(id));
    }
}
