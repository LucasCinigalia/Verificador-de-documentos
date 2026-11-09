package br.com.veridoc.api.decisao.regras;

import br.com.veridoc.api.decisao.Motivo;
import br.com.veridoc.api.decisao.ResultadoDecisao;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MotorDeRiscoTest {

    private final RegrasRiscoProperties properties = new RegrasRiscoProperties(0.80, 18);

    private final MotorDeRisco motor = new MotorDeRisco(List.of(
            new CamposObrigatoriosRegra(),
            new ConfiancaMinimaRegra(properties),
            new CpfRegra(),
            new NomeRegra(),
            new ValidadeRegra(),
            new IdadeMinimaRegra(properties)));

    @Test
    void aprovaDocumentoSemRessalvasEGuardaOMotivo() {
        var avaliacao = motor.avaliar(Cenario.cnhValida().contexto());

        assertThat(avaliacao.resultado()).isEqualTo(ResultadoDecisao.APROVADO);
        assertThat(avaliacao.motivos()).containsExactly(MotorDeRisco.SEM_RESSALVAS);
    }

    @Test
    void revisaoManualQuandoSoHaRessalvasLeves() {
        var contexto = Cenario.cnhValida().nome(Cenario.campo("MARIA F. DA SILVA", 0.60)).contexto();

        var avaliacao = motor.avaliar(contexto);

        assertThat(avaliacao.resultado()).isEqualTo(ResultadoDecisao.REVISAO_MANUAL);
        assertThat(avaliacao.motivos()).extracting(Motivo::codigo)
                .containsExactlyInAnyOrder("BAIXA_CONFIANCA", "NOME_DIVERGENTE");
    }

    @Test
    void reprovacaoVenceRevisaoMasTodosOsMotivosSaoGuardados() {
        var contexto = Cenario.cnhValida()
                .nome(Cenario.campo("OUTRA PESSOA"))
                .dataValidade(Cenario.campo("2020-01-01"))
                .contexto();

        var avaliacao = motor.avaliar(contexto);

        assertThat(avaliacao.resultado()).isEqualTo(ResultadoDecisao.REPROVADO);
        assertThat(avaliacao.motivos()).extracting(Motivo::codigo)
                .containsExactlyInAnyOrder("NOME_DIVERGENTE", "DOCUMENTO_VENCIDO");
    }
}
