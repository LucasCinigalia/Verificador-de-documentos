package br.com.veridoc.api.documento;

import br.com.veridoc.api.compartilhado.RecursoNaoEncontradoException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DocumentoController.class)
class DocumentoControllerTest {

    private static final MockMultipartFile ARQUIVO =
            new MockMultipartFile("arquivo", "cnh-ficticia.png", "image/png", new byte[]{1, 2, 3});

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private DocumentoService service;

    @Test
    void enviarRetorna202ComLocationEStatusRecebido() throws Exception {
        var documento = documento();
        when(service.enviar(any())).thenReturn(documento);

        mvc.perform(multipart("/api/v1/documentos")
                        .file(ARQUIVO)
                        .param("tipo", "CNH")
                        .param("cpfDeclarado", "52998224725")
                        .param("nomeDeclarado", "Maria Ficticia da Silva"))
                .andExpect(status().isAccepted())
                .andExpect(header().string("Location", containsString("/api/v1/documentos/" + documento.getId())))
                .andExpect(jsonPath("$.status").value("RECEBIDO"))
                .andExpect(jsonPath("$.tipo").value("CNH"));
    }

    @Test
    void enviarComCamposInvalidosRetornaProblemDetailComErrosPorCampo() throws Exception {
        mvc.perform(multipart("/api/v1/documentos")
                        .file(ARQUIVO)
                        .param("tipo", "CNH")
                        .param("cpfDeclarado", "529.982.247-25"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("urn:veridoc:problema:dados-invalidos"))
                .andExpect(jsonPath("$.erros[*].campo", hasItem("cpfDeclarado")))
                .andExpect(jsonPath("$.erros[*].campo", hasItem("nomeDeclarado")));

        verifyNoInteractions(service);
    }

    /**
     * Com binding por construtor (record), se um valor não converte o objeto nem é criado:
     * só o erro de conversão volta, e a Bean Validation dos outros campos não roda.
     */
    @Test
    void tipoDesconhecidoRetornaErroDeConversao() throws Exception {
        mvc.perform(multipart("/api/v1/documentos")
                        .file(ARQUIVO)
                        .param("tipo", "PASSAPORTE")
                        .param("cpfDeclarado", "52998224725")
                        .param("nomeDeclarado", "Maria"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros[0].campo").value("tipo"))
                .andExpect(jsonPath("$.erros[0].mensagem").value("valor inválido"));

        verifyNoInteractions(service);
    }

    @Test
    void enviarSemArquivoRetorna400() throws Exception {
        mvc.perform(multipart("/api/v1/documentos")
                        .param("tipo", "CNH")
                        .param("cpfDeclarado", "52998224725")
                        .param("nomeDeclarado", "Maria"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros[*].campo", hasItem("arquivo")));
    }

    @Test
    void arquivoVazioRetornaProblemaDeArquivoInvalido() throws Exception {
        when(service.enviar(any())).thenThrow(new ArquivoInvalidoException("O arquivo enviado está vazio"));

        mvc.perform(multipart("/api/v1/documentos")
                        .file(new MockMultipartFile("arquivo", "vazio.png", "image/png", new byte[0]))
                        .param("tipo", "CNH")
                        .param("cpfDeclarado", "52998224725")
                        .param("nomeDeclarado", "Maria"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("urn:veridoc:problema:arquivo-invalido"));
    }

    @Test
    void buscarDocumentoInexistenteRetorna404() throws Exception {
        var id = UUID.randomUUID();
        when(service.buscar(id)).thenThrow(new RecursoNaoEncontradoException("Documento %s não encontrado".formatted(id)));

        mvc.perform(get("/api/v1/documentos/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value(containsString(id.toString())));
    }

    @Test
    void idMalFormadoRetorna400() throws Exception {
        mvc.perform(get("/api/v1/documentos/nao-e-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
    }

    private static Documento documento() {
        return new Documento(UUID.randomUUID(), TipoDocumento.CNH, "cnh-ficticia.png", "image/png", 3,
                "a".repeat(64), "/dados/x", "52998224725", "Maria Ficticia da Silva",
                Instant.parse("2026-11-09T12:00:00Z"));
    }
}
