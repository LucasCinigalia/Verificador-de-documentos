package br.com.veridoc.api.documento;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

/** Formulário multipart do envio: o arquivo e os dados que a empresa declara sobre o titular. */
public record EnvioDocumentoForm(
        @NotNull(message = "é obrigatório")
        @Schema(description = "Imagem ou PDF do documento (fictício)", type = "string", format = "binary")
        MultipartFile arquivo,

        @NotNull(message = "é obrigatório")
        @Schema(example = "CNH")
        TipoDocumento tipo,

        @NotBlank(message = "é obrigatório")
        @Pattern(regexp = "\\d{11}", message = "deve ter 11 dígitos, sem pontuação")
        @Schema(example = "52998224725")
        String cpfDeclarado,

        @NotBlank(message = "é obrigatório")
        @Size(max = 200, message = "deve ter no máximo 200 caracteres")
        @Schema(example = "Maria Ficticia da Silva")
        String nomeDeclarado) {
}
