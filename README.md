# WEBETIKO — backend

Java 17, Spring Boot 3.5.11, JPA, PostgreSQL y Spring Security. La candidata del 24/11 responde a la PPT del 23/11/2026. No ejecutar contra producción sin aprobación.

## Instalación local

Instalar Java 17, PostgreSQL y Maven o usar el wrapper incluido. Crear una base vacía. Definir las variables de `.env.example` en el entorno del proceso; Spring Boot no carga ese archivo automáticamente. Usar una contraseña administrativa propia. Para desarrollo mantener `NOTIFICATIONS_ENABLED=false` y `STORAGE_TYPE=local`; no configurar SMTP real en pruebas.

```sh
./mvnw spring-boot:run
./mvnw test
./mvnw package
```

En Windows usar `mvnw.cmd`. La suite usa H2 y mocks con perfil `test`; no requiere acceso a PostgreSQL, R2 o SMTP. El arnés de navegador se omite en una ejecución normal.

## Ensayo de navegador optativo

```sh
./mvnw -Dtest=AuditBrowserPreviewTest -Dcanal.audit.preview=true test
```

Esperar `AUDIT_BROWSER_READY`; arrancar el frontend en 5173 con `VITE_CANAL_API_URL=http://localhost:18081`. El arnés usa exclusivamente H2 y mocks, un reloj sintético y una cuenta local de prueba. Se detiene creando `target/audit-stop` o a los 25 minutos. Eliminar el marcador antes del siguiente ensayo. Al terminar escribe el conteo de avisos entregados al mock en `target/audit-outbox-evidence.json`.

Algunos equipos Windows restringen el autoacoplamiento de Mockito: ejecutar Maven con `-DargLine=-javaagent:RUTA/byte-buddy-agent-1.17.8.jar`, desde la dependencia resuelta por Maven.

## Activación de la candidata

1. Validar backup y configuración actuales, sin copiar secretos al repositorio.
2. Aplicar `operations/20261124_observaciones.sql` con una cuenta de migración.
3. Configurar destinatarios, remitente y SMTP. La aplicación conserva los alias `MAIL_*` además de `SMTP_*`.
4. Publicar manualmente el backend aprobado. En producción Hibernate valida el esquema y no lo modifica automáticamente.
5. Comprobar salud y configuración antes de activar el despachador; no enviar denuncias reales de prueba.
6. Publicar el frontend aprobado y ejecutar la aceptación acordada.

La migración es aditiva: no cambia las fechas históricas ni elimina denuncias o aportes. La cola guarda un aviso dentro de cada transacción; SMTP se ejecuta después. Cada evento tiene una clave única, estado y reintentos acotados. No se garantiza exactamente una entrega ante un fallo entre la aceptación SMTP y el commit; el procedimiento operativo está documentado.

## Documentación

Consultar `docs/WEBETIKO_DOCUMENTACION_TECNICA.md` y `docs/WEBETIKO_REPORTE_CAMBIOS_2026-11-23.md`. La entrega no incluye claves, respaldos con denuncias ni una cesión contractual de derechos o cuentas. Las credenciales antiguas que estuvieron versionadas requieren rotación fuera de Git.
