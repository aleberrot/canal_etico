# Auditoría del Canal Ético — 23/11/2026

## Resultado

La candidata cubre las observaciones funcionales de la PPT OBSERVACIONES CANAL DE DENUNCIAS_WEBETIKO_15112026.pptx (13 láminas) y se validó con frontend y Spring Boot conectados a una base H2 aislada. Se corrigieron diseño, navegación del formulario en móvil, acciones sin adjunto y accesibilidad del detalle administrativo.

**Producción todavía no puede declararse cerrada:** el frontend de sucursales está publicado pero Render ejecuta un backend anterior. No se crearon denuncias ni se enviaron correos reales durante esta auditoría.

## Por qué no funciona el selector en producción

- GitHub backend master: 2c3f3ce28d123f37aa46516d802a3e79dbc3ad69, incluye sucursales; PR #3 fusionada.
- GitHub frontend main: 1e027f91d3b2073e48d405474985aa2605bf5e35; PR #4 fusionada. Vercel informa publicación completada.
- Render muestra como último commit activo d87cba09ed247a8682d6891f079fe6cb36852202, anterior al módulo.
- Abrir la API pública de sucursales provoca un desafío de autenticación (el navegador informa credenciales de autenticación no válidas). El backend viejo no incluye la ruta pública /api/empresas/{empresa}/sucursales y la política residual exige autenticación.
- No se debe añadir credenciales de administrador al selector ni permitir empresas/sucursales ficticias como alternativa. La corrección operativa es publicar el backend fusionado y comprobar el catálogo público sin autenticación.
- El mismo flujo, conectado al backend candidato, devolvió las 17 sucursales y creó correctamente ambas clases de denuncia en la base de prueba.
- Aiven quedó preparado el 22/11: 17 sucursales de Mathiesen, índice, unicidad por empresa/código y clave foránea compuesta. Esta auditoría no alteró los datos de producción. El ensayo de creación, renombre y desactivación se hizo exclusivamente en H2.

## Matriz contra la PPT

| Lámina | Pedido del cliente | Resultado en la candidata | Evidencia / límite en producción |
| --- | --- | --- | --- |
| 1 | Logo del cliente y WEBETIKO | Cumple: cabecera con ambos logos oficiales | Ambos observados en portada pública; tamaños y distribución mejorados en candidata |
| 2 | Canal personalizado por empresa | Cumple: empresa, nombre y logo de la respuesta pública | Mathiesen separado de emp-01/emp-02; ejemplos revisados sin datos ni logo de Mathiesen |
| 3 | Introducción con nombre de empresa | Cumple: texto aprobado con sustitución del nombre | Revisado para las tres empresas; contenido legal conservado |
| 4 | Acceso a términos y condiciones | Cumple mediante desplegable accesible | Apertura y cierre probados; no se inventó una URL externa que la PPT no proporciona |
| 5 | Texto completo de términos y botón Cerrar | Cumple: se revisó el contenido frente a la lámina | Texto integrado conforme a aprobación previa del responsable indicada por el usuario; esta revisión comprueba incorporación, no sustituye asesoría jurídica |
| 6 | Recomendación de Google Chrome al pie | Cumple: inicio del canal y puerta de términos | Texto literal presente |
| 7 | Logo del cliente al iniciar denuncia | Cumple en general, Karin, términos y confirmación | Navegación local comprobada; la producción queda bloqueada por la API vieja de sucursales |
| 8 | Selector Dónde ocurrió el caso en ambos tipos | Cumple y es obligatorio para Mathiesen | Bloqueado en producción hasta publicar backend; también se corrigió desplazamiento heredado en móvil |
| 9 | Las sucursales del Grupo Mathiesen | Cumple: 17 nombres literales de la PPT, con códigos internos estables | Catálogo inicial preparado en Aiven el 22/11; catálogo de prueba completo comprobado en ambos formularios |
| 10 | No reutilizar código/contraseña en teléfono ni en denuncia nueva | Cumple: formularios nuevos vacíos, nombres/autocomplete diferenciados y código nuevo por denuncia | Dos envíos locales dieron códigos distintos; tras cambiar de tipo y empresa, teléfono, contraseña e identidad aparecieron vacíos |
| 11 | WEBETIKO en panel administrativo | Cumple: vertical en acceso y horizontal en cabecera interna | Disposición, controles y tabla revisados también en móvil |
| 12 | Logo del cliente en seguimiento | Cumple: obtenido de la denuncia autenticada | Se verificaron el logo Mathiesen y su ubicación histórica en seguimiento |
| 13 | Sin observación adicional visible | No aplica | No se atribuyen requisitos a una lámina vacía |

## Pruebas funcionales realizadas en navegador

Entorno: interfaz real React/Vite, API real Spring Boot en localhost:18081, H2 en memoria, notificación por correo y almacenamiento externo sustituidos por mocks. Se contó con autorización del usuario para términos y CAPTCHA exclusivamente de prueba.

1. Apertura de Mathiesen con logos oficiales y texto correcto.
2. Términos inicialmente no aceptados; continuar deshabilitado; apertura y botón Cerrar operativos.
3. Denuncia general anónima de prueba, Mathiesen Chile, archivo sintético de 88 bytes y código nuevo.
4. Contraseña incorrecta rechazada en seguimiento.
5. Seguimiento correcto con descripción, logo y ubicación.
6. Primer aporte con archivo y segundo aporte sin archivo: ambos permanecen en el historial y el segundo no reutiliza el anterior.
7. Descarga pública autenticada del aporte: comparación byte a byte con el archivo original satisfactoria.
8. Denuncia Ley Karin nueva: identidad, teléfono, contraseña, archivo y sucursal anteriores no reaparecen. Solo se muestran las cuatro tipificaciones Karin.
9. La sucursal vacía impidió el envío de Karin con aviso nativo; al seleccionar Mathiesen Perú se creó un código distinto del general.
10. Acceso administrativo local, dos denuncias, estadísticas y sucursales correctas.
11. Descargo y conclusiones guardados; cambio de estado a En revisión reflejado en estadísticas y seguimiento.
12. Las conclusiones son visibles al denunciante; el texto de descargo reservado no aparece en el seguimiento público.
13. Alta de una sucursal sintética inactiva, renombre/desactivación de Chile y posterior reactivación; los nombres históricos de denuncias permanecen iguales. La API pública omitió las sucursales inactivas.
14. Cambiar de empresa en administración limpia los datos anteriores; emp-01 y emp-02 muestran cero denuncias en la base de prueba.
15. Empresas sin sucursales conservan formulario compatible; no se asigna Mathiesen a los ejemplos.
16. Escape cierra el detalle administrativo y devuelve el foco al botón de origen.
17. Salir elimina la sesión local; entrar de nuevo a la ruta administrativa redirige al acceso sin mostrar denuncias.

Las denuncias sintéticas de esta sesión solo existieron en memoria. El arnés se detuvo al terminar; no se copian esos registros a Aiven.

## Correcciones visuales y de interacción

- Portada con cabecera compacta para los dos logos, texto legible y acciones diferenciadas. En móvil las acciones preceden al bloque introductorio completo para evitar una pantalla inicial dominada por texto.
- Se mantienen los PNG originales. La marca horizontal solo recorta márgenes blancos mediante CSS; no se redibuja ni deforma el logo.
- Tarjetas, bordes, fondos, espaciados y tamaños de campos coherentes entre portada, términos, denuncia, acceso administrativo y seguimiento.
- Tabla administrativa contenida con desplazamiento horizontal propio, métricas en dos columnas móviles y catálogo de sucursales desplegable para no desplazar las denuncias hacia abajo.
- Descarga deshabilitada cuando una denuncia carece de respaldo; antes mostraba una acción que terminaba en error.
- Al entrar al formulario y mostrar la confirmación se restablecen desplazamiento y foco al encabezado. Antes el encabezado móvil quedó a -221 px y el selector parcialmente fuera de vista; después, encabezado a 156 px y selector a 361 px, visibles.
- Foco confinado dentro del diálogo administrativo, Escape y restauración de foco; botones de tabla y cierre de al menos 44 px.
- Estados legibles en seguimiento (En revisión, Ley Karin) en lugar de códigos internos con guiones bajos.
- Corrección de ocho erratas evidentes en etiquetas del catálogo general sin cambiar códigos ni descripciones legales.
- Errores y confirmaciones administrativas con anuncios accesibles.

Se comprobaron anchos de ventana 375, 900 y 1280 px; el contenido útil observado fue 360, 885 y 1265 px debido a las barras del navegador. En portada, formulario y administración móvil no hubo desbordamiento horizontal de la página. Las pruebas se realizaron en el navegador Chromium disponible; no se declara certificación en todos los navegadores ni una auditoría WCAG exhaustiva.

## Validación automática y rendimiento

- 53 pruebas frontend aprobadas, incluidas ausencia de descarga sin archivo y etiquetas de estado comprensibles.
- TypeScript estricto aprobado.
- Vite compila correctamente, con API de producción HTTPS y sin la página temporal de diagnóstico dentro de dist.
- 24 pruebas Java aprobadas de nuevo el 23/11: UrgentCorrectionsTest, MathiesenCompanyTest, SucursalServiceTest y SucursalesIntegrationTest.
- La integración usa contexto completo de Spring Boot, MockMvc y H2. No se ejecuta la prueba antigua de contexto que requiere configuración externa de producción.
- El arnés AuditBrowserPreviewTest permite repetir la navegación real con correo simulado; solo arranca con -Dcanal.audit.preview=true.
- El JS inicial compilado ronda 287 kB / 93.5 kB gzip; administración y seguimiento siguen en fragmentos diferidos. Se añadieron unos 6 kB CSS sin fuentes ni bibliotecas visuales externas nuevas.
- Se comprobó apertura funcional de emp-01 y emp-02, limpieza al cambiar de canal y comportamiento con catálogo vacío. **No se obtuvo una medición representativa de latencia HTTP ni percentiles de arranque frío en producción**: no se debe dar esa prioridad por cerrada por aprobar pruebas locales o por medir la duración de una llamada de automatización.
- Render informa que su instancia gratuita puede dormir y demorar más de 50 segundos. La tolerancia acotada de 90 segundos y el reintento manual ya existen en frontend; no eliminan el arranque frío. Un cambio de alojamiento sigue siendo diagnóstico/cotización aparte.

## Observaciones de seguridad y pendientes de aceptación

Estos puntos surgieron de la revisión del código, sin realizar ataques contra producción:

1. El CAPTCHA aritmético se verifica en el cliente; no debe considerarse una protección de servidor contra automatización. No se identificó limitación de intentos implementada en los repositorios para las operaciones públicas. Revisar protección contra abuso y fuerza bruta antes de escalar el servicio.
2. El acceso administrativo actual es un ADMIN global con HTTP Basic. El filtrado visual por empresa no equivale a cuentas y permisos independientes por cliente. Si se requieren administradores de cada cliente, ese aislamiento de cuentas no está implementado.
3. Las credenciales administrativas se guardan codificadas en localStorage. Esa codificación no las cifra ni protege de JavaScript ejecutado en el mismo origen. Debe revisarse el diseño de sesión para un uso de mayor exposición.
4. Notificaciones reales por SMTP y almacenamiento R2 no se ejercitaron de extremo a extremo: los casos locales usan mocks. Se conserva la configuración de destinatario que el usuario indicó; no se envió ningún correo a Christian.
5. Se verificaron incorporaciones de textos legales aprobados; procesos de conservación de datos, atención de denuncias y obligaciones de investigación fuera del software quedan fuera de la prueba técnica.

Los puntos 1–3 no deben ocultarse bajo un resultado genérico de «todo seguro». Tampoco se declaran requisitos adicionales de la PPT sin confirmación del alcance comercial.

## Entrega y activación

La base y el código funcional de sucursales ya estaban fusionados. Para que el selector funcione en el sitio actual hay que publicar el backend 2c3f3ce en el servicio existente; no se necesita inventar datos ni migrar alojamiento para resolver ese desfase. Después de la autorización de publicación: comprobar GET público de sucursales sin credenciales, 17 entradas, canal Mathiesen y ambos accesos; no enviar denuncias reales de prueba.

Los ajustes estéticos se preparan en una PR nueva con esta auditoría. No se fusionan ni publican automáticamente mientras se mantenga la instrucción del usuario de validar antes de producción. El código candidato fue validado localmente; la aceptación final del cliente y el cierre de los puntos pendientes no se sustituyen por un informe.

Sucursales continúa identificado como ampliación separada de las correcciones originales. La posible migración de alojamiento también es un adicional separado. No se cambian precios, saldos ni acuerdos comerciales en esta auditoría.
