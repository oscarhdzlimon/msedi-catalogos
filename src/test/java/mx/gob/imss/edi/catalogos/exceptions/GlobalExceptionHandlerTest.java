package mx.gob.imss.edi.catalogos.exceptions;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class GlobalExceptionHandlerTest {

    @Test
    void usaRespuestaGenericaParaErrorFuncional() {
        var response = new GlobalExceptionHandler().handleEdi(
                new EdiException(HttpStatus.BAD_REQUEST, "CATALOGO_NO_SOPORTADO", "No soportado"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().exito()).isFalse();
        assertThat(response.getBody().respuesta()).containsEntry("codigo", "CATALOGO_NO_SOPORTADO");
    }
}
