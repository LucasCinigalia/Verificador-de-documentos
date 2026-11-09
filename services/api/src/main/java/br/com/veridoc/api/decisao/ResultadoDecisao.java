package br.com.veridoc.api.decisao;

/** Resultado da verificação, do menos para o mais grave. */
public enum ResultadoDecisao {
    APROVADO(0),
    REVISAO_MANUAL(1),
    REPROVADO(2);

    private final int gravidade;

    ResultadoDecisao(int gravidade) {
        this.gravidade = gravidade;
    }

    public ResultadoDecisao maisGrave(ResultadoDecisao outro) {
        return outro.gravidade > this.gravidade ? outro : this;
    }
}
