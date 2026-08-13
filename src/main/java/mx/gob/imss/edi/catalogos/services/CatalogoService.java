package mx.gob.imss.edi.catalogos.services;

import mx.gob.imss.edi.catalogos.models.response.CatalogoResponseDto;

public interface CatalogoService {

    CatalogoResponseDto consultar(String claveCatalogo);
}
