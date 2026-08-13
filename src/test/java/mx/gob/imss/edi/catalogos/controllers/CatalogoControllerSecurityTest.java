package mx.gob.imss.edi.catalogos.controllers;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import mx.gob.imss.edi.catalogos.models.response.CatalogoResponseDto;
import mx.gob.imss.edi.catalogos.services.CatalogoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class CatalogoControllerSecurityTest {

    private static final String SECRET = "01234567890123456789012345678901";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CatalogoService catalogoService;

    @Test
    void rechazaPeticionSinBearer() throws Exception {
        mockMvc.perform(get("/api/v1/catalogos/perfil").header("X-Transaccion-Id", "12"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value("TOKEN_INVALIDO"));
    }

    @Test
    void exigeTransaccionConBearerValido() throws Exception {
        mockMvc.perform(get("/api/v1/catalogos/perfil")
                        .header("Authorization", "Bearer " + token(12L)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("TRANSACCION_REQUERIDA"));
    }

    @Test
    void rechazaTransaccionDistintaAlClaim() throws Exception {
        mockMvc.perform(get("/api/v1/catalogos/perfil")
                        .header("Authorization", "Bearer " + token(12L))
                        .header("X-Transaccion-Id", "13"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("TRANSACCION_NO_CORRESPONDE"));
    }

    @Test
    void consultaConBearerYTransaccionCorrespondiente() throws Exception {
        when(catalogoService.consultar(anyString()))
                .thenReturn(new CatalogoResponseDto("perfil", List.of()));

        mockMvc.perform(get("/api/v1/catalogos/perfil")
                        .header("Authorization", "Bearer " + token(12L))
                        .header("X-Transaccion-Id", "12")
                        .header("X-Correlacion-Id", "corr-1"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Correlacion-Id", "corr-1"))
                .andExpect(jsonPath("$.exito").value(true))
                .andExpect(jsonPath("$.respuesta.claveCatalogo").value("perfil"));
    }

    private String token(Long idTransaccion) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject("usuario-prueba")
                .id("jti-prueba")
                .issuer("msedi-autenticacion")
                .audience().add("edi").and()
                .claim("idTransaccion", idTransaccion)
                .claim("cveTransaccion", "transaccion-prueba")
                .claim("sistemaOrigen", "SIMF")
                .claim("user", "usuario-prueba")
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(300)))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }
}
