# WEBETIKO — Reporte de cambios y aceptación

**Requisitos:** PPT del 23/11/2026, 20 láminas. **Trabajo y validación:** 24/11/2026, America/Santiago. Se conserva el nombre de archivo solicitado. **Entrega:** candidata en PR; sin autorización ni ejecución de publicación productiva en esta revisión.

## Reauditoría de las PR

Se contrastó de nuevo la implementación con las 20 láminas. Se corrigieron tres defectos adicionales: arranque de Mathiesen existente sin correo configurado, remitente SMTP vacío y pérdida de empresa al vencer la sesión. Resultado actualizado: 49 pruebas Java, 57 frontend y 9 comprobaciones Chrome aprobadas. Detalle y límites en [AUDITORIA_PR_CLIENTE_20261124.md](AUDITORIA_PR_CLIENTE_20261124.md). No se desplegó en producción.

## Resumen ejecutivo

Se implementaron fechas de negocio coherentes con Chile, aportes sucesivos para Ley Karin, cuatro clases de notificación centralizadas y persistentes, el logo nuevo recuperado de la PPT y navegación `Inicio` que conserva la empresa. Se mantienen términos aprobados, formularios separados, sucursales, trazabilidad, archivos privados y administración.

Las observaciones funcionales están implementadas y probadas en la candidata, con los límites indicados abajo. **No están aceptadas ni cerradas en producción**: falta revisar las PR, confirmar configuración y migración en staging, autorizar la publicación y ejecutar aceptación final. No se usaron denuncias reales, no se modificó Aiven y no se enviaron correos reales.

## Leyenda de estados

- **Implementado:** cambio presente en archivos de la rama candidata.
- **Probado:** comprobación efectivamente ejecutada y con resultado registrado.
- **Pendiente:** acción necesaria antes de aceptación/operación final.
- **Bloqueado:** no se pudo validar con los accesos o entorno disponibles.
- **No aplica:** no hay requisito comprobable en la lámina o actividad.

Un requisito puede estar implementado/probado localmente y pendiente de validación productiva. Estas categorías no son equivalentes a aprobación legal o comercial.

## Repositorios, versión y PR

| Elemento | Referencia |
| --- | --- |
| Frontend base | `aleberrot/canalEticov6`, main `1e08622edbfe9df27698f7556e81b224e0f8fb11` |
| Backend base | `aleberrot/canal_etico`, master `70df3537c5f5c58ccc3c9a76b03b8884071d6c1c` |
| Rama de trabajo | `fix-observaciones-20261123` en ambos |
| PR frontend | https://github.com/aleberrot/canalEticov6/pull/6 |
| Commit funcional frontend | `b6e1c82ebc184bf9da0605587813a9d08de045f5` |
| PR backend | https://github.com/aleberrot/canal_etico/pull/5 |
| Commit funcional backend | `702a1804928e9ae8ca91393bfce91e59a78541ed` |

Se consultaron las ramas de ambos repositorios: no apareció una rama más reciente de WEBETIKO/Mathiesen que reemplazara estas principales. Se recuperaron del remoto los archivos relevantes para trabajar sobre su contenido actual, en un directorio de candidata separado del ensayo anterior.

## Estado observado de producción

El 24/11 la página principal devolvió HTML con servidor Vercel y su bundle apunta a `https://canal-etico-api-w9dm.onrender.com`. Los endpoints públicos de las tres empresas respondieron 200 y Mathiesen conserva 17 sucursales. GitHub registra éxito del despliegue Vercel del main de referencia, actualizado el 23/11 a las 05:03:43 UTC.

La última lectura directa de Render del 23/11 mostró el backend funcional `2c3f3ce`. Los commits posteriores de master solo añadieron QA/documentación. Esta revisión no pudo renovar el SHA interno del proceso ni leer variables actuales del dashboard, parámetros JVM o `SHOW timezone` de PostgreSQL. No se inventa esa comprobación a partir de un endpoint público.

La infraestructura conocida es Vercel/Render, PostgreSQL en Aiven, R2 y SMTP. Solo Vercel, el endpoint Render y los catálogos se comprobaron de nuevo por HTTP en esta sesión. R2/SMTP y parámetros internos quedan pendientes de comprobación operativa. Oracle Cloud no se da por vigente. No se cambiaron planes ni servicios facturables.

## Matriz de las 20 láminas

| Lámina | Prioridad y estado original | Solución y archivos principales | Estado de candidata / límite |
| --- | --- | --- | --- |
| 1 | P0: General mostraba 24/11 para una creación del 23/11 | `BusinessTime`, nuevos instantes en `Denuncia`, DTO, formateador común y panel | Implementado y Probado; no se reescribió la fecha histórica |
| 2 | P0: aviso nuevo con un solo destinatario y otro asunto | Configuración central, `NotificationQueue`, `EmailService` | Implementado y Probado con mocks; envío real Pendiente |
| 3 | P0: los aportes no generaban aviso | Evento por UUID de aporte y cola transaccional | Implementado y Probado; activación SMTP Pendiente |
| 4 | P1: portada con logo anterior | PNG original incrustado en PPT, convertido a WebP y `WebetikoBrand` | Implementado y Probado; sin redibujar marca |
| 5 | P1: retorno genérico sin elección de denuncia | `navigation.ts`, consulta y retorno al canal con General/Karin | Implementado y Probado por navegación real local |
| 6 | P1: título/subtítulo anteriores | `Landing`: `WEBETICO` y `Canal de Denuncias`, empresa en línea propia | Implementado y Probado; marca WEBETIKO con K conservada |
| 7 | P1: logo antiguo en consulta | Componente reutilizable, nuevo recurso | Implementado y Probado |
| 8 | P0: fecha de Karin adelantada | Misma lógica de instantes y Chile en panel/seguimiento | Implementado y Probado con navegador en UTC |
| 9 | P0: Karin impedía agregar antecedentes | Eliminación de restricción en backend y habilitación de formulario; `AporteDenuncia` independiente | Implementado y Probado con dos aportes por tipo y archivo protegido |
| 10 | P0: secuencia creación/aporte sin cuatro avisos diferenciados | Dos clases de evento y asunto según tipo, tres destinatarios configurables | Implementado y Probado; no se usó el correo truncado de la anotación frente a la dirección completa aprobada |
| 11 | P1: formato de correo insuficiente | HTML con tabla de metadatos y alternativa texto plano | Implementado y Probado estructuralmente; clientes de correo reales Pendiente |
| 12 | P1: falta “Karin” en aviso de creación | Asunto exacto `Detalle de la Denuncia Karin` y encabezado equivalente | Implementado y Probado |
| 13 | P1: falta “General” en aviso de creación | Asunto exacto `Detalle de la Denuncia General` | Implementado y Probado |
| 14 | P0: cada aporte debía avisar a tres correos | CSV central, evento persistido y validación de destinatarios | Implementado y Probado; entrega real Pendiente |
| 15 | P1: aporte General sin tipo en asunto | `Nuevos antecedentes agregados a una Denuncia General` | Implementado y Probado |
| 16 | P1: aporte Karin sin tipo en asunto | `Nuevos antecedentes agregados a una Denuncia Karin` | Implementado y Probado |
| 17 | P0: aviso de cada denuncia a tres correos | Mismo flujo de cola para ambos tipos | Implementado y Probado; no contiene identidad, descripción, contraseña o documentos |
| 18 | P1: Inicio del acceso administrativo no llevaba al canal correcto | Contexto de empresa por ruta/consulta, selección inicial y retorno contextual | Implementado y Probado; raíz conduce al canal por defecto |
| 19 | P1: logo anterior en varias pantallas | Componente nuevo en portada, login, panel, consulta, estado, términos, formularios y confirmación | Implementado y Probado en Chrome local |
| 20 | Sin requisito visible: `xxxxxxxxxx` | No se atribuye una funcionalidad al texto de relleno | No aplica |

Las capturas, recursos y anotaciones se revisaron, además del texto extraído. No se copiaron a Git las capturas de denuncias del cliente. Los datos de los ejemplos de correo de la PPT no se usan como destinatarios o configuración del sistema.

## Fechas: causa, corrección y datos históricos

La entidad anterior almacenaba `createdAt`/`updatedAt` como `LocalDate`; el servicio usaba `LocalDate.now()` sin zona explícita. Esa combinación puede guardar el día UTC siguiente durante la noche chilena. El panel imprimía la fecha sin convertir y el seguimiento la construía como medianoche local. Los aportes sí tenían `Instant`, pero la interfaz los formateaba con la zona del navegador. No se obtuvo una lectura interna de la JVM productiva que permita certificar su zona actual.

Se añade `registered_at` y `last_activity_at` como instantes. Los nuevos registros mantienen los campos DATE compatibles, calculados mediante reglas `America/Santiago`. Hibernate y Jackson se configuran en UTC para los instantes. El mismo formateador de negocio se aplica a admin, seguimiento y aportes, y el equivalente Java al correo. No hay restas manuales de horas o días.

Para denuncias anteriores, el `Instant` de consentimiento se capturaba en la misma transacción de creación. Si existe, se utiliza como referencia temporal de presentación, sin sobrescribir el DATE original. Si no hay ni instante nuevo ni consentimiento, se conserva el día histórico y no se fabrica una hora. Los registros que necesiten una corrección adicional sin evidencia temporal quedan **Pendientes** de revisión autorizada; no se cambian todas las fechas al 23/11.

Se probó `2026-11-24T00:30:00Z` como **23/11/2026 21:30 en Chile**, ambos lados de medianoche, invierno y transición de verano. Las fechas de incidente y descargo siguen siendo fechas de calendario y no se desplazan por zona horaria.

## Correos: configuración, privacidad y fallos

`NOTIFICATION_RECIPIENTS` configura, sin duplicarlos en controladores, los tres destinos aprobados en la configuración central existente. La plantilla `.env.example` permite preparar la configuración; no se ha aplicado al servicio real. Se conserva compatibilidad con los nombres `MAIL_*` además de `SMTP_*`. No se cambió de proveedor SMTP sin comprobarlo.

La creación o aporte y su evento se guardan juntos. El dispatcher envía después, con bloqueo de eventos pendientes, clave única, contador y estados. SMTP fallido no fuerza al denunciante a repetir la operación. Se reintenta de forma acotada; al quinto fallo se detiene para revisión. Un `Message-ID` estable permite correlación. La cola no se precarga con denuncias históricas, evitando avisos retrospectivos indiscriminados.

La notificación incluye empresa, sucursal, código, tipo, fecha/hora de Chile, clase de evento y enlace HTTPS al panel. El HTML escapa contenido variable y el mensaje incluye alternativa de texto. No envía archivos, descripción, nombres de denunciantes, contraseñas o enlaces directos R2.

**Límite:** SMTP no garantiza exactamente una entrega. Existe una ventana si el proveedor acepta y el proceso cae antes de confirmar el estado, o si hay aceptación parcial por destinatario. Revisar el proveedor antes de un reenvío manual; no reiniciar toda la cola. Esto no duplica las denuncias o aportes persistidos.

## Archivos y endpoints afectados

Frontend: `Landing.tsx`, `Tracking.tsx`, `ReportStatus.tsx`, `SubmitReport.tsx`, `AdminLogin.tsx`, `AdminDashboard.tsx`, `LeyKarinInfo.tsx`; componentes `WebetikoBrand`, `TermsGate`, `AportesHistory`; `businessTime.ts`, `navigation.ts`, tipos en `api.ts`, rutas, CSS, branding estático, pruebas, README, configuración y lockfile npm.

Backend: `Denuncia`, DTO administrativo/seguimiento, `DenunciaService`, descripción OpenAPI del endpoint de aportes; nuevos `BusinessTime`, `TimeConfiguration`, `NotificacionDenuncia`, `NotificacionRepository`, `NotificationQueue`, `NotificationDispatcher`, `NotificationScheduler`; `EmailService`, perfiles de configuración, seed compatible, pruebas, README, plantilla y SQL.

No se añade un endpoint público para leer notificaciones. `POST /api/denuncias/{codigo}/informacion-adicional` ahora acepta Karin con las mismas comprobaciones de contraseña y archivo. Las respuestas administrativa y de seguimiento añaden `createdAtInstant` y `updatedAtInstant` nullable; conservan fechas compatibles. Los endpoints de descarga no pierden sus controles ni sus cabeceras de seguridad.

## Base de datos y activación

`operations/20261124_observaciones.sql` es una migración aditiva, con transacción. Añade dos columnas nullable a denuncias, tabla de avisos y un índice de pendientes. No elimina ni convierte registros existentes; no necesita inventar sucursales. Las reglas y catálogo anteriores permanecen.

En producción se cambia Hibernate a `validate`: aplicar y revisar la migración antes de arrancar la nueva API. La migración se preparó y la persistencia se ensayó con H2 PostgreSQL-mode; **no se ejecutó contra PostgreSQL/Aiven real**. Esa validación en staging es Pendiente, no se presenta como aprobada por los tests en memoria.

## Pruebas y evidencia

| Comprobación | Resultado y alcance |
| --- | --- |
| Frontend automatizado | 57 aprobadas, cero fallos |
| TypeScript estricto | Aprobado con TypeScript 5.9.3 |
| Compilación producción | Aprobado con Vite 6.4.6 y API productiva HTTPS |
| Java, compilación y empaquetado | 50 casos encontrados: 49 aprobados, 1 arnés de navegador omitido por diseño en la suite normal; cero fallos |
| Fechas | 5 pruebas Java más escenarios frontend: medianoche, UTC, invierno/verano, consentimiento e históricos sin instante |
| Correo | Cuatro asuntos, MIME HTML/texto, destinos sin duplicados, ausencia de adjuntos; proveedor simulado |
| Persistencia/integración | Ambos tipos, múltiples aportes, avisos únicos, fallos SMTP, reintento, descarga con contraseña y pertenencia |
| Navegador conectado | 9 comprobaciones agrupadas aprobadas en Chrome headless; escritorio 1280 y móvil 375 px |
| Avisos de navegación local | 6 eventos persistidos y 6 entregados al mock; 2 creaciones y 4 aportes |
| Safari/iOS real | Bloqueado: no hay ese entorno disponible; no se marca aprobado |
| SMTP y R2 reales | Pendiente de prueba operativa autorizada; se usaron mocks |
| Migración PostgreSQL productiva | Pendiente de staging y autorización |

La navegación conectó React con Spring Boot real en localhost, H2 en memoria, reloj fijo y mocks de correo/archivos. El navegador estuvo en UTC y aun así mostró Chile correctamente. Incluyó aceptación de términos exclusivamente de prueba, creación General/Karin, 17 sucursales, dos aportes por tipo, descarga con contenido idéntico, administración, Inicio, recarga, historial y pantalla móvil de 375 px. No se escribieron datos en producción.

Las evidencias resumidas y reproducibles se incluyen en `docs/evidence/20261124_acceptance.json`. Los scripts y clases de test quedan en los repositorios. No se adjuntan dumps ni capturas de denuncias reales. Las capturas locales muestran datos sintéticos y se entregan solo como apoyo de revisión.

## Rendimiento e infraestructura

Muestra HTTP del 24/11, una petición GET por recurso desde esta sesión, sin credenciales ni datos de denuncias:

| Recurso | HTTP | Tiempo observado | Tamaño |
| --- | --- | --- | --- |
| HTML principal Vercel | 200 | 8.411 s | 510 bytes |
| Empresa emp-01 | 200 | 6.400 s | 130 bytes |
| Empresa emp-02 | 200 | 6.423 s | 80 bytes |
| Empresa Mathiesen | 200 | 6.303 s | 115 bytes |
| Catálogo Mathiesen | 200 | 6.475 s | 1.160 bytes / 17 entradas |

Son duraciones de solicitudes HTTP medidas en cliente, no tiempos de ejecución SQL ni percentiles. Incluyen red y no distinguen DNS, TLS o posible suspensión previa. Una sola muestra no demuestra el arranque frío ni el rendimiento habitual de los usuarios. Los intentos de lectura mediante otro servicio no dieron acceso al sitio; no se usan para afirmar que la web está caída.

La candidata conserva fragmentos diferidos para panel/seguimiento y no agrega fuentes externas. El logo se optimizó a WebP con transparencia: **76.514 bytes**, frente al PNG original de la PPT, sin redibujarlo. Resultado del build: JS inicial 295,50 kB / 96,22 kB gzip; CSS 102,31 kB / 17,16 kB gzip. Panel y seguimiento continúan diferidos.

No se afirma que React elimine el arranque frío. El plan Render observado el 23/11 podía suspenderse por inactividad; plan y condiciones actuales deben confirmarse antes de cotizar un cambio. Quedan Pendientes medidas repetidas en caliente, un ensayo controlado de inactividad y observabilidad de PostgreSQL. La posible migración sigue siendo un diagnóstico/cotización aparte; no se activó un servicio de pago.

## Auditoría de seguridad y regresión

Se preservan BCrypt, consentimiento/versionado, validación de sucursal por empresa, descarga por denuncia/contraseña, límites de 50 MiB, `no-store`, `nosniff` y Content-Disposition. Se verificó el aislamiento de ubicación y aportes ajenos. No se volvió a exponer una ruta de descarga para hacer funcionar el selector.

Se eliminaron credenciales embebidas de configuración de desarrollo y SQL detallado. **Rotación Pendiente:** los literales que estuvieron en Git y los archivos históricos requieren revisión/rotación fuera del código. No se publican sus valores en esta entrega. Retirar `.env` versionados y los lockfiles anteriores de la candidata evita incluir configuración productiva o que Vercel elija otra resolución; las versiones anteriores siguen recuperables en Git.

El análisis npm inicial indicó 3 dependencias con alertas (2 altas y 1 crítica). Se actualizaron React Router a 7.18.7 y Vite a 6.4.6, y se fijó tar 7.5.21 transitivo, sin migración de framework. La instalación final informó **0 vulnerabilidades** para 289 paquetes evaluados. Es un resultado del registro consultado, no prueba de ausencia de vulnerabilidades en todo el sistema ni un análisis SCA Java.

Referencias de soporte: [React Router](https://github.com/advisories/GHSA-337j-9hxr-rhxg), [Vite](https://github.com/advisories/GHSA-fx2h-pf6j-xcff), [node-tar](https://github.com/advisories/GHSA-r292-9mhp-454m). Varias alertas corresponden a modos servidor/RSC o herramientas de desarrollo, no se afirma una explotación comprobada de esta SPA estática.

Pendientes de seguridad ya identificados: CAPTCHA solo cliente, control de intentos/rate limiting, sesión Basic persistida en localStorage, antivirus/inspección de archivos y cuentas administrativas por cliente si se requieren. No se hizo un pentest. El administrador actual es global; no entregar esa cuenta como si aislara a un cliente.

## Instrucciones de despliegue

1. Revisar las dos PR, confirmar aceptación de alcance y solicitar aprobación explícita para publicar commits concretos. No fusionar/publicar automáticamente esta candidata.
2. Confirmar servicio Render, origen Vercel, base real, almacenamiento y proveedor SMTP actuales; comprobar backup y restauración de staging. No copiar secretos a incidencias o PR.
3. Aplicar primero la migración en un PostgreSQL de staging y verificar columnas, índice, restricciones y lectura de registros antiguos. Corregir cualquier diferencia antes de producción.
4. Preparar `NOTIFICATION_RECIPIENTS`, `NOTIFICATION_FROM`, host/puerto/usuario/clave SMTP, URL administrativa, CORS y demás variables. Mantener `NOTIFICATIONS_ENABLED=false` en el primer arranque.
5. Con aprobación, aplicar SQL aditivo a producción y desplegar backend revisado. El arranque debe validar el esquema. No alterar plan Render ni migrar alojamiento.
6. Verificar catálogos, CORS, lectura de fechas y autorización. Habilitar avisos después de revisar destinos y proveedor; no generar denuncias de prueba en canales reales.
7. Publicar frontend con `VITE_CANAL_API_URL` HTTPS correcto y canal por defecto. Confirmar rutas directas/recarga y ambos logos.
8. Acordar y autorizar aparte cualquier prueba de correo real o aceptación del cliente. Registrar hora, commits activos, observaciones y resultado.

## Rollback

Desactivar primero el scheduler si se requiere detener avisos. Volver a frontend `1e08622` y backend funcional `2c3f3ce` o al commit operativo verificado antes del despliegue. Conservar las columnas y tabla nuevas: son aditivas y el código anterior puede ignorarlas. No borrar avisos pendientes, denuncias, aportes ni objetos R2 para revertir software.

Una restauración de base puede perder denuncias nuevas: exige una decisión explícita y coordinación con el responsable, no es el rollback automático de esta entrega. Revisar aceptación SMTP antes de reenviar eventos. No eliminar evidencias de investigación ni limpiar todo el historial para resolver una fecha.

## Checklist de aceptación final

- [x] Revisadas las 20 láminas, incluidos recursos y capturas.
- [x] Implementadas correcciones de fechas, Karin, avisos, branding e Inicio.
- [x] Conservados términos aprobados y validaciones especiales Karin.
- [x] Ejecutados tests locales y compilaciones; entregado arnés reproducible.
- [x] Probados aportes, descargas, navegación contextual y móvil en Chrome aislado.
- [x] Configuración de ejemplo sin secretos y documentación técnica integral preparada.
- [ ] Revisadas y aprobadas ambas PR por el responsable.
- [ ] Rotadas credenciales históricamente expuestas y revisados archivos antiguos antes de transferencia.
- [ ] Validada migración y configuración en PostgreSQL de staging.
- [ ] Confirmados SMTP, destinatarios/remitente, R2 privado y respaldo/restauración reales.
- [ ] Autorizado y registrado el despliegue productivo.
- [ ] Aceptación final del cliente en producción y prueba real autorizada de notificaciones.
- [ ] Mediciones representativas de carga activa/fría y comprobación Safari/iOS si forman parte de aceptación.

## Entrega del código y condiciones

Se preparan los repositorios con README, variables vacías, build/tests, lockfile y migración. Los Markdown son los originales completos de la entrega; una copia PDF de lectura no sustituye código, diagramas Mermaid o instrucciones. No se incluyen claves de acceso, dumps ni cesiones contractuales no pactadas.

Sucursales y alojamiento continúan separados de las correcciones del proyecto original. Se conserva el antecedente comercial de **$150.000 CLP de V3** y el saldo pendiente del desarrollo original, sin inventar monto para ese saldo ni nuevas cotizaciones. La validación técnica no implica condonar, recalcular o cambiar esos acuerdos.

