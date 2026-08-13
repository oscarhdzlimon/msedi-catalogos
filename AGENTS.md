# AGENTS.md

## Fuente de verdad

Leer `docs/specs/README.md`, `spec-tecnica.md` y la matriz antes de modificar codigo.

## Flujo Spec Driven

- Actualizar la lista cerrada de claves antes de agregar un catalogo.
- Agregar consulta MyBatis explicita y pruebas.
- Probar registros activos, orden y lista vacia.

## Restricciones

- Solo consultas para Angular.
- Un catalogo por solicitud.
- No usar nombres de tabla recibidos del cliente.
- No usar sustitucion `${}` de MyBatis.
- No exponer evento, mensaje, sistema_origen, guia_recuperacion o transicion_estatus.
- No implementar altas, cambios o bajas.
- No agregar Feign Client.
- No usar Flyway, Liquibase, DDL ni inicializacion automatica de esquema o datos.
- La base Postgres del servidor es la fuente de verdad.
