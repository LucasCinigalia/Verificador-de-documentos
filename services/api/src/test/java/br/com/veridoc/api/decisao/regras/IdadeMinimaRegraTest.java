package br.com.veridoc.api.decisao.regras;

import br.com.veridoc.api.decisao.ResultadoDecisao;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class IdadeMinimaRegraTest {

    private final IdadeMinimaRegra regra = new IdadeMinimaRegra(new RegrasRiscoProperties(0.80, 18));

    @Test
    void aceitaQuemFaz18AnosHoje() {
        var nascimento = Cenario.HOJE.minusYears(18).toString();
        var contexto = Cenario.cnhValida().dataNascimento(Cenario.campo(nascimento)).contexto();

        assertThat(regra.avaliar(contexto)).isEmpty();
    }

    @Test
    void reprovaQuemFaz18AnosAmanha() {
        var nascimento = Cenario.HOJE.minusYears(18).plusDays(1).toString();
        var contexto = Cenario.cnhValida().dataNascimento(Cenario.campo(nascimento)).contexto();

        var motivo = regra.avaliar(contexto).orElseThrow();

        assertThat(motivo.resultado()).isEqualTo(ResultadoDecisao.REPROVADO);
        assertThat(motivo.codigo()).isEqualTo("MENOR_DE_IDADE");
        assertThat(motivo.descricao()).contains("17 anos");
    }

    @Test
    void mandaParaRevisaoQuandoNascimentoEstaNoFuturo() {
        var contexto = Cenario.cnhValida().dataNascimento(Cenario.campo("2099-01-01")).contexto();

        var motivo = regra.avaliar(contexto).orElseThrow();

        assertThat(motivo.resultado()).isEqualTo(ResultadoDecisao.REVISAO_MANUAL);
        assertThat(motivo.codigo()).isEqualTo("DATA_INVALIDA");
    }
}
