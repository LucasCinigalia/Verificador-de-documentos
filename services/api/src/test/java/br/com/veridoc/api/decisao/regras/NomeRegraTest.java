package br.com.veridoc.api.decisao.regras;

import br.com.veridoc.api.decisao.ResultadoDecisao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class NomeRegraTest {

    private final NomeRegra regra = new NomeRegra();

    @ParameterizedTest
    @ValueSource(strings = {"MARIA FICTICIA DA SILVA", "maria fictícia da silva", "  Maria   Fictícia da Silva "})
    void ignoraAcentoCaixaEEspacos(String nomeExtraido) {
        var contexto = Cenario.cnhValida().nome(Cenario.campo(nomeExtraido)).contexto();

        assertThat(regra.avaliar(contexto)).isEmpty();
    }

    @Test
    void mandaParaRevisaoQuandoNomeDiverge() {
        var contexto = Cenario.cnhValida().nome(Cenario.campo("MARIA F. DA SILVA")).contexto();

        var motivo = regra.avaliar(contexto).orElseThrow();

        assertThat(motivo.resultado()).isEqualTo(ResultadoDecisao.REVISAO_MANUAL);
        assertThat(motivo.codigo()).isEqualTo("NOME_DIVERGENTE");
    }
}
