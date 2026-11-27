# WEBETIKO — Correcciones y candidata V2, 26/11/2026

## Resumen ejecutivo

Se preparó una candidata para el cierre antes del 28/11. Se reutilizaron la outbox y la relación AporteDenuncia existentes: las correcciones de la PPT del 23/11 ya estaban fusionadas, aunque eso no demostraba activación productiva. Se añadieron diagnóstico SMTP sin secretos, estado administrativo de la cola y configuración explícita de TLS. Se verificaron los cuatro eventos con transporte SMTP sintético real por loopback y persistencia en H2, además de tres aportes por cada tipo de denuncia en navegador.

**No se fusionó ni desplegó esta candidata. No se declara producción 100 % operativa.** El 26/11 el frontend respondió, pero la API de Render agotó los timeouts. No existe en esta sesión acceso conectado a logs, plan o variables del servidor. La causa exacta de los correos del cliente del 26/11 queda bloqueada por esa evidencia faltante; no se atribuye a una variable concreta ni se reenvían eventos reales.

## Fuentes y versiones verificadas

- Fuente prioritaria: WEBETIKO_OBSERVACIONES_V2__26112026.pptx, 12 láminas. Consultados texto e imágenes incrustadas disponibles; la lámina 12 contiene solo xxxxxxxxxx y no tiene imagen extraíble. Se conserva el diseño seguro de correo HTML/texto con metadatos; no se replica contenido privado de las capturas.
- PPT de 23/11, sus informes y documentación técnica existentes. Términos de la PPT previa conservados con la aprobación legal informada por el usuario.
- ENTREGA_CANAL_ETICO_V3.md (24/09): identificó AporteDenuncia 1:N y la restricción antigua de Karin. ENTREGA_CANAL_ETICO.md (17/09): referencia histórica de OCI/Nginx y del límite de 10 MiB, superada por V3; no describe la instalación actual.
- Enumeradas ramas remotas antes de trabajar. Frontend base `14ae5f82b773d33d12b3603cfbdb010a8fc53676`, `main`, aleberrot/canalEticov6. Backend base `91bdfac9d784e3219e61a41becc2a7ff54109e1e`, `master`, aleberrot/canal_etico. No se encontró una rama funcional más reciente que estas bases fusionadas.
- Rama nueva en ambos repositorios: `fix-cierre-v2-20261126`. Commits de implementación: backend `6f303f786bf7615916dc57301c99b54d721af523`; frontend `e0357fc162005b293e13b7431fdca2eb906f9dc7`. Las PR y el commit posterior de documentación se registran en GitHub. Las ramas históricas se conservan.
- Frontend público: bundle `index-CuNZRHrc.js`, API Render correcta y sin IP Oracle. Backend activo: SHA exacto sin verificar. El contrato público del 25/11 no tenía los campos de instante del master actual; el 26/11 no pudo releerse por timeout.

## Matriz de las 12 láminas

Los estados de implementación se refieren a la candidata local. La columna productiva impide confundirlos con entrega real a los buzones.

| Lámina | Pedido | Candidata | Producción |
| --- | --- | --- | --- |
| 1 | Avisos nueva General y Karin | Implementado y probado: dos eventos de creación, asuntos y commits independientes | Bloqueado: cliente reporta no recepción; faltan logs/entorno |
| 2 | Correo por cada aporte General | Implementado y probado: tres aportes, tres eventos | Implementado sin validar en servicios reales |
| 3 | Aportes Karin con n posibilidades y avisos | Implementado y probado: misma entidad 1:N, tres aportes, sin tope fijo | Bloqueado para certificar versión activa |
| 4 | Secuencia creación/aportes, cuatro asuntos y tres destinos | Implementado y probado con SMTP sintético; configuración central cotejada | Pendiente validar remitente, destinos y recepción real autorizada |
| 5 | Formato HTML diferenciado por tipo | Implementado y probado: HTML/texto, tabla y metadatos seguros | Implementado sin validar en cliente de correo real |
| 6 | Karin en encabezado de nueva denuncia | Implementado y probado | Implementado sin validar en buzón real |
| 7 | General en encabezado de nueva denuncia | Implementado y probado | Implementado sin validar en buzón real |
| 8 | Avisos por aporte para ambos tipos a tres buzones | Implementado y probado con direcciones sintéticas | Pendiente validación real autorizada |
| 9 | General en aviso de aporte | Implementado y probado | Implementado sin validar en buzón real |
| 10 | Karin en aviso de aporte | Implementado y probado | Implementado sin validar en buzón real |
| 11 | Cada denuncia nueva informa a tres destinos | Implementado y probado: lista central única, sin datos privados del reporte | Pendiente comprobar variables efectivas y entrega |
| 12 | xxxxxxxxxx | No aplica: no define comportamiento | No aplica |

## Hallazgos y causas

| Hallazgo | Causa comprobada y corrección | Límite de evidencia |
| --- | --- | --- |
| Falta de avisos productivos el 26/11 | Reportado por el cliente. Se verificó que master guarda creación/aporte y evento en una misma transacción y envía después por scheduler | No se verificaron configuración, plan, logs ni commit activo; causa productiva exacta bloqueada |
| Fallos de correo indistinguibles | Dispatcher guardaba únicamente SMTP_DELIVERY_FAILED; faltaban categorías de autenticación, conexión, DNS, TLS y timeout | Se corrige y prueba sin copiar mensajes de excepción |
| Configuración incompleta silenciosa | Dispatcher retornaba sin diagnóstico cuando faltaba proveedor/destinos/remitente | Ahora informa código seguro una vez por cambio y conserva pendientes; endpoint administrativo permite inspección |
| TLS implícito sin configuración explícita | STARTTLS estaba fijado y no se exponían flags para diferenciar 465 y 587 | Se añaden flags documentados; no se presume que el proveedor real use 465 |
| HTML sin fila explícita de evento | El asunto y la introducción diferenciaban el evento, pero faltaba su fila en tabla | Añadida fila Evento; se conservan contenido mínimo y escape HTML |
| Karin y avisos persistentes | Ya implementados en master por PR anterior; la entrega V3 histórica restringía Karin | Se reutilizan, se amplía cobertura a tres aportes y se valida commit real local; no se crea otro servicio o entidad |
| API no disponible en esta revisión | Cuatro GET concurrentes agotaron timeout y una comprobación posterior individual también | No basta para concluir Render Free, arranque en frío o fallo de PostgreSQL |

Render documenta bloqueo de puertos SMTP 25/465/587 para instancias Free. Es una hipótesis condicionada al plan efectivo, que no pudo leerse. Fuentes: https://render.com/docs/free y https://render.com/changelog/free-web-services-will-no-longer-allow-outbound-traffic-to-smtp-ports. No se contrató ni cambió alojamiento/proveedor.

## Modificaciones y endpoints

- EmailService: diagnóstico seguro de configuración, cantidad de destinos y fila Evento HTML. Se mantienen los cuatro asuntos exactos, la lista CSV única y Message-ID estable.
- NotificationDispatcher: categorías sanitizadas, aviso por cambio de configuración y logs que distinguen aceptación SMTP de recepción en buzón. Persistencia, máximo de cinco intentos y bloqueo pesimista conservados.
- NotificacionRepository: conteo por estado para operación. NotificationStatusController: GET `/api/admin/notificaciones/estado`, solo ADMIN, no-store, sin buzones ni secretos y sin operaciones de envío/reintento.
- application.properties y .env.example: SMTP_SSL_ENABLED, SMTP_STARTTLS_ENABLED y SMTP_STARTTLS_REQUIRED. Valores de producción sin modificar.
- LoopbackSmtp y SprintClosureIntegrationTest: servidor SMTP efímero 127.0.0.1, mensajes sintéticos, commits reales en H2, fallos y concurrencia. Ampliaciones en NotificationDispatcherTest, ObservacionesIntegrationTest y UrgentCorrectionsTest.
- Frontend tests/browser.e2e.cjs: tres aportes General y Karin. vercel.json: rama de candidata sin despliegue automático. No se modifican formularios o módulos que ya superaban las pruebas.
- operations/20261126_notification_diagnostics.sql: diagnóstico de esquema y conteos, de solo lectura, sin cuerpos de denuncias.

Se revalidan POST `/api/denuncias`, POST `/api/denuncias/seguimiento`, POST `/api/denuncias/{codigo}/informacion-adicional`, descargas autenticadas de aportes, PATCH de detalle/estado administrativo, estadísticas y catálogos por empresa. No cambia su contrato. El único endpoint nuevo es la consulta administrativa de estado de avisos, necesaria para el diagnóstico.

## Pruebas y evidencia

Resultados exactos y casos JUnit: `docs/evidence/20261126_validation.json`. Todos los datos son sintéticos. No se guardan ni copian denuncias reales.

- Backend: 57 casos encontrados, 56 aprobados y uno omitido por tratarse del servidor de navegador optativo; JAR compilado; el servidor de navegador se ejecuta aparte porque está deshabilitado por defecto. Ver conteos finales en evidencia.
- SMTP local: ocho avisos de escenario principal (una General, una Karin y tres aportes por tipo), cuatro asuntos exactos, tres destinatarios sintéticos y ocho Message-ID distintos. Correo sin identidad, contraseña, texto privado de reportes/aportes ni archivos. Fallo temporal 451 conserva los registros, respeta espera y reintenta sin recrearlos. Dos despachadores concurrentes no envían el mismo evento dos veces en H2.
- Seguridad: contraseña incorrecta, denuncia ausente, archivo ajeno y más de 50 MiB rechazados; descargas legítimas y administración comprobadas. Se conservan términos e identidad Karin. Descargo administrativo no se filtra al seguimiento; conclusiones, estados e historial permanecen.
- Frontend: 66 tests, TypeScript estricto y build Vercel. Dos escenarios de carga 1280/375 px con API simulada y sin AbortSignal.any/timeout, recuperación de 503 y selección de sucursal.
- Navegador funcional local: nueve grupos aprobados, creación General/Karin, 17 sucursales, tres aportes por tipo, descarga, administración, Inicio contextual, sesión vencida, móvil y no reutilización. Outbox del navegador: ocho eventos y ocho aceptaciones por el mock.
- Producción 26/11: HTML Mathiesen 200 (5,88 s), bundle 200 (5,32 s). API individual agotó timeout tras 70,04 s totales; las cuatro consultas iniciales tras 61,9 s. Son muestras desde este entorno, no un SLA ni una prueba de la causa. No se leyó contenido de denuncias reales.

### Reproducción

Backend: `mvn -B package` con JDK compatible y perfil de prueba incorporado. En este Windows se utiliza el agente Byte Buddy del repositorio Maven local mediante argLine para Mockito; no es configuración de producción. LoopbackSmtp requiere permitir sockets locales, nunca salida SMTP externa.

Frontend: `npm test`, `npm run check`, `node scripts/build-vercel.mjs`. Navegador de carga: `node tests/channel-loading.browser.cjs`, con CHROME_EXECUTABLE opcional. Funcional completo: iniciar AuditBrowserPreviewTest con canal.audit.preview=true, Vite apuntando a http://localhost:18081 y ejecutar `node tests/browser.e2e.cjs`. El marcador target/audit-stop detiene el servidor y genera evidencia de outbox. No apuntar ese ensayo a producción.

## Limitaciones y pendientes

- Obtener acceso de solo lectura a Render para plan, commit activo, logs de arranque/SMTP, estado del scheduler y nombres/presencia de variables. Se notificó el bloqueo durante el trabajo. No se piden secretos por chat.
- Verificar esquema y respaldo PostgreSQL con acceso autorizado. H2 no certifica locks, migración ni rendimiento de Aiven real. La migración aditiva de 24/11 ya existe; esta candidata no agrega otra.
- Confirmar R2 privado y controles del bucket; los adaptadores y autorización de descarga se probaron con almacenamiento simulado.
- Autorizar y acordar cualquier ensayo de recepción real. ENVIADA solo acredita aceptación por transporte; puede existir rebote, spam o fallo posterior. SMTP no garantiza exactamente una entrega ante caída entre aceptación y commit.
- Cinco fallos dejan FALLIDA; no existe reenvío automático infinito ni endpoint de reactivación. Revisar el proveedor antes de cualquier reenvío manual autorizado.
- Riesgo previo de credenciales históricamente versionadas: requieren rotación por el titular; no se copian valores a esta entrega. CAPTCHA servidor, rate limiting y cuentas por cliente siguen pendientes conocidos de seguridad; no se realizó un pentest productivo.
- No se valida Safari/iOS con una sesión real ni entrega en buzones. El ensayo de concurrencia usa H2 y se debe repetir en staging PostgreSQL.

## Despliegue controlado

1. Revisar PR y autorizar por separado fusión/despliegue. No fusionar esta candidata automáticamente.
2. Comprobar respaldo restaurable y commit activo; inspeccionar esquema con SQL de solo lectura. Aplicar operations/20261124_observaciones.sql únicamente si aún falta y con autorización de migración.
3. Desplegar backend en staging con perfil prod, TLS DB y configuración R2 privada; mantener NOTIFICATIONS_ENABLED=false mientras se revisan remitente, destinos, plan y transporte.
4. Verificar GET administrativo de estado, diagnóstico, versiones y cuatro casos sintéticos con SMTP simulado. Activar scheduler en staging de prueba y comprobar persistencia/reintentos.
5. Acordar validación productiva. Revisar correo real solo con autorización y confirmar recepción con los responsables; no generar avisos retroactivos ni reenviar masivamente reportes históricos.
6. Publicar la versión aprobada y revisar Vercel, Render, catálogos, fechas, aportes, adjuntos y cola. Registrar commits activos y resultado, sin secretos ni denuncias.

## Rollback

Desactivar primero el scheduler y volver a la última versión efectivamente verificada del backend/frontend. Las bases Git de esta candidata no deben confundirse con una versión productiva sana ya acreditada. Conservar tablas/columnas aditivas, denuncias, aportes, archivos R2 y eventos. No restaurar una base antigua sobre denuncias nuevas ni borrar pendientes. Coordinar compatibilidad de interfaz y backend; revisar aceptación SMTP antes de reintentar eventos FALLIDA. No modificar DNS, planes ni servicios para revertir código sin autorización.

## Checklist para Christian

- [ ] Confirmar los commits activos y configuración de Render; verificar disponibilidad sostenida.
- [ ] Validar creación General y Karin con sucursal y fecha/hora de Chile.
- [ ] Comprobar tres aportes sucesivos por tipo: texto, archivo y ambos; historial intacto.
- [ ] Verificar descargas legítimas y rechazo de contraseña/archivo ajenos.
- [ ] Comprobar investigación administrativa, descargo, conclusiones, estados y estadísticas.
- [ ] Revisar los cuatro asuntos y la lista central de tres destinos autorizados.
- [ ] Autorizar ensayo real sintético y confirmar recepción en cada buzón, además de aceptación SMTP.
- [ ] Confirmar que un fallo de correo no pierde denuncia ni aporte y deja evento diagnosticable.
- [ ] Comprobar Mathiesen, emp-01/02, términos, branding WEBETIKO, Inicio y móvil.
- [ ] Verificar respaldo, R2 privado, rotación de credenciales expuestas y rollback.
- [ ] Aprobar las PR y la versión candidata antes de producción.

Las sucursales y una eventual migración de alojamiento continúan como ampliaciones separadas del proyecto original. Esta corrección no cambia los $150.000 CLP pendientes de V3 ni el saldo original, ni constituye una nueva cotización. No se enviaron mensajes a Christian.
