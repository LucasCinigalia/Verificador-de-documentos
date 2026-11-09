package br.com.veridoc.api.documento;

import br.com.veridoc.api.compartilhado.CategoriaErro;
import br.com.veridoc.api.compartilhado.ErroDeNegocioException;

public class ArquivoInvalidoException extends ErroDeNegocioException {

    public ArquivoInvalidoException(String mensagem) {
        super(CategoriaErro.REQUISICAO_INVALIDA, "arquivo-invalido", "Arquivo inválido", mensagem);
    }
}
