package br.com.veridoc.api.decisao;

import br.com.veridoc.api.analise.AnaliseService;
import br.com.veridoc.api.analise.CampoExtraido;
import br.com.veridoc.api.analise.DadosExtraidos;
import br.com.veridoc.api.auditoria.AuditoriaService;
import br.com.veridoc.api.auditoria.TipoEventoAuditoria;
import br.com.veridoc.api.decisao.regras.CpfRegra;
import br.com.veridoc.api.decisao.regras.MotorDeRisco;
import br.com.veridoc.api.compartilhado.RecursoNaoEncontradoException;
import br.com.veridoc.api.documento.DadosDeclarados;
import br.com.veridoc.api.documento.Documento;
import br.com.veridoc.api.documento.DocumentoService;
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
    private static final UUID DOCUMENTO_ID = UUID.randomUUID();

    @Mock private DocumentoService documentoService;
    @Mock private AnaliseService analiseService;
    @Mock private DecisaoRepository decisoes;
    @Mock private AuditoriaService auditoria;

    private DecisaoService service;

    @BeforeEach
    void setUp() {
        var motor = new MotorDeRisco(List.of(new CpfRegra()));
        service = new DecisaoService(documentoService, analiseService, decisoes, motor, auditoria,
                Clock.fixed(AGORA, ZoneOffset.UTC));
    }

    @Test
    void concluiODocumentoRegistraAnaliseEDecide() {
        when(documentoService.concluirAnalise(DOCUMENTO_ID)).thenReturn(declarados());
        when(decisoes.save(any())).thenAnswer(invocacao -> invocacao.getArgument(0));

        var decisao = service.registrarAnalise(DOCUMENTO_ID, dadosComCpf("12345678909"));

        assertThat(decisao.getResultado()).isEqualTo(ResultadoDecisao.REPROVADO);
        assertThat(decisao.getMotivos()).extracting(Motivo::codigo).containsExactly("CPF_DIVERGENTE");
        verify(analiseService).registrar(eq(DOCUMENTO_ID), any());
        verify(auditoria).registrar(eq(DOCUMENTO_ID), eq(TipoEventoAuditoria.DECISAO_TOMADA), anyString());
    }

    @Test
    void naoGravaNadaQuandoODocumentoNaoPodeSerConcluido() {
        when(documentoService.concluirAnalise(DOCUMENTO_ID))
                .thenThrow(new TransicaoDeStatusInvalidaException(StatusDocumento.CONCLUIDO, StatusDocumento.CONCLUIDO));

        assertThatThrownBy(() -> service.registrarAnalise(DOCUMENTO_ID, dadosComCpf("52998224725")))
                .isInstanceOf(TransicaoDeStatusInvalidaException.class);
        verify(analiseService, never()).registrar(any(), any());
        verify(decisoes, never()).save(any());
    }

    @Test
    void buscarDecisaoDeDocumentoSemDecisaoInformaOStatus() {
        var documento = new Documento(DOCUMENTO_ID, TipoDocumento.CNH, "cnh.png", "image/png", 3,
                "a".repeat(64), "/dados/x", "52998224725", "Maria Ficticia da Silva", AGORA);
        when(documentoService.buscar(DOCUMENTO_ID)).thenReturn(documento);
        when(decisoes.findByDocumentoId(DOCUMENTO_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorDocumento(DOCUMENTO_ID))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessageContaining("RECEBIDO");
    }

    private static DadosDeclarados declarados() {
        return new DadosDeclarados(DOCUMENTO_ID, TipoDocumento.CNH, "52998224725", "Maria Ficticia da Silva");
    }

    private static DadosExtraidos dadosComCpf(String cpf) {
        return new DadosExtraidos(null, new CampoExtraido(cpf, 0.99), null, null);
    }
}
