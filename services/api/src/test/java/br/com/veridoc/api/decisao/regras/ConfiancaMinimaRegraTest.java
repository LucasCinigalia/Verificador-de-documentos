package br.com.veridoc.api.decisao.regras;

import br.com.veridoc.api.decisao.ResultadoDecisao;
import br.com.veridoc.api.documento.TipoDocumento;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConfiancaMinimaRegraTest {

    private final ConfiancaMinimaRegra regra = new ConfiancaMinimaRegra(new RegrasRiscoProperties(0.80, 18));

    @Test
    void aceitaConfiancaIgualAoLimite() {
        var contexto = Cenario.cnhValida().nome(Cenario.campo(Cenario.NOME, 0.80)).contexto();

        assertThat(regra.avaliar(contexto)).isEmpty();
    }

    @Test
    void mandaParaRevisaoQuandoCampoObrigatorioTemBaixaConfianca() {
        var contexto = Cenario.cnhValida().nome(Cenario.campo(Cenario.NOME, 0.79)).contexto();

        var motivo = regra.avaliar(contexto).orElseThrow();

        assertThat(motivo.resultado()).isEqualTo(ResultadoDecisao.REVISAO_MANUAL);
        assertThat(motivo.codigo()).isEqualTo("BAIXA_CONFIANCA");
        assertThat(motivo.descricao()).contains("NOME");
    }

    @Test
    void ignoraCampoQueNaoEObrigatorioParaOTipo() {
        var contexto = Cenario.cnhValida()
                .tipo(TipoDocumento.RG)
                .dataValidade(Cenario.campo("2030-01-31", 0.10))
                .contexto();

        assertThat(regra.avaliar(contexto)).isEmpty();
    }
}
