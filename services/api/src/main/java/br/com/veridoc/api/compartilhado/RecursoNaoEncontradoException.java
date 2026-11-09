package br.com.veridoc.api.compartilhado;

public class RecursoNaoEncontradoException extends ErroDeNegocioException {

    public RecursoNaoEncontradoException(String mensagem) {
        super(CategoriaErro.NAO_ENCONTRADO, "recurso-nao-encontrado", "Recurso não encontrado", mensagem);
    }
}
