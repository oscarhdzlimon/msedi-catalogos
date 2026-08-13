# Spec tecnica - msedi-catalogos

## Responsabilidad

`msedi-catalogos` expone al frontend Angular los catalogos funcionales de EDI usados en listas y selectores.

Alcance inicial:

- Solo consultas.
- Un catalogo por solicitud.
- Solo registros activos.
- Acceso con Bearer Token EDI e identificador de transaccion.
- Sin consumo desde otros microservicios.

`msedi-autenticacion` y `msedi-bitacora` consultaran directamente las tablas del esquema `catalogo` que necesiten. No se exponen al frontend `evento`, `mensaje`, `sistema_origen`, `guia_recuperacion` ni `transicion_estatus`.

## Context path

```yaml
server:
  servlet:
    context-path: /msedi-catalogos
```

## Endpoint

### Consultar catalogo por clave

```http
GET /api/v1/catalogos/{claveCatalogo}
```

Ruta completa:

```http
GET /msedi-catalogos/api/v1/catalogos/{claveCatalogo}
```

Headers:

```http
Authorization: Bearer <token-edi>
X-Transaccion-Id: 12
X-Correlacion-Id: <opcional>
```

Ejemplo:

```http
GET /msedi-catalogos/api/v1/catalogos/tipo-identificacion
```

Response exitosa:

```json
{
  "exito": true,
  "mensaje": "Catalogo consultado correctamente",
  "respuesta": {
    "claveCatalogo": "tipo-identificacion",
    "elementos": [
      {
        "id": 1,
        "descripcion": "Credencial para votar",
        "orden": 1
      }
    ]
  }
}
```

Un catalogo valido sin registros activos devuelve HTTP `200` y `elementos: []`.

## Catalogos permitidos

| Clave de API | Tabla Postgres | Orden |
| --- | --- | --- |
| `clasif-incapacidad` | `catalogo.clasif_incapacidad` | `num_orden, id_clasif_incapacidad` |
| `estatus` | `catalogo.estatus` | `des_estatus, id_estatus` |
| `perfil` | `catalogo.perfil` | `des_perfil, id_perfil` |
| `ramo-seguro` | `catalogo.ramo_seguro` | `num_orden, id_ramo_seguro` |
| `tipo-documento` | `catalogo.tipo_documento` | `des_tipo_documento, id_tipo_documento` |
| `tipo-estatus` | `catalogo.tipo_estatus` | `des_tipo_estatus, id_tipo_estatus` |
| `tipo-identificacion` | `catalogo.tipo_identificacion` | `num_orden, id_tipo_identificacion` |
| `tipo-incapacidad` | `catalogo.tipo_incapacidad` | `num_orden, id_tipo_incapacidad` |
| `tipo-riesgo` | `catalogo.tipo_riesgo` | `num_orden, id_tipo_riesgo` |

La clave se recibe en minusculas y kebab-case. No se aceptan alias, nombres de esquema ni nombres de tabla.

## Contrato normalizado

```java
public class CatalogoResponseDto {
    private String claveCatalogo;
    private List<CatalogoItemDto> elementos;
}

public class CatalogoItemDto {
    private Long id;
    private String clave;
    private String descripcion;
    private Integer orden;
    private Long idPadre;
    private String referencia;
}
```

Los campos nulos se omiten del JSON.

Mapeo especial:

- `estatus.id_tipo_estatus` se devuelve como `idPadre`.
- `tipo_documento.ref_formato` se devuelve como `referencia`.
- `perfil.cve_perfil` y `ramo_seguro.cve_ramo_seguro` se devuelven como `clave`.
- Los catalogos sin clave funcional devuelven `id`, `descripcion` y, cuando exista, `orden`.

## Reglas de consulta

- Consultar exclusivamente registros con `ind_activo = true`.
- Aplicar el orden definido para cada catalogo.
- Validar `claveCatalogo` contra `CatalogoClave`.
- No concatenar `claveCatalogo` dentro de SQL.
- No usar `${}` de MyBatis para resolver tablas o columnas.
- Implementar una consulta SQL explicita por catalogo.
- Ejecutar consultas en modo de solo lectura.
- No devolver campos de auditoria al frontend.

## Resolucion de clave

```text
CatalogoClave
  CLASIF_INCAPACIDAD("clasif-incapacidad")
  ESTATUS("estatus")
  PERFIL("perfil")
  RAMO_SEGURO("ramo-seguro")
  TIPO_DOCUMENTO("tipo-documento")
  TIPO_ESTATUS("tipo-estatus")
  TIPO_IDENTIFICACION("tipo-identificacion")
  TIPO_INCAPACIDAD("tipo-incapacidad")
  TIPO_RIESGO("tipo-riesgo")
```

El service resuelve el enum y delega al metodo MyBatis correspondiente mediante un registro estatico. No se permite reflexion ni SQL dinamico basado en el texto recibido.

## Errores HTTP

| Caso | HTTP | Codigo |
| --- | --- | --- |
| Clave no soportada | `400` | `CATALOGO_NO_SOPORTADO` |
| Falta `X-Transaccion-Id` | `400` | `TRANSACCION_REQUERIDA` |
| Bearer ausente, invalido o expirado | `401` | `TOKEN_INVALIDO` |
| La transaccion no coincide con el JWT | `403` | `TRANSACCION_NO_CORRESPONDE` |
| Base de datos no disponible | `503` | `CATALOGO_NO_DISPONIBLE` |

Ejemplo de clave no soportada:

```json
{
  "exito": false,
  "mensaje": "El catalogo solicitado no esta soportado",
  "respuesta": {
    "codigo": "CATALOGO_NO_SOPORTADO",
    "claveCatalogo": "catalogo-inexistente"
  }
}
```

## Seguridad

- Seguridad stateless con Spring Security.
- Validar firma, expiracion y audiencia del JWT EDI.
- Comparar `X-Transaccion-Id` con el claim `idTransaccion`.
- No aceptar la API key del sistema convocante.
- No permitir acceso anonimo al endpoint funcional.
- Liberar unicamente los endpoints operativos autorizados, como health checks.

El Bearer se obtiene previamente mediante:

```http
POST /msedi-autenticacion/api/v1/accesos/intercambio
```

## Persistencia

- Postgres, esquema `catalogo`.
- MyBatis con SQL explicito y nombres calificados por esquema.
- Usuario de base con `USAGE` en el esquema y `SELECT` solo en las nueve tablas expuestas.
- Sin permisos de escritura o DDL para el usuario de aplicacion.
- Mientras desarrollo use el owner, los permisos restringidos se consideran objetivo de despliegue.

## Paquetes sugeridos

```text
mx.gob.imss.edi.catalogos
  config
  controllers
  exceptions
  filters
  security
  services
  services.impl
  repository
  models.response
  models.dto
  common.enums
  common.response
```

## Clases principales

- `CatalogoController`
- `CatalogoService`
- `CatalogoServiceImpl`
- `CatalogoMapper`
- `CatalogoClave`
- `CatalogoResponseDto`
- `CatalogoItemDto`
- `RespuestaGenerica<T>`
- `JwtAuthenticationFilter`
- `CorrelationIdFilter`
- `GlobalExceptionHandler`

## MyBatis mapper

`CatalogoMapper` define un metodo por catalogo:

```text
consultarClasificacionesIncapacidad()
consultarEstatus()
consultarPerfiles()
consultarRamosSeguro()
consultarTiposDocumento()
consultarTiposEstatus()
consultarTiposIdentificacion()
consultarTiposIncapacidad()
consultarTiposRiesgo()
```

## Dependencias Maven

- `spring-boot-starter-web`
- `spring-boot-starter-security`
- `spring-boot-starter-validation`
- `spring-boot-starter-actuator`
- `mybatis-spring-boot-starter`
- `postgresql`
- `jjwt-api`, `jjwt-impl`, `jjwt-jackson`
- `springdoc-openapi-starter-webmvc-ui`
- `lombok`
- `spring-boot-starter-test`
- `spring-security-test`
- `testcontainers-postgresql`

No requiere Feign Client en el alcance inicial.

## Configuracion yml esperada

```yaml
server:
  port: 8080
  servlet:
    context-path: /msedi-catalogos

spring:
  application:
    name: msedi-catalogos
  datasource:
    url: ${urlConexionBd}
    username: ${userBd}
    password: ${passwordBd}
    driver-class-name: org.postgresql.Driver

mybatis:
  mapper-locations: classpath:mappers/*.xml
  configuration:
    map-underscore-to-camel-case: true

edi:
  seguridad:
    jwt:
      secret-key: ${jwtSecretKey}
      audience: ${EDI_JWT_AUDIENCE:edi}
```

## Swagger y Actuator

- Swagger UI: `/msedi-catalogos/swagger-ui/index.html`
- OpenAPI: `/msedi-catalogos/v3/api-docs`
- Actuator: `/msedi-catalogos/actuator`
- Health: `/msedi-catalogos/actuator/health`

## Pruebas requeridas

Pruebas unitarias:

- Resolver las nueve claves permitidas.
- Rechazar claves desconocidas.
- Delegar cada clave al metodo MyBatis correcto.
- Validar ausencia o diferencia de `X-Transaccion-Id`.
- Omitir campos nulos en la respuesta.

Pruebas de integracion:

- Consultar los nueve catalogos con Postgres Testcontainers.
- Comprobar registros activos y orden.
- Comprobar lista vacia para un catalogo valido.
- Comprobar respuestas `400`, `401`, `403` y `503`.
- Comprobar que no se exponen campos de auditoria.

## Decisiones cerradas

- El micro se consume unicamente desde Angular.
- Los demas micros consultan directamente el esquema `catalogo`.
- Existe un solo endpoint funcional.
- Se consulta un catalogo por solicitud.
- No se exponen catalogos internos.
- No se implementan altas, modificaciones ni bajas.
- No se usa Feign Client.

## Decisiones pendientes

- Confirmar cuales de los nueve catalogos se utilizan en HU001-HU012 al cerrar el contrato de los 28 campos.


## Decisiones implementadas

- El micro no consume `msedi-bitacora` y no registra cada consulta exitosa para evitar una dependencia circular.
- Los errores tecnicos se conservan en el log operativo con `X-Correlacion-Id`.
- Cada clave delega a un metodo MyBatis con SQL explicito; no existe sustitucion de nombres de tabla desde la URL.
- Las consultas se ejecutan en transacciones de solo lectura y el pool JDBC se configura como `read-only`.
