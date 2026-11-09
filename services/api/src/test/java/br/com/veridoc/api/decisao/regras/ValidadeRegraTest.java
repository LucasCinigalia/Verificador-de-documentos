package br.com.veridoc.api.decisao.regras;

import br.com.veridoc.api.decisao.ResultadoDecisao;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ValidadeRegraTest {

    private final ValidadeRegra regra = new ValidadeRegra();

    @Test
    void aceitaDocumentoQueVenceHoje() {
        var contexto = Cenario.cnhValida().dataValidade(Cenario.campo(Cenario.HOJE.toString())).contexto();

        assertThat(regra.avaliar(contexto)).isEmpty();
    }

    @Test
    void reprovaDocumentoVencidoOntem() {
        var ontem = Cenario.HOJE.minusDays(1).toString();
        var contexto = Cenario.cnhValida().dataValidade(Cenario.campo(ontem)).contexto();

        var motivo = regra.avaliar(contexto).orElseThrow();

        assertThat(motivo.resultado()).isEqualTo(ResultadoDecisao.REPROVADO);
        assertThat(motivo.codigo()).isEqualTo("DOCUMENTO_VENCIDO");
    }

    @Test
    void mandaParaRevisaoQuandoDataEstaMalFormatada() {
        var contexto = Cenario.cnhValida().dataValidade(Cenario.campo("31/01/2030")).contexto();

        var motivo = regra.avaliar(contexto).orElseThrow();

        assertThat(motivo.resultado()).isEqualTo(ResultadoDecisao.REVISAO_MANUAL);
        assertThat(motivo.codigo()).isEqualTo("DATA_INVALIDA");
    }

    @Test
    void ignoraValidadeAusente() {
        assertThat(regra.avaliar(Cenario.cnhValida().dataValidade(null).contexto())).isEmpty();
    }
}
