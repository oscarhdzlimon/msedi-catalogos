package mx.gob.imss.edi.catalogos.services.impl;

import mx.gob.imss.edi.catalogos.common.enums.MensajeEdi;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import mx.gob.imss.edi.catalogos.common.enums.CatalogoClave;
import mx.gob.imss.edi.catalogos.exceptions.EdiException;
import mx.gob.imss.edi.catalogos.models.dto.CatalogoItemDto;
import mx.gob.imss.edi.catalogos.models.response.CatalogoResponseDto;
import mx.gob.imss.edi.catalogos.repository.CatalogoMapper;
import mx.gob.imss.edi.catalogos.services.CatalogoService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CatalogoServiceImpl implements CatalogoService {

    private final Map<CatalogoClave, Supplier<List<CatalogoItemDto>>> consultas;
    private final CatalogoMapper mapper;

    public CatalogoServiceImpl(CatalogoMapper mapper) {
        this.mapper = mapper;
        consultas = new EnumMap<>(CatalogoClave.class);
        consultas.put(CatalogoClave.CLASIF_INCAPACIDAD, mapper::consultarClasificacionesIncapacidad);
        consultas.put(CatalogoClave.DIA_DESCANSO, mapper::consultarDiasDescanso);
        consultas.put(CatalogoClave.ESTADO_CIVIL, mapper::consultarEstadosCiviles);
        consultas.put(CatalogoClave.ESTATUS, mapper::consultarEstatus);
        consultas.put(CatalogoClave.PERFIL, mapper::consultarPerfiles);
        consultas.put(CatalogoClave.RAMO_SEGURO, mapper::consultarRamosSeguro);
        consultas.put(CatalogoClave.TIPO_DOCUMENTO, mapper::consultarTiposDocumento);
        consultas.put(CatalogoClave.TIPO_ESTATUS, mapper::consultarTiposEstatus);
        consultas.put(CatalogoClave.TIPO_IDENTIFICACION, mapper::consultarTiposIdentificacion);
        consultas.put(CatalogoClave.TIPO_INCAPACIDAD, mapper::consultarTiposIncapacidad);
        consultas.put(CatalogoClave.TIPO_RIESGO, mapper::consultarTiposRiesgo);
    }

    @Override
    @Transactional(readOnly = true)
    public CatalogoResponseDto consultar(String claveCatalogo) {
        return consultar(claveCatalogo, null);
    }

    @Override
    @Transactional(readOnly = true)
    public CatalogoResponseDto consultar(String claveCatalogo, String fraccionEmpresa) {
        CatalogoClave clave = CatalogoClave.buscar(claveCatalogo)
                .orElseThrow(() -> new EdiException(
                        HttpStatus.BAD_REQUEST,
                        MensajeEdi.CATALOGO_NO_SOPORTADO.codigo(),
                        MensajeEdi.CATALOGO_NO_SOPORTADO.textoRespaldo()));
        List<CatalogoItemDto> elementos;
        if (clave == CatalogoClave.CARGA_TRABAJO) {
            if (fraccionEmpresa == null || !fraccionEmpresa.matches("[0-9]{1,4}")) {
                throw new EdiException(HttpStatus.BAD_REQUEST, MensajeEdi.FRACCION_EMPRESA_INVALIDA.codigo(),
                        MensajeEdi.FRACCION_EMPRESA_INVALIDA.textoRespaldo());
            }
            elementos = mapper.consultarCargaTrabajo(Integer.toString(Integer.parseInt(fraccionEmpresa)));
        } else {
            elementos = consultas.get(clave).get();
        }
        return new CatalogoResponseDto(clave.getClaveApi(), elementos == null ? List.of() : List.copyOf(elementos));
    }
}
