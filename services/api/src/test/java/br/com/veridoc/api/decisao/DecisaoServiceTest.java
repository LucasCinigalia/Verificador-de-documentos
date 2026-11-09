package br.com.veridoc.api.decisao;

import br.com.veridoc.api.analise.AnaliseRepository;
import br.com.veridoc.api.analise.CampoExtraido;
import br.com.veridoc.api.analise.DadosExtraidos;
import br.com.veridoc.api.auditoria.AuditoriaService;
import br.com.veridoc.api.auditoria.TipoEventoAuditoria;
import br.com.veridoc.api.compartilhado.RecursoNaoEncontradoException;
import br.com.veridoc.api.decisao.regras.CpfRegra;
import br.com.veridoc.api.decisao.regras.MotorDeRisco;
import br.com.veridoc.api.documento.Documento;
import br.com.veridoc.api.documento.DocumentoRepository;
import br.com.veridoc.api.documento.StatusDocumento;
import br.com.veridoc.api.documento.TipoDocumento;
import br.com.veridoc.api.documento.TransicaoDeStatusInvalidaException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DecisaoServiceTest {

    private static final Instant AGORA = Instant.parse("2026-11-09T15:00:00Z");

    @Mock private DocumentoRepository documentos;
    @Mock private AnaliseRepository analises;
    @Mock private DecisaoRepository decisoes;
    @Mock private AuditoriaService auditoria;

    private DecisaoService service;

    @BeforeEach
    void setUp() {
        var motor = new MotorDeRisco(List.of(new CpfRegra()));
        service = new DecisaoService(documentos, analises, decisoes, motor, auditoria,
                Clock.fixed(AGORA, ZoneOffset.UTC));
    }

    @Test
    void registraAnaliseDecideEConcluiODocumento() {
        var documento = documentoRecebido();
        when(documentos.findById(documento.getId())).thenReturn(Optional.of(documento));
        when(decisoes.save(any())).thenAnswer(invocacao -> invocacao.getArgument(0));

        var decisao = service.registrarAnalise(documento.getId(), dadosComCpf("12345678909"));

        assertThat(decisao.getResultado()).isEqualTo(ResultadoDecisao.REPROVADO);
        assertThat(decisao.getMotivos()).extracting(Motivo::codigo).containsExactly("CPF_DIVERGENTE");
        assertThat(documento.getStatus()).isEqualTo(StatusDocumento.CONCLUIDO);
        verify(analises).save(any());
        verify(documentos).saveAndFlush(documento);
        verify(auditoria).registrar(eq(documento.getId()), eq(TipoEventoAuditoria.DECISAO_TOMADA), anyString());
    }

    @Test
    void naoGravaNadaQuandoDocumentoJaFoiConcluido() {
        var documento = documentoRecebido();
        documento.alterarStatus(StatusDocumento.CONCLUIDO, AGORA);
        when(documentos.findById(documento.getId())).thenReturn(Optional.of(documento));

        assertThatThrownBy(() -> service.registrarAnalise(documento.getId(), dadosComCpf("52998224725")))
                .isInstanceOf(TransicaoDeStatusInvalidaException.class);
        verify(analises, never()).save(any());
        verify(decisoes, never()).save(any());
    }

    @Test
    void documentoInexistenteGeraNaoEncontrado() {
        var id = UUID.randomUUID();
        when(documentos.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.registrarAnalise(id, dadosComCpf("52998224725")))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    void buscarDecisaoDeDocumentoSemDecisaoInformaOStatus() {
        var documento = documentoRecebido();
        when(documentos.findById(documento.getId())).thenReturn(Optional.of(documento));
        when(decisoes.findByDocumentoId(documento.getId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorDocumento(documento.getId()))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessageContaining("RECEBIDO");
    }

    private static Documento documentoRecebido() {
        return new Documento(UUID.randomUUID(), TipoDocumento.CNH, "cnh.png", "image/png", 3,
                "a".repeat(64), "/dados/x", "52998224725", "Maria Ficticia da Silva", AGORA);
    }

    private static DadosExtraidos dadosComCpf(String cpf) {
        return new DadosExtraidos(null, new CampoExtraido(cpf, 0.99), null, null);
    }
}
