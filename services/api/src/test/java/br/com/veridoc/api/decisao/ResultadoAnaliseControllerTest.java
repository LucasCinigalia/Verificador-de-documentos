package br.com.veridoc.api.decisao;

import br.com.veridoc.api.documento.Documento;
import br.com.veridoc.api.documento.StatusDocumento;
import br.com.veridoc.api.documento.TransicaoDeStatusInvalidaException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasItem;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ResultadoAnaliseController.class)
class ResultadoAnaliseControllerTest {

    private static final String CORPO_VALIDO = """
            {"campos": {
              "nome": {"valor": "MARIA FICTICIA DA SILVA", "confianca": 0.98},
              "cpf": {"valor": "52998224725", "confianca": 0.99},
              "dataNascimento": {"valor": "1990-05-20", "confianca": 0.95},
              "dataValidade": {"valor": "2030-01-31", "confianca": 0.93}
            }}
            """;

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private DecisaoService service;

    @Test
    void registrarRetornaDecisaoComMotivos() throws Exception {
        var id = UUID.randomUUID();
        var motivo = new Motivo("SEM_RESSALVAS", ResultadoDecisao.APROVADO, "ok");
        when(service.registrarAnalise(eq(id), any()))
                .thenReturn(new Decisao(id, ResultadoDecisao.APROVADO, List.of(motivo), Instant.now()));

        mvc.perform(post("/api/v1/documentos/{id}/analise", id)
                        .contentType(MediaType.APPLICATION_JSON).content(CORPO_VALIDO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultado").value("APROVADO"))
                .andExpect(jsonPath("$.motivos[0].codigo").value("SEM_RESSALVAS"));
    }

    @Test
    void confiancaForaDoIntervaloRetorna400() throws Exception {
        String corpo = CORPO_VALIDO.replace("0.98", "1.5");

        mvc.perform(post("/api/v1/documentos/{id}/analise", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros[*].campo", hasItem("campos.nome.confianca")));
    }

    @Test
    void jsonMalformadoRetorna400ComProblemDetail() throws Exception {
        mvc.perform(post("/api/v1/documentos/{id}/analise", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"campos\": "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void documentoJaConcluidoRetorna409() throws Exception {
        when(service.registrarAnalise(any(), any()))
                .thenThrow(new TransicaoDeStatusInvalidaException(StatusDocumento.CONCLUIDO, StatusDocumento.CONCLUIDO));

        mvc.perform(post("/api/v1/documentos/{id}/analise", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON).content(CORPO_VALIDO))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.type").value("urn:veridoc:problema:transicao-de-status-invalida"));
    }

    @Test
    void alteracaoConcorrenteRetorna409() throws Exception {
        when(service.registrarAnalise(any(), any()))
                .thenThrow(new ObjectOptimisticLockingFailureException(Documento.class, UUID.randomUUID()));

        mvc.perform(post("/api/v1/documentos/{id}/analise", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON).content(CORPO_VALIDO))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.type").value("urn:veridoc:problema:conflito-de-concorrencia"));
    }
}
