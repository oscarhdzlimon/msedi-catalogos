package mx.gob.imss.edi.catalogos.models.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class CatalogoItemDtoJsonTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void omiteCamposNulos() throws Exception {
        String json = objectMapper.writeValueAsString(
                new CatalogoItemDto(1L, null, "Activo", null, null, null));

        assertThat(json).contains("\"id\":1", "\"descripcion\":\"Activo\"")
                .doesNotContain("clave", "orden", "idPadre", "referencia");
    }
}
