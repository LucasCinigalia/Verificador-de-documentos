package br.com.veridoc.api.decisao.regras;

import br.com.veridoc.api.decisao.ResultadoDecisao;
import br.com.veridoc.api.documento.TipoDocumento;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CamposObrigatoriosRegraTest {

    private final CamposObrigatoriosRegra regra = new CamposObrigatoriosRegra();

    @Test
    void naoGeraMotivoQuandoTodosOsCamposExistem() {
        assertThat(regra.avaliar(Cenario.cnhValida().contexto())).isEmpty();
    }

    @Test
    void mandaParaRevisaoListandoCamposAusentes() {
        var contexto = Cenario.cnhValida().cpf(null).dataValidade(Cenario.campo("  ")).contexto();

        var motivo = regra.avaliar(contexto).orElseThrow();

        assertThat(motivo.resultado()).isEqualTo(ResultadoDecisao.REVISAO_MANUAL);
        assertThat(motivo.codigo()).isEqualTo("CAMPO_AUSENTE");
        assertThat(motivo.descricao()).contains("CPF", "DATA_VALIDADE");
    }

    @Test
    void rgNaoExigeDataDeValidade() {
        var contexto = Cenario.cnhValida().tipo(TipoDocumento.RG).dataValidade(null).contexto();

        assertThat(regra.avaliar(contexto)).isEmpty();
    }
}
