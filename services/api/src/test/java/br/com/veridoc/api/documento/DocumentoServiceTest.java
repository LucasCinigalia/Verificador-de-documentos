package br.com.veridoc.api.documento;

import br.com.veridoc.api.auditoria.AuditoriaService;
import br.com.veridoc.api.compartilhado.RecursoNaoEncontradoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Este teste fica no pacote documento porque é o único lugar que enxerga o DocumentoRepository. */
@ExtendWith(MockitoExtension.class)
class DocumentoServiceTest {

    private static final Instant AGORA = Instant.parse("2026-11-09T15:00:00Z");

    @Mock private DocumentoRepository repository;
    @Mock private ArmazenamentoArquivos armazenamento;
    @Mock private AuditoriaService auditoria;

    private DocumentoService service;

    @BeforeEach
    void setUp() {
        service = new DocumentoService(repository, armazenamento, auditoria, Clock.fixed(AGORA, ZoneOffset.UTC));
    }

    @Test
    void concluirAnaliseTrocaStatusGravaNaHoraEDevolveDadosDeclarados() {
        var documento = documentoRecebido();
        when(repository.findById(documento.getId())).thenReturn(Optional.of(documento));

        var declarados = service.concluirAnalise(documento.getId());

        assertThat(documento.getStatus()).isEqualTo(StatusDocumento.CONCLUIDO);
        assertThat(declarados).isEqualTo(new DadosDeclarados(documento.getId(), TipoDocumento.CNH,
                "52998224725", "Maria Ficticia da Silva"));
        verify(repository).saveAndFlush(documento);
    }

    @Test
    void concluirAnaliseDeDocumentoJaConcluidoNaoGravaNada() {
        var documento = documentoRecebido();
        documento.alterarStatus(StatusDocumento.CONCLUIDO, AGORA);
        when(repository.findById(documento.getId())).thenReturn(Optional.of(documento));

        assertThatThrownBy(() -> service.concluirAnalise(documento.getId()))
                .isInstanceOf(TransicaoDeStatusInvalidaException.class);
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void documentoInexistenteGeraNaoEncontrado() {
        var id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.concluirAnalise(id))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessageContaining(id.toString());
    }

    private static Documento documentoRecebido() {
        return new Documento(UUID.randomUUID(), TipoDocumento.CNH, "cnh.png", "image/png", 3,
                "a".repeat(64), "/dados/x", "52998224725", "Maria Ficticia da Silva", AGORA);
    }
}
