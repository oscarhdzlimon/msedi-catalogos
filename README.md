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

