package mx.gob.imss.edi.catalogos.models.response;

import java.util.List;
import mx.gob.imss.edi.catalogos.models.dto.CatalogoItemDto;

public record CatalogoResponseDto(String claveCatalogo, List<CatalogoItemDto> elementos) {
}
