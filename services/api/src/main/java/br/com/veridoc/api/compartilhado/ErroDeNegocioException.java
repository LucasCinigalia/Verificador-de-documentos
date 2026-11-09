package br.com.veridoc.api.compartilhado;

/**
 * Base das exceções de negócio. Cada domínio cria as suas estendendo esta classe, e o
 * TratadorDeErros trata todas de uma vez, sem precisar importar nada dos domínios.
 * A dependência aponta para cá (domínio → compartilhado), nunca no sentido contrário.
 */
public abstract class ErroDeNegocioException extends RuntimeException {

    private final CategoriaErro categoria;
    private final String codigo;
    private final String titulo;

    /**
     * @param codigo identificador estável do problema, vira o "type" do ProblemDetail
     *               (urn:veridoc:problema:{codigo}); faz parte do contrato da API
     * @param titulo resumo legível para humanos, vira o "title"
     */
    protected ErroDeNegocioException(CategoriaErro categoria, String codigo, String titulo, String mensagem) {
        super(mensagem);
        this.categoria = categoria;
        this.codigo = codigo;
        this.titulo = titulo;
    }

    public CategoriaErro getCategoria() {
        return categoria;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getTitulo() {
        return titulo;
    }
}
