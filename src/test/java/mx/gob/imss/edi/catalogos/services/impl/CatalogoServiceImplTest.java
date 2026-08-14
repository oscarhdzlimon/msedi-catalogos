package mx.gob.imss.edi.catalogos.services.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import mx.gob.imss.edi.catalogos.exceptions.EdiException;
import mx.gob.imss.edi.catalogos.models.dto.CatalogoItemDto;
import mx.gob.imss.edi.catalogos.repository.CatalogoMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CatalogoServiceImplTest {

    @Mock
    private CatalogoMapper mapper;

    private CatalogoServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new CatalogoServiceImpl(mapper);
    }

    @Test
    void delegaCadaClaveAlMetodoExplicito() {
        service.consultar("clasif-incapacidad");
        service.consultar("estatus");
        service.consultar("perfil");
        service.consultar("ramo-seguro");
        service.consultar("tipo-documento");
        service.consultar("tipo-estatus");
        service.consultar("tipo-identificacion");
        service.consultar("tipo-incapacidad");
        service.consultar("tipo-riesgo");

        verify(mapper).consultarClasificacionesIncapacidad();
        verify(mapper).consultarEstatus();
        verify(mapper).consultarPerfiles();
        verify(mapper).consultarRamosSeguro();
        verify(mapper).consultarTiposDocumento();
        verify(mapper).consultarTiposEstatus();
        verify(mapper).consultarTiposIdentificacion();
        verify(mapper).consultarTiposIncapacidad();
        verify(mapper).consultarTiposRiesgo();
    }

    @Test
    void devuelveUnaListaInmutableConLaClaveSolicitada() {
        var item = new CatalogoItemDto(1L, null, "Credencial", 1, null, null);
        when(mapper.consultarTiposIdentificacion()).thenReturn(List.of(item));

        var response = service.consultar("tipo-identificacion");

        assertThat(response.claveCatalogo()).isEqualTo("tipo-identificacion");
        assertThat(response.elementos()).containsExactly(item);
    }

    @Test
    void devuelveListaVaciaSiElMapperNoEncuentraActivos() {
        when(mapper.consultarPerfiles()).thenReturn(null);

        assertThat(service.consultar("perfil").elementos()).isEmpty();
    }

    @Test
    void rechazaClaveNoSoportadaSinInvocarSqlDinamico() {
        assertThatThrownBy(() -> service.consultar("catalogo-no-soportado"))
                .isInstanceOf(EdiException.class)
                .extracting("codigo")
                .isEqualTo("CATALOGO_NO_SOPORTADO");
    }
}
