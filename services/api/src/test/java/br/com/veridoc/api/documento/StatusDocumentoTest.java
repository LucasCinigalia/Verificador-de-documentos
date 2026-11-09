package br.com.veridoc.api.documento;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StatusDocumentoTest {

    @Test
    void recebidoPodeIrParaEmAnaliseOuConcluido() {
        assertThat(StatusDocumento.RECEBIDO.podeIrPara(StatusDocumento.EM_ANALISE)).isTrue();
        assertThat(StatusDocumento.RECEBIDO.podeIrPara(StatusDocumento.CONCLUIDO)).isTrue();
    }

    @Test
    void concluidoEEstadoFinal() {
        for (var destino : StatusDocumento.values()) {
            assertThat(StatusDocumento.CONCLUIDO.podeIrPara(destino)).isFalse();
        }
    }

    @Test
    void naoVoltaParaRecebido() {
        assertThat(StatusDocumento.EM_ANALISE.podeIrPara(StatusDocumento.RECEBIDO)).isFalse();
    }

    @Test
    void documentoRecusaTransicaoInvalidaSemAlterarEstado() {
        var criadoEm = Instant.parse("2026-11-09T12:00:00Z");
        var documento = new Documento(UUID.randomUUID(), TipoDocumento.RG, "rg.png", "image/png", 10,
                "0".repeat(64), "/tmp/x", "52998224725", "Maria Ficticia", criadoEm);
        documento.alterarStatus(StatusDocumento.CONCLUIDO, criadoEm.plusSeconds(5));

        assertThatThrownBy(() -> documento.alterarStatus(StatusDocumento.CONCLUIDO, criadoEm.plusSeconds(10)))
                .isInstanceOf(TransicaoDeStatusInvalidaException.class)
                .hasMessageContaining("CONCLUIDO");
        assertThat(documento.getAtualizadoEm()).isEqualTo(criadoEm.plusSeconds(5));
    }
}
