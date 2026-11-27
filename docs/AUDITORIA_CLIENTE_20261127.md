# WEBETIKO — Auditoría de cierre del cliente, 27/11/2026

## Dictamen

**Las correcciones no están completamente listas en producción.** La interfaz pública funciona y las correcciones del sprint están fusionadas y probadas en los repositorios, pero el backend publicado todavía expone el contrato anterior. El usuario informa plan Render Free y un último despliegue que parece corresponder a `2c3f3ce`. La restricción de aportes Karin y el correo antiguo están presentes en el código de ese commit.

Se encontraron además una regresión de contexto al iniciar sesión administrativa y una fecha mal asignada en la respuesta de creación. Las correcciones se entregan en PR nuevas, sin fusionar ni desplegar. Este informe actualiza el estado operativo de los informes del 24, 25 y 26/11; sus resultados locales siguen siendo válidos para el código comprobado, pero sus estados históricos de candidata no describen el despliegue actual.

## Versiones y alcance de la evidencia

| Elemento | Evidencia |
| --- | --- |
| Frontend main | `303b58dc9ed5f0eb2530962585a1784bdaf89c8f`; PR #9 fusionada; Vercel confirma publicación completada |
| Backend master | `1210baff0172d5432a6bc97923b54d881154c00d`; PR #6 fusionada |
| Comparación con candidata probada | GitHub compara cero archivos distintos entre cada head probado y su merge actual |
| Frontend servido | Bundle `index-CuNZRHrc.js`, origen Render correcto, sin IP Oracle y sin WEBETICO con C |
| Backend servido | OpenAPI 200 sin createdAtInstant/updatedAtInstant ni `/api/admin/notificaciones/estado`; contrato anterior al master |
| Render informado por usuario | Plan Free; commit visible aproximado `2c3f3ce`. No se leyó directamente el dashboard autenticado ni las variables |
| Requisitos | PPT V2 26/11 (12 láminas), PPT 23/11 y correcciones originales; términos aprobados según conversación |

No se aceptaron términos, crearon denuncias, consultaron casos reales ni enviaron correos en producción. Las comprobaciones públicas fueron GET y navegación aislada. Las pruebas de negocio y SMTP usaron datos sintéticos, H2 y almacenamiento simulado.

## Hallazgos prioritarios

### P0 — Backend sin actualizar

El contrato observado hoy conserva el esquema de seguimiento sin los nuevos instantes y no anuncia el diagnóstico de notificaciones. Concuerda con el commit antiguo que el usuario ve en Render. Haber fusionado las PR no activó esos cambios en el proceso que responde a la aplicación.

Se inspeccionó `DenunciaService.java` del commit `2c3f3ce`: rechaza explícitamente los aportes cuando el tipo es LEY_KARIN y guarda aportes General sin generar su evento de notificación. `EmailService.java` de ese mismo commit utiliza el destinatario antiguo único, asunto «Nueva denuncia recibida» con código y SimpleMailMessage de texto plano. No implementa los cuatro asuntos, tres destinos y HTML del sprint actual. Estas son causas funcionales concretas del código antiguo, no un problema de caché visual.

El código actual de master elimina esa restricción, usa la relación AporteDenuncia existente, guarda un evento por creación/aporte dentro de su transacción y envía después mediante la outbox. Antes de activarlo hay que comprobar el esquema y aplicar únicamente la migración aditiva que aún falte. No se comprobó la migración productiva desde esta sesión.

### P0 — Transporte de correo en Render Free

El usuario confirma que la instancia utiliza Free. Render documenta bloqueo de salida por puertos 25, 465 y 587; el código configura 587 por defecto. No se leyeron el host y puerto efectivos, por lo que no se atribuye el fallo a unas credenciales concretas. Con SMTP por esos puertos, el bloqueo de infraestructura impide el transporte aunque la contraseña sea correcta.

Se debe acordar un transporte compatible: una instancia que admita el SMTP elegido o un proveedor por API HTTPS, con sus costos, dominio y remitente autorizados. No se eligió ni contrató ninguno durante esta auditoría. Una integración HTTPS sería trabajo de implementación posterior; no existe hoy en la aplicación.

Fuente oficial consultada el 27/11: https://render.com/docs/free, apartados Spinning down on idle y Other limitations. Render también indica suspensión tras 15 minutos sin tráfico y arranque de aproximadamente un minuto. Eso explica una posibilidad de demora inicial, pero no prueba la causa de cada timeout observado anteriormente ni justifica clasificar toda demora como arranque en frío.

### P1 — Empresa perdida al iniciar sesión administrativa

`AdminLogin` conservaba empresa en el enlace de entrada, pero navegaba a `/admin/dashboard` sin query. El panel seleccionaba entonces la primera empresa del catálogo. La prueba anterior sembraba Mathiesen primero y ocultaba el defecto.

Se reprodujo en navegador local con emp-01 primero: seleccionaba emp-01 en lugar de Mathiesen. La corrección usa la ruta central validada `adminDashboardRoute(company)` y conserva replace del historial. La nueva prueba verifica selección, URL e Inicio en escritorio 1280 px y móvil 375 px, también sin los métodos estáticos de AbortSignal. No cambia permisos: el rol administrativo global sigue siendo el existente.

### P1 — Fecha de creación confundida con la fecha del incidente

`DenunciaService.mapToResponse` asignaba `denuncia.getFechaIncidente()` al campo `CrearDenunciaResponse.fechaCreacion`. La respuesta podía devolver un día anterior o null, aunque la denuncia se hubiera registrado correctamente hoy. Es un defecto distinto del formato de fechas de seguimiento/listados y no implica que esos componentes consuman este campo del POST.

Se cambia a `denuncia.getCreatedAt()`, ya calculado en America/Santiago. La nueva prueba registra General y Karin con incidente el 20/11 y reloj 24/11 00:30 UTC: exige creación el 23/11 de Chile y preserva incidente el 20/11. Antes de corregir devolvía 20/11 y la prueba fallaba. No modifica fechas históricas ni la fecha del incidente.

## Matriz de requisitos originales y regresión

| Pedido | Repositorio/pruebas | Producción actual |
| --- | --- | --- |
| Códigos, claves y datos independientes | Pruebas de generación, transporte, reset y navegador aprobadas | No se crean denuncias reales para volver a comprobar persistencia |
| Carga emp-01/emp-02/Mathiesen | Carga compatible y error con reintento manual | Los tres abren; APIs 200 y CORS del dominio correcto |
| General y Karin con términos e identidad | Separación de formularios e identidad Karin conservadas | Ambos accesos y aceptación obligatoria verificados; envíos productivos no ejercidos |
| Aportes ilimitados por tipo | Tres por tipo probados, sin límite fijo en servicio | No cerrable con backend antiguo: `2c3f3ce` restringe Karin |
| Seguimiento e historial | Consulta, orden, texto/archivo/ambos y ownership probados | Pantalla de consulta abre; casos reales no consultados |
| Adjuntos hasta 50 MiB | Límite y descargas legítimas/ajenas probados localmente | R2 real y su configuración privada pendientes de inspección autorizada |
| Admin, descargo, conclusiones y estadísticas | Regresión backend aprobada; contexto de login corregido en nueva PR | Login abre; la nueva corrección aún no está publicada |
| Fechas America/Santiago | Instantes y pruebas de medianoche/verano/invierno aprobados; corrección adicional de fechaCreacion en PR nueva | DTO público aún anterior; requiere backend actualizado y la corrección nueva |
| Cuatro eventos y asuntos correctos | Ocho eventos principales por SMTP sintético, con tres destinatarios de prueba | No cumplido por código antiguo y bloqueo SMTP si se usan puertos habituales en Free |
| Logos y WEBETIKO | Recurso oficial y marca con K | Logos Mathiesen y WEBETIKO cargados; emp-01 conserva logo/banner; emp-02 es ejemplo sin logo propio |
| Sucursales | 17 entradas y selección General/Karin probadas | Catálogo remoto devuelve las 17 activas; no se modifica la base |
| Inicio contextual | Rutas por empresa probadas | Links públicos conservan Mathiesen; login se corrige en candidata |
| Estética y móvil | 1280 y 375 px en escenarios de carga | Portada y términos General/Karin sin desborde horizontal en el recorrido |

## Matriz PPT V2, 12 láminas

| Lámina | Pedido | Resultado para entrega |
| --- | --- | --- |
| 1 | Avisos por General/Karin nuevos | Implementado y probado local; bloqueado por backend/transporte productivo |
| 2 | Aviso por cada aporte General | Implementado y probado local; backend antiguo no genera ese evento |
| 3 | n aportes Karin y avisos | Implementado y probado local; commit antiguo rechaza Karin |
| 4 | Ciclo, cuatro asuntos y tres destinatarios | Implementado y probado local; configuración/entrega reales pendientes |
| 5 | HTML diferenciado | Implementado y probado local; commit antiguo usa texto plano |
| 6 | Karin en encabezado de creación | Implementado y probado local; backend activo no refleja la plantilla nueva |
| 7 | General en encabezado de creación | Implementado y probado local; mismo bloqueo de activación |
| 8 | Aviso por cada aporte de ambos tipos | Implementado y probado local; no cerrado en producción |
| 9 | General en asunto de aporte | Implementado y probado local; no cerrado en producción |
| 10 | Karin en asunto de aporte | Implementado y probado local; no cerrado en producción |
| 11 | Tres destinos en avisos nuevos | Lista central preparada y cotejada; variables efectivas y buzones pendientes |
| 12 | xxxxxxxxxx | No define requisito funcional |

## Pruebas y mediciones

El 26/11 se aprobaron 56 tests backend, con un servidor de navegador optativo ejecutado aparte, 66 tests frontend y nueve grupos funcionales. Hay evidencia de tres aportes por tipo, ocho eventos de outbox, fallo SMTP temporal 451 con persistencia, reintento y dos despachadores concurrentes. Se verificó identidad exacta de archivos tras la fusión. Tras el hallazgo de fechaCreacion se reejecutó la suite backend completa: 58 casos encontrados, 57 aprobados, cero fallos/errores y un servidor optativo omitido (ejecutado aparte en el sprint anterior). JAR compilado correctamente. Los resultados exactos se guardan en la evidencia de hoy. H2 no acredita migración, locks ni rendimiento de PostgreSQL real.

Hoy pasaron nuevamente los 66 tests frontend, TypeScript y build para la corrección nueva de login. Se añade una prueba que falla antes de corregir y pasa después con catálogo reordenado. No se declara que la simulación sin AbortSignal sea una prueba real de Safari/iOS.

Navegación pública nueva: Mathiesen abrió en 10,885 s incluyendo espera de imágenes; emp-01 en 0,576 s y emp-02 en 1,088 s en la misma sesión, con recursos y conexiones ya utilizados. No comparar esas muestras como si fueran mediciones independientes en frío. Las APIs medidas en paralelo tardaron 6,15–6,65 s desde este entorno. No son un SLA ni un benchmark de carga concurrente.

Se verificaron el nombre WEBETIKO, logos cargados, enlaces, términos obligatorios a 1280/375 px, contraseña vacía en consulta/login e Inicio contextual. No hubo errores JavaScript en ese recorrido. El catálogo remoto devolvió 17 sucursales y CORS coincidió con el dominio.

La evidencia pública y de la corrección se guarda en `docs/evidence/20261127_audit.json`. El resultado histórico completo del sprint permanece en `docs/evidence/20261126_validation.json`.

## Seguridad y límites de la auditoría

Los controles de contraseña, pertenencia de adjuntos, rol ADMIN, CORS y privacidad de los correos se comprobaron en el código probado. La protección de bucket, SMTP, backups y credenciales productivas requieren acceso a sus servicios. No se copian denuncias ni secretos a este informe.

Se mantienen pendientes ya documentados: CAPTCHA validado solo en cliente, ausencia de limitador de intentos identificado en backend y credencial Basic administrativa guardada en localStorage (Base64 es reversible). No se consultó almacenamiento del navegador del usuario. Son riesgos concretos del código actual; no constituyen una afirmación de intrusión o explotación. No se realizó un pentest productivo ni una nueva evaluación jurídica de términos ya aprobados.

Las credenciales históricamente versionadas requieren rotación por su titular. Retirar literales del código actual no demuestra esa rotación ni elimina el historial; no se copian sus valores a esta auditoría.

## Orden para cerrar

1. Confirmar en Render el SHA completo, rama y configuración de despliegue; el objetivo backend fusionado es `1210baff0172d5432a6bc97923b54d881154c00d`.
2. Verificar respaldo restaurable y esquema PostgreSQL con acceso autorizado; revisar `operations/20261124_observaciones.sql` y aplicarla solo si corresponde. No borrar historial ni recrear tablas.
3. Acordar transporte de correo y cualquier costo. Configurar tres destinos autorizados y remitente sin exponer valores. Mantener envíos detenidos mientras se verifica el entorno.
4. Publicar backend actualizado de forma controlada y verificar esquema OpenAPI, fechas y estado administrativo de outbox. Revisar los eventos históricos antes de reintentar; no generar un reenvío masivo.
5. Revisar y validar las PR nuevas de contexto administrativo y fecha de respuesta antes de activar sus correcciones. El backend fusionado de referencia requiere además esta corrección posterior de fecha.
6. Con autorización específica, ejecutar los cuatro eventos sintéticos en el entorno acordado y confirmar recepción en cada buzón. Aceptación SMTP y recepción son evidencias distintas. Verificar R2 privado y descarga autorizada.
7. Registrar commits activos y aceptación de Christian. Conservar rollback de código y datos aditivos; no restaurar una base antigua sobre nuevas denuncias.

Sucursales y eventual migración de alojamiento siguen separadas de correcciones originales. Permanecen los $150.000 CLP de V3 y el saldo original. Esta auditoría no cambia precios, plan ni proveedor y no envía mensajes a Christian.
