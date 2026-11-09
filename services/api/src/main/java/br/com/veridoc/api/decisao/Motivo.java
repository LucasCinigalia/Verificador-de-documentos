package br.com.veridoc.api.decisao;

/** Por que uma regra levou a um resultado. O código é estável; a descrição é para humanos. */
public record Motivo(String codigo, ResultadoDecisao resultado, String descricao) {

    public static Motivo revisao(String codigo, String descricao) {
        return new Motivo(codigo, ResultadoDecisao.REVISAO_MANUAL, descricao);
    }

    public static Motivo reprovacao(String codigo, String descricao) {
        return new Motivo(codigo, ResultadoDecisao.REPROVADO, descricao);
    }
}
