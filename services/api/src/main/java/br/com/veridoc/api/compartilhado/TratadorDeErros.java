package br.com.veridoc.api.compartilhado;

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

    /** Uma regra para todas as exceções de negócio: a categoria decide o status HTTP. */
    @ExceptionHandler(ErroDeNegocioException.class)
    ProblemDetail erroDeNegocio(ErroDeNegocioException e) {
        HttpStatus status = switch (e.getCategoria()) {
            case NAO_ENCONTRADO -> HttpStatus.NOT_FOUND;
            case CONFLITO -> HttpStatus.CONFLICT;
            case REQUISICAO_INVALIDA -> HttpStatus.BAD_REQUEST;
        };
        return problema(status, e.getCodigo(), e.getTitulo(), e.getMessage());
    }

    @ExceptionHandler({ObjectOptimisticLockingFailureException.class, DataIntegrityViolationException.class})
    ProblemDetail conflitoDeConcorrencia(Exception e) {
        return problema(HttpStatus.CONFLICT, "conflito-de-concorrencia", "Conflito de concorrência",
                "O documento foi alterado por outra requisição. Consulte o status e tente novamente se necessário.");
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
