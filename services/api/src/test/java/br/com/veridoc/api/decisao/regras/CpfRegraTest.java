package br.com.veridoc.api.decisao.regras;

import br.com.veridoc.api.decisao.ResultadoDecisao;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CpfRegraTest {

    private final CpfRegra regra = new CpfRegra();

    @Test
    void aceitaCpfValidoIgualAoDeclaradoMesmoComPontuacao() {
        assertThat(regra.avaliar(Cenario.cnhValida().contexto())).isEmpty();
    }

    @Test
    void reprovaCpfComDigitoVerificadorInvalido() {
        var contexto = Cenario.cnhValida().cpf(Cenario.campo("529.982.247-24")).contexto();

        var motivo = regra.avaliar(contexto).orElseThrow();

        assertThat(motivo.resultado()).isEqualTo(ResultadoDecisao.REPROVADO);
        assertThat(motivo.codigo()).isEqualTo("CPF_INVALIDO");
    }

    @Test
    void reprovaCpfValidoMasDiferenteDoDeclarado() {
        var contexto = Cenario.cnhValida().cpf(Cenario.campo(Cenario.OUTRO_CPF_VALIDO)).contexto();

        var motivo = regra.avaliar(contexto).orElseThrow();

        assertThat(motivo.resultado()).isEqualTo(ResultadoDecisao.REPROVADO);
        assertThat(motivo.codigo()).isEqualTo("CPF_DIVERGENTE");
    }

    @Test
    void deixaCpfAusenteParaARegraDeCamposObrigatorios() {
        assertThat(regra.avaliar(Cenario.cnhValida().cpf(null).contexto())).isEmpty();
    }
}
