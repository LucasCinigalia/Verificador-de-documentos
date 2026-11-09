package br.com.veridoc.api.decisao.regras;

import br.com.veridoc.api.analise.CampoExtraido;
import br.com.veridoc.api.analise.DadosExtraidos;
import br.com.veridoc.api.documento.TipoDocumento;

import java.time.LocalDate;

/**
 * Monta contextos de avaliação para os testes. Todos os dados são fictícios:
 * 52998224725 e 12345678909 são CPFs de teste com dígitos verificadores válidos.
 */
final class Cenario {

    static final LocalDate HOJE = LocalDate.of(2026, 11, 9);
    static final String CPF_VALIDO = "52998224725";
    static final String OUTRO_CPF_VALIDO = "12345678909";
    static final String NOME = "Maria Ficticia da Silva";

    private TipoDocumento tipo = TipoDocumento.CNH;
    private CampoExtraido nome = campo("MARIA FICTICIA DA SILVA");
    private CampoExtraido cpf = campo("529.982.247-25");
    private CampoExtraido dataNascimento = campo("1990-05-20");
    private CampoExtraido dataValidade = campo("2030-01-31");

    static Cenario cnhValida() {
        return new Cenario();
    }

    static CampoExtraido campo(String valor) {
        return new CampoExtraido(valor, 0.99);
    }

    static CampoExtraido campo(String valor, double confianca) {
        return new CampoExtraido(valor, confianca);
    }

    Cenario tipo(TipoDocumento tipo) {
        this.tipo = tipo;
        return this;
    }

    Cenario nome(CampoExtraido nome) {
        this.nome = nome;
        return this;
    }

    Cenario cpf(CampoExtraido cpf) {
        this.cpf = cpf;
        return this;
    }

    Cenario dataNascimento(CampoExtraido dataNascimento) {
        this.dataNascimento = dataNascimento;
        return this;
    }

    Cenario dataValidade(CampoExtraido dataValidade) {
        this.dataValidade = dataValidade;
        return this;
    }

    ContextoAvaliacao contexto() {
        return new ContextoAvaliacao(tipo, CPF_VALIDO, NOME,
                new DadosExtraidos(nome, cpf, dataNascimento, dataValidade), HOJE);
    }
}
