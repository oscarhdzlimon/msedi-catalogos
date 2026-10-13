package mx.gob.imss.edi.catalogos.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.Map;
import mx.gob.imss.edi.catalogos.common.response.RespuestaGenerica;
import mx.gob.imss.edi.catalogos.filters.CorrelationIdFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(EdiException.class)
    public ResponseEntity<RespuestaGenerica<Map<String, String>>> handleEdi(
            EdiException exception, HttpServletRequest request) {
        logException(exception.getStatus(), exception.getCodigo(), exception.getMessage(), request, exception);
        return handleEdi(exception);
    }

    public ResponseEntity<RespuestaGenerica<Map<String, String>>> handleEdi(EdiException exception) {
        return ResponseEntity.status(exception.getStatus()).body(RespuestaGenerica.fallida(
                exception.getMessage(), Map.of("codigo", exception.getCodigo())));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleUnreadable(
            HttpMessageNotReadableException exception, HttpServletRequest request) {
        String mensaje = "El cuerpo de la solicitud no tiene un formato valido";
        logException(HttpStatus.BAD_REQUEST, "JSON_INVALIDO", mensaje, request, exception);
        return build(HttpStatus.BAD_REQUEST, "JSON_INVALIDO", mensaje, request);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ApiErrorResponse> handleMissingHeader(
            MissingRequestHeaderException exception, HttpServletRequest request) {
        String mensaje = "Falta el encabezado requerido: " + exception.getHeaderName();
        logException(HttpStatus.BAD_REQUEST, "ENCABEZADO_REQUERIDO", mensaje, request, exception);
        return build(HttpStatus.BAD_REQUEST, "ENCABEZADO_REQUERIDO", mensaje, request);
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiErrorResponse> handleDatabase(DataAccessException exception, HttpServletRequest request) {
        logException(HttpStatus.SERVICE_UNAVAILABLE, "CATALOGO_NO_DISPONIBLE",
                "El catalogo no se encuentra disponible", request, exception);
        return build(HttpStatus.SERVICE_UNAVAILABLE, "CATALOGO_NO_DISPONIBLE",
                "El catalogo no se encuentra disponible", request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(Exception exception, HttpServletRequest request) {
        logException(HttpStatus.INTERNAL_SERVER_ERROR, "ERROR_INTERNO",
                "Ocurrio un error interno al procesar la solicitud", request, exception);
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

    private void logException(HttpStatus status, String codigo, String mensaje,
            HttpServletRequest request, Exception exception) {
        String correlationId = valor((String) request.getAttribute(CorrelationIdFilter.REQUEST_ATTRIBUTE));
        String idTransaccion = valor(request.getHeader(CorrelationIdFilter.TRANSACTION_HEADER_NAME));
        Throwable root = rootCause(exception);
        SQLException sql = buscarCausa(exception, SQLException.class);
        String log = "Error en solicitud httpMethod={} path={} status={} codigo={} mensaje={} "
                + "correlacionId={} idTransaccion={} exception={} rootCause={} rootMessage={} "
                + "sqlState={} sqlErrorCode={}";
        if (status.is5xxServerError()) {
            LOGGER.error(log, request.getMethod(), request.getRequestURI(), status.value(),
                    codigo, mensaje, correlationId, idTransaccion,
                    exception.getClass().getName(), clase(root), mensaje(root),
                    sql == null ? "-" : valor(sql.getSQLState()),
                    sql == null ? "-" : sql.getErrorCode(), exception);
        } else {
            LOGGER.warn(log, request.getMethod(), request.getRequestURI(), status.value(),
                    codigo, mensaje, correlationId, idTransaccion,
                    exception.getClass().getName(), clase(root), mensaje(root),
                    sql == null ? "-" : valor(sql.getSQLState()),
                    sql == null ? "-" : sql.getErrorCode());
        }
    }

    private <T extends Throwable> T buscarCausa(Throwable exception, Class<T> tipo) {
        Throwable actual = exception;
        while (actual != null) {
            if (tipo.isInstance(actual)) {
                return tipo.cast(actual);
            }
            actual = actual.getCause();
        }
        return null;
    }

    private Throwable rootCause(Throwable throwable) {
        Throwable actual = throwable;
        while (actual != null && actual.getCause() != null && actual.getCause() != actual) {
            actual = actual.getCause();
        }
        return actual == null ? throwable : actual;
    }

    private String clase(Throwable throwable) {
        return throwable == null ? "-" : throwable.getClass().getName();
    }

    private String mensaje(Throwable throwable) {
        return throwable == null ? "-" : valor(throwable.getMessage());
    }

    private String valor(String value) {
        return StringUtils.hasText(value) ? value.strip() : "-";
    }
}
