package mx.gob.imss.edi.catalogos.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import java.time.OffsetDateTime;
import java.util.Map;
import mx.gob.imss.edi.catalogos.common.response.RespuestaGenerica;
import mx.gob.imss.edi.catalogos.filters.CorrelationIdFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(EdiException.class)
    public ResponseEntity<RespuestaGenerica<Map<String, String>>> handleEdi(EdiException exception) {
        return ResponseEntity.status(exception.getStatus()).body(RespuestaGenerica.fallida(
                exception.getMessage(), Map.of("codigo", exception.getCodigo())));
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiErrorResponse> handleDatabase(DataAccessException exception, HttpServletRequest request) {
        LOGGER.error("Error al consultar catalogo", exception);
        return build(HttpStatus.SERVICE_UNAVAILABLE, "CATALOGO_NO_DISPONIBLE",
                "El catalogo no se encuentra disponible", request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(Exception exception, HttpServletRequest request) {
        LOGGER.error("Error no controlado", exception);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "ERROR_INTERNO",
                "Ocurrio un error interno al procesar la solicitud", request);
    }

    private ResponseEntity<ApiErrorResponse> build(
            HttpStatus status, String codigo, String mensaje, HttpServletRequest request) {
        var body = new ApiErrorResponse(
                OffsetDateTime.now(), status.value(), status.getReasonPhrase(), codigo, mensaje,
                request.getRequestURI(),
                (String) request.getAttribute(CorrelationIdFilter.REQUEST_ATTRIBUTE), Map.of());
        return ResponseEntity.status(status).body(body);
    }
}
