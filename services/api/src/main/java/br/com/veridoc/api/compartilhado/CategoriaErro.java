package br.com.veridoc.api.compartilhado;

/**
 * Tipo de falha de negócio, sem falar de HTTP. O domínio diz "isto é um conflito";
 * só o TratadorDeErros sabe que conflito vira 409.
 */
public enum CategoriaErro {
    NAO_ENCONTRADO,
    CONFLITO,
    REQUISICAO_INVALIDA
}
