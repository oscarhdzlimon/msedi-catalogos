package mx.gob.imss.edi.catalogos.common.enums;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import org.junit.jupiter.api.Test;

class CatalogoClaveTest {

    @Test
    void contieneLasDiezClavesPermitidas() {
        assertThat(CatalogoClave.values()).hasSize(10);
        Arrays.stream(CatalogoClave.values())
                .forEach(clave -> assertThat(CatalogoClave.buscar(clave.getClaveApi())).contains(clave));
    }

    @Test
    void rechazaUnaClaveNoPermitida() {
        assertThat(CatalogoClave.buscar("catalogo-no-soportado")).isEmpty();
    }
}
