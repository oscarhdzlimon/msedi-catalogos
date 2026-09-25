package mx.gob.imss.edi.catalogos.repository;

import static org.assertj.core.api.Assertions.assertThat;

import mx.gob.imss.edi.catalogos.models.dto.CatalogoItemDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

@SpringBootTest
@Sql(scripts = "/catalogo-mapper-test.sql")
class CatalogoMapperIntegrationTest {

    @Autowired
    private CatalogoMapper mapper;

    @Test
    void mapeaElContratoCompletoEnTodasLasConsultas() {
        assertThat(mapper.consultarClasificacionesIncapacidad())
                .containsExactly(new CatalogoItemDto(1L, "CLASIFICACION", "Clasificacion", 1, null, null));
        assertThat(mapper.consultarDiasDescanso())
                .containsExactly(new CatalogoItemDto(1L, "LUN", "Lunes", 1, null, null));
        assertThat(mapper.consultarEstatus())
                .containsExactly(new CatalogoItemDto(2L, "ACTIVO", "Activo", null, 20L, null));
        assertThat(mapper.consultarPerfiles())
                .containsExactly(new CatalogoItemDto(3L, "MED", "Medico", null, null, null));
        assertThat(mapper.consultarRamosSeguro())
                .containsExactly(new CatalogoItemDto(4L, "EG", "Enfermedad general", 1, null, null));
        assertThat(mapper.consultarTiposDocumento())
                .containsExactly(new CatalogoItemDto(5L, "DOCUMENTO", "Documento", null, null, "^[0-9]+$"));
        assertThat(mapper.consultarTiposEstatus())
                .containsExactly(new CatalogoItemDto(6L, "TIPO_ESTATUS", "Tipo estatus", null, null, null));
        assertThat(mapper.consultarTiposIdentificacion())
                .containsExactly(new CatalogoItemDto(7L, "INE_IFE", "INE", 1, null, null));
        assertThat(mapper.consultarTiposIncapacidad())
                .containsExactly(new CatalogoItemDto(8L, "INICIAL", "Inicial", 1, null, null));
        assertThat(mapper.consultarTiposRiesgo())
                .containsExactly(new CatalogoItemDto(9L, "ACCIDENTE_TRABAJO", "Accidente de trabajo", 1, null, null));
    }
}
