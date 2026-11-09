package br.com.veridoc.api.compartilhado;

import br.com.veridoc.api.documento.ArquivoInvalidoException;
import br.com.veridoc.api.documento.TransicaoDeStatusInvalidaException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;
import java.util.List;

/**
 * Converte exceções em respostas RFC 9457 (application/problem+json). A classe base já trata
 * os erros do Spring MVC (JSON malformado, UUID inválido, upload grande demais etc.).
 */
@RestControllerAdvice
public class TratadorDeErros extends ResponseEntityExceptionHandler {

    private static final String BASE_TIPO = "urn:veridoc:problema:";

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    ProblemDetail naoEncontrado(RecursoNaoEncontradoException e) {
        return problema(HttpStatus.NOT_FOUND, "recurso-nao-encontrado", "Recurso não encontrado", e.getMessage());
    }

    @ExceptionHandler(TransicaoDeStatusInvalidaException.class)
    ProblemDetail transicaoInvalida(TransicaoDeStatusInvalidaException e) {
        return problema(HttpStatus.CONFLICT, "transicao-de-status-invalida", "Transição de status inválida", e.getMessage());
    }

    @ExceptionHandler({ObjectOptimisticLockingFailureException.class, DataIntegrityViolationException.class})
    ProblemDetail conflitoDeConcorrencia(Exception e) {
        return problema(HttpStatus.CONFLICT, "conflito-de-concorrencia", "Conflito de concorrência",
                "O documento foi alterado por outra requisição. Consulte o status e tente novamente se necessário.");
    }

    @ExceptionHandler(ArquivoInvalidoException.class)
    ProblemDetail arquivoInvalido(ArquivoInvalidoException e) {
        return problema(HttpStatus.BAD_REQUEST, "arquivo-invalido", "Arquivo inválido", e.getMessage());
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers, HttpStatusCode status,
                                                                  WebRequest request) {
        List<ErroDeCampo> erros = ex.getBindingResult().getFieldErrors().stream()
                .map(erro -> new ErroDeCampo(erro.getField(),
                        erro.isBindingFailure() ? "valor inválido" : erro.getDefaultMessage()))
                .toList();
        var problema = problema(HttpStatus.BAD_REQUEST, "dados-invalidos", "Dados inválidos",
                "A requisição tem %d campo(s) inválido(s)".formatted(erros.size()));
        problema.setProperty("erros", erros);
        return handleExceptionInternal(ex, problema, headers, HttpStatus.BAD_REQUEST, request);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, @Nullable Object body, HttpHeaders headers,
                                                             HttpStatusCode statusCode, WebRequest request) {
        if (statusCode.is5xxServerError()) {
            logger.error("Erro inesperado", ex);
        }
        return super.handleExceptionInternal(ex, body, headers, statusCode, request);
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail erroInesperado(Exception e) {
        logger.error("Erro inesperado", e);
        return problema(HttpStatus.INTERNAL_SERVER_ERROR, "erro-interno", "Erro interno",
                "Ocorreu um erro inesperado. Tente novamente mais tarde.");
    }

    private static ProblemDetail problema(HttpStatus status, String tipo, String titulo, String detalhe) {
        var problema = ProblemDetail.forStatusAndDetail(status, detalhe);
        problema.setType(URI.create(BASE_TIPO + tipo));
        problema.setTitle(titulo);
        return problema;
    }

    public record ErroDeCampo(String campo, String mensagem) {
    }
}
