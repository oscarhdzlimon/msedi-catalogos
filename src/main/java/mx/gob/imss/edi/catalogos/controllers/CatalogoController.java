package mx.gob.imss.edi.catalogos.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import mx.gob.imss.edi.catalogos.common.response.RespuestaGenerica;
import mx.gob.imss.edi.catalogos.models.response.CatalogoResponseDto;
import mx.gob.imss.edi.catalogos.services.CatalogoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/catalogos")
@SecurityRequirement(name = "bearerAuth")
public class CatalogoController {

    private final CatalogoService catalogoService;

    public CatalogoController(CatalogoService catalogoService) {
        this.catalogoService = catalogoService;
    }

    @GetMapping("/{claveCatalogo}")
    @Operation(summary = "Consulta un catalogo funcional permitido")
    public ResponseEntity<RespuestaGenerica<CatalogoResponseDto>> consultar(
            @PathVariable String claveCatalogo) {
        CatalogoResponseDto respuesta = catalogoService.consultar(claveCatalogo);
        return ResponseEntity.ok(RespuestaGenerica.exitosa("Catalogo consultado correctamente", respuesta));
    }
}
