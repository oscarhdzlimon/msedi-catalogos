package mx.gob.imss.edi.catalogos.models.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CatalogoItemDto(
        Long id,
        String clave,
        String descripcion,
        Integer orden,
        Long idPadre,
        String referencia) {
}
