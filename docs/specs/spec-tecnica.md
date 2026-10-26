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
| `carga-trabajo` | `catalogo.edic_actividad_economica` + `catalogo.edic_carga_trabajo` | Una carga por `fraccionEmpresa` |
| `clasif-incapacidad` | `catalogo.edic_clasif_incapacidad` | `num_orden, id_clasif_incapacidad` |
| `estado-civil` | `catalogo.edic_estado_civil` | `num_orden, id_estado_civil` |
| `estatus` | `catalogo.edic_estatus` | `des_estatus, id_estatus` |
| `perfil` | `catalogo.edic_perfil` | `des_perfil, id_perfil` |
| `ramo-seguro` | `catalogo.edic_ramo_seguro` | `num_orden, id_ramo_seguro` |
| `tipo-documento` | `catalogo.edic_tipo_documento` | `des_tipo_documento, id_tipo_documento` |
| `tipo-estatus` | `catalogo.edic_tipo_estatus` | `des_tipo_estatus, id_tipo_estatus` |
| `tipo-identificacion` | `catalogo.edic_tipo_identificacion` | `num_orden, id_tipo_identificacion` |
| `tipo-incapacidad` | `catalogo.edic_tipo_incapacidad` | `num_orden, id_tipo_incapacidad` |
| `tipo-riesgo` | `catalogo.edic_tipo_riesgo` | `num_orden, id_tipo_riesgo` |

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
  ESTADO_CIVIL("estado-civil")
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
- Usuario de base con `USAGE` en el esquema y `SELECT` solo en las tablas expuestas.
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
consultarEstadosCiviles()
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

- Resolver las claves permitidas.
- Rechazar claves desconocidas.
- Delegar cada clave al metodo MyBatis correcto.
- Validar ausencia o diferencia de `X-Transaccion-Id`.
- Omitir campos nulos en la respuesta.

Pruebas de integracion:

- Consultar los catalogos permitidos con Postgres Testcontainers.
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

## Carga de trabajo por actividad económica (2026-10-26)

`GET /api/v1/catalogos/carga-trabajo?fraccionEmpresa=3510` utiliza la fracción
del patrón seleccionado de Historia Laboral, no su giro. Conserva la seguridad
JWT, transacción y correlación del endpoint existente. Solo consultas MyBatis;
la aplicación no crea ni carga tablas al iniciar.

- `fraccionEmpresa` es obligatorio para esta clave: entero de 0 a 9999; se
  normaliza a la representación decimal del catálogo. Ausencia o formato inválido:
  HTTP 400, `FRACCION_EMPRESA_INVALIDA`.
- IDs fijos: 1 Liviano, 2 Moderado, 3 Pesado.
- Respuesta normalizada: `claveCatalogo: "carga-trabajo"`, cero o un elemento;
  `id` es la carga, `clave` su ID textual, `descripcion` su nombre, `orden` su ID
  y `referencia` la clave de actividad económica.
- Ejemplo 3510: `id: 3`, `clave: "3"`, `descripcion: "Pesado"`, `referencia: "3510"`.
- Actividad inexistente, sin carga (clave 0), actividad inactiva o carga inactiva:
  HTTP 200 y `elementos: []`. Falla de base: HTTP 503; no convertirla en ausencia.
- Fuente autorizada: catálogo de julio 2026, 292 actividades, 3 cargas;
  carga manual `config/sql/20261026_catalogo_actividad_carga.sql` desde raíz EDI,
  con auditoría en ambas tablas. La clave de actividad es PK y la carga es FK.
- El front asigna automáticamente el único resultado por `fraccionEmpresa`,
  limpia carga y sugerencia al cambiar patrón, cancela respuestas anteriores y
  ofrece reintento ante falla técnica. No presenta tres opciones de demostración.
  Impide construir el registro mientras la consulta está en curso o con error;
  al construir el payload HINC utiliza el ID resuelto, no una carga de la precarga.
- Tras integrar el front actualizado (commit base `4f94085`), los días se consultan
  mediante `GuiasIttApiService` en `POST /msedi-expedicion/api/v1/guias-itt/calcular`.
  Los IDs 1/2/3 del catálogo se traducen a `liviano`/`moderado`/`pesado` para ese
  contrato. Se conservan diagnóstico, edad, diabetes e hipertensión; los días
  mostrados son los devueltos por la consulta, sin valores locales de demostración.

## Decisiones pendientes

- Confirmar cuales de los catalogos permitidos se utilizan en HU001-HU012 al cerrar el contrato de los 28 campos.


## Decisiones implementadas

- El micro no consume `msedi-bitacora` y no registra cada consulta exitosa para evitar una dependencia circular.
- Los errores tecnicos se conservan en el log operativo con `X-Correlacion-Id`.
- Cada clave delega a un metodo MyBatis con SQL explicito; no existe sustitucion de nombres de tabla desde la URL.
- Las consultas se ejecutan en transacciones de solo lectura y el pool JDBC se configura como `read-only`.


## Estado civil SIST (2026-10-20)

GET /api/v1/catalogos/estado-civil devuelve registros activos en orden institucional.
Claves e ids coinciden con SIST: 1 SOLTERO, 2 CASADO, 3 VIUDO, 4 DIVORCIADO, 5 UNIÓN LIBRE.
El frontend usa descripcion para mostrar y clave para estadoCivil. La clave se conserva como string numérico en el contrato interno y se resuelve a id_estado_civil (FK) al registrar la información clínica. La conversión al tipo numérico del contrato institucional corresponderá al adaptador real SIST.
Aplicar manualmente config/sql/20261020_catalogo_estado_civil_sist.sql desde la raíz EDI, como owner, antes de desplegar. No hay DDL ni carga automática desde la aplicación.
