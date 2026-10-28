# msedi-catalogos

Base tecnica del microservicio de consulta de catalogos funcionales de EDI.

## Requisitos

- JDK 21
- Acceso de solo lectura a los catalogos permitidos de PostgreSQL EDI

## Configuracion minima

```text
urlConexionBd
userBd
passwordBd
jwtSecretKey
```

## Comandos

```powershell
.\mvnw.cmd test
.\mvnw.cmd spring-boot:run
```

- Health: `http://localhost:8082/msedi-catalogos/actuator/health`
- Swagger: `http://localhost:8082/msedi-catalogos/swagger-ui/index.html`

La aplicacion configura su pool JDBC en modo de solo lectura y no ejecuta DDL, migraciones ni carga automatica de datos.

## Endpoint

```http
GET /api/v1/catalogos/{claveCatalogo}
```

Requiere Bearer Token y `X-Transaccion-Id`. Solo permite las nueve claves cerradas en `CatalogoClave`.


## Perfiles y zona horaria

El perfil \`local\` es el predeterminado. Sus valores explicitos estan en
\`config/application-local.yml\`; este archivo se carga al ejecutar desde la raiz
del repositorio, se excluye de Git y no se empaqueta dentro del JAR.

Para OpenShift se usa \`src/main/resources/application-openshift.yml\`:

\`\`\`text
SPRING_PROFILES_ACTIVE=openshift
EDI_TIME_ZONE=America/Mexico_City
TZ=America/Mexico_City
\`\`\`

\`application.yml\` configura Jackson, la JVM de la aplicacion y cada sesion
PostgreSQL con \`America/Mexico_City\`. En OpenShift tambien se recomienda
\`TZ=America/Mexico_City\` para que el proceso tenga la zona correcta desde su
arranque.



## Referencias de mensajes mediante enum local

`common.enums.MensajeEdi` centraliza los códigos y textos de respaldo de este
micro. Los servicios, excepciones y publicaciones a bitácora utilizan sus
referencias; los códigos públicos y sus textos actuales se conservan.

Los resolutores existentes siguen consultando primero `catalogo.edic_mensaje`.
El enum aporta un respaldo para las rutas que ya lo utilizaban y para fallos
técnicos donde la base no está disponible. Las variantes `DETALLE` conservan
los diferentes contextos técnicos de un mismo código; no son códigos nuevos.
Los mensajes recibidos de otros servicios y las búsquedas dinámicas en base
siguen aceptando sus claves; el enum no se usa como una lista restrictiva.
El catálogo y enum numérico del motor de riesgos no se migran en esta etapa.
