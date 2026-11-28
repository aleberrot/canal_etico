# Corrección del arranque productivo — 28/11/2026

## Diagnóstico confirmado en Render

Workspace autorizado: My Workspace. Servicio canal-etico-api (`srv-dbnmki3m8hqs73cl517g`), repositorio aleberrot/canal_etico, rama master, Docker, plan de ejecución Free y auto-deploy por commit activado.

El deploy `dep-dc4pv50u01pc73evfc0g` de `1210baff0172d5432a6bc97923b54d881154c00d` figura update_failed. Los logs privados confirman `Schema-validation: missing column [last_activity_at] in table [denuncias]`. El servicio conserva como live `2c3f3ce28d123f37aa46516d802a3e79dbc3ad69`.

El usuario autorizó aplicar la migración y desplegar, y confirmó un respaldo disponible de Aiven de hace 23 horas. Esa disponibilidad fue informada por el usuario; no se ensayó restauración ni se copian datos reales a archivos de diagnóstico.

## Solución

Se empaqueta el mismo archivo `operations/20261124_observaciones.sql` en `BOOT-INF/classes/db/20261124_observaciones.sql`, mediante recursos Maven. Docker copia operations antes de construir. No se duplica ni se descarga SQL remoto al arrancar.

Spring SQL initialization puede aplicar ese recurso antes de crear EntityManagerFactory. Se conserva `spring.jpa.hibernate.ddl-auto=validate`, `spring.jpa.defer-datasource-initialization=false` y fail-fast. La migración permanece deshabilitada por defecto (`SCHEMA_MIGRATION_MODE=never`). El operador puede activar el mecanismo explícitamente con `SPRING_SQL_INIT_MODE=always` para el despliegue controlado.

La operación existente es aditiva y transaccional: dos columnas nullable de instante y una tabla/índice de notificaciones. No actualiza, elimina, convierte o backfillea denuncias, aportes ni fechas históricas. `IF NOT EXISTS` permite repetirla. La conexión es el mismo DataSource configurado por Render para Aiven; no se publican credenciales ni se usa la configuración local de desarrollo.

Referencia técnica consultada: https://docs.spring.io/spring-boot/3.5/how-to/data-initialization.html, sección Initialize a Database Using Basic SQL Scripts. Este mecanismo no reemplaza la validación ni introduce otro ORM o una segunda cola de notificaciones.

## Secuencia operativa

1. Con respaldo confirmado y pruebas aprobadas, guardar únicamente variables necesarias en Render, preservando las demás: `SPRING_SQL_INIT_MODE=always`, `SPRING_JPA_HIBERNATE_DDL_AUTO=validate` y `NOTIFICATIONS_ENABLED=false`. Mantener la lista central de tres destinatarios aprobados. No iniciar pruebas ni reenvíos SMTP reales.
2. Fusionar la PR con el recurso empaquetado y la corrección de fechaCreacion. Render tiene auto-deploy por commit: no disparar otro deploy duplicado tras el merge.
3. Seguir ese deploy hasta live. Comprobar el commit activo, ausencia de Schema-validation, API pública y OpenAPI con campos nuevos de instante y diagnóstico de notificaciones.
4. Cambiar `SPRING_SQL_INIT_MODE=never` y activar esa configuración mediante un redeploy sin cambios de código, verificando de nuevo live. Dejar el inicializador desactivado para arranques posteriores.
5. Resolver el transporte de correo con el plan/proveedor autorizado antes de activar NOTIFICATIONS_ENABLED. Render Free bloquea salida por 25/465/587; configurar destinatarios no acredita recepción.

Si el SQL falla, se detiene el arranque; no se ignoran errores ni se configura ddl-auto=update. Revisar el error concreto antes de intentar de nuevo. Si el despliegue no queda sano, el servicio anterior continúa conforme al mecanismo de rollout de Render. No borrar columnas o registros para revertir código.

## Validación

SchemaMigrationTest verifica que el recurso empaquetado exista, que dos ejecuciones preserven una denuncia sintética y sus fechas, que los nuevos instantes queden nulos sin inventar horas, que la tabla de outbox arranque vacía y que la activación siga siendo explícita con validación Hibernate conservada. Usa H2 en modo PostgreSQL; no sustituye la comprobación del arranque real en Aiven.

La suite completa y el contenido del JAR se validan antes de publicar. Los resultados y los IDs/estados finales de despliegue se registrarán en la PR para no generar otro despliegue únicamente por actualizar documentación.

## Límites de la entrega

No se leyeron denuncias productivas ni se enviaron correos reales. El respaldo de 23 horas no equivale a una restauración ensayada. Esta acción no cambia planes ni contrata servicios. Persisten los pendientes de validación de recepción SMTP, R2 real, seguridad previamente documentada y aceptación del cliente. Sucursales/alojamiento mantienen el alcance comercial separado y no cambian los saldos de V3 ni del proyecto original.
