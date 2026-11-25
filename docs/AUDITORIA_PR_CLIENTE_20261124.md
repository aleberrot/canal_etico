# Reauditoría de las PR contra lo pedido por el cliente

**Fecha:** 24/11/2026, America/Santiago. **Fuente:** las 20 láminas de `WEBETIKO_OBSERVACIONES_23112026.pptx` y las instrucciones de entrega del usuario. **Resultado:** candidata corregida para revisión; no desplegada.

Se revisaron frontend PR #6, inicialmente `9e73a963841565a81c5420f794f9bfe41d8d6fba`, y backend PR #5, inicialmente `deb573f3da199f4c3b61499a4e1608789d1f1c60`. Las correcciones de esta revisión se agregan a esas mismas PR. No se fusionaron ni se modificó producción.

## Dictamen y correspondencia funcional

Los cambios atacan las causas de las observaciones: sustituyen el día del servidor por instantes y presentación de Chile; habilitan Karin en servicio e interfaz; guardan un aviso por creación/aporte y utilizan los cuatro asuntos; recuperan el logo de la PPT y conservan la empresa al volver a Inicio. No son solamente ajustes visuales ni una habilitación de botones sin soporte backend.

| Pedido del cliente | Láminas | Evidencia comprobada | Estado |
| --- | --- | --- | --- |
| Día correcto en General y Karin | 1, 8 | `BusinessTime`, DTO y formateador frontend; medianoche, invierno/verano, correo decodificado y navegador en UTC | Implementado y Probado en candidata; históricos sin instante no se reescriben |
| Avisos de cada denuncia y cada aporte a tres destinos | 2, 3, 10–17 | Cola dentro de la transacción; cuatro asuntos exactos; CSV de ejemplo contrastado con los tres correos aprobados; MIME HTML/texto y destinatarios comprobados | Implementado y Probado con correo simulado; entrega SMTP real Pendiente |
| Aportes sucesivos de Karin, igual que General | 9 | Restricción retirada de servicio y condición de interfaz ampliada; dos aportes por tipo, archivo, historial y propiedad de descarga | Implementado y Probado; no existe un número fijo de aportes en el servicio |
| Nuevo logo sin reemplazar Mathiesen | 4, 7, 19 | Recurso original incrustado de la PPT, WebP con transparencia y componente reutilizable; cliente separado | Implementado y Probado en las pantallas locales |
| Título WEBETICO y subtítulo Canal de Denuncias | 6 | Portada renderizada; K preservada en el nombre y logotipo de marca | Implementado y Probado; la diferencia C/K es deliberada por la PPT |
| Inicio vuelve a la elección General/Karin de esa empresa | 5, 18 | Rutas por empresa, consulta, recarga, historial y sesión administrativa vencida | Implementado y Probado, incluida la corrección encontrada en esta revisión |
| Lámina de relleno | 20 | Solo contiene `xxxxxxxxxx` | No aplica; no se inventa un requisito |

La matriz individual de las 20 láminas se conserva en `WEBETIKO_REPORTE_CAMBIOS_2026-11-23.md`. Varias láminas reiteran los mismos eventos de correo: no se presentan como funcionalidades nuevas distintas.

## Defectos encontrados y corregidos

| Prioridad | Hallazgo comprobado | Corrección y regresión |
| --- | --- | --- |
| P1 | El seed de Mathiesen exigía correo antes de comprobar si el cliente ya existía. Con avisos aún sin configurar podía impedir arrancar una instalación existente | `MathiesenCompany` comprueba existencia antes de exigir correo para un alta nueva. Conserva el correo ya almacenado y el catálogo sin sobrescribir ediciones. La prueba nueva falló antes del cambio y pasa después |
| P1 | `NOTIFICATION_FROM` vacío podía dejar el mensaje sin From explícito, usando la identidad de la máquina. Además, un usuario SMTP sin formato de buzón se consideraba configuración suficiente | `EmailService` utiliza un remitente válido explícito o el buzón de autenticación SMTP; sin ambos conserva eventos pendientes. Dos pruebas fallaron antes y pasan después. No se envió correo real |
| P2 | Al cambiar de empresa y vencer la sesión, el panel volvía al contexto original de la URL y no al cliente seleccionado | Una referencia de empresa activa conserva el contexto sin recrear callbacks ni recargar el panel en bucle. Chrome simula un 401 local tras seleccionar emp-02 y verifica Inicio hacia ese canal |

También se retiró ruido de cambios de fin de línea en tres archivos React para que el diff muestre las modificaciones efectivas. No se hizo una refactorización general. La observación de configuración del logo se ajustó a WebP; la imagen no se recreó.

## Pruebas añadidas y resultados de esta revisión

- **Java:** 50 casos encontrados, 49 aprobados, cero fallos y un arnés omitido por diseño en la suite normal. El arnés se ejecutó por separado y terminó aprobado. Compilación y JAR aprobados.
- **Frontend:** 57 pruebas aprobadas, TypeScript estricto y build productivo HTTPS aprobados. La configuración existente rechaza compilar sin origen HTTPS de API: no se acepta silenciosamente localhost para producción.
- **Chrome local conectado:** 9 comprobaciones agrupadas aprobadas, con Spring Boot/H2, reloj fijo y dispositivo UTC. Incluye ambas denuncias, 17 sucursales, cuatro aportes, descarga byte a byte, administración, navegación/historial, móvil y pérdida de sesión.
- **Notificaciones del ensayo:** 6 eventos persistidos y 6 enviados al mock: dos creaciones y cuatro aportes. No se enviaron avisos a los destinatarios reales.
- **Cobertura reforzada:** tres buzones únicos de configuración (contrastados manualmente con la PPT), destinatarios exactos del mensaje simulado, fecha de Chile en HTML y texto plano, remitente, arranque existente sin SMTP y rechazo de archivos mayores a 50 MiB tanto iniciales como aportes Karin antes de almacenamiento/persistencia.

Las tres pruebas comparativas inicialmente fallidas corresponden al arranque existente y a los dos casos de remitente. El caso de sesión vencida se comprobó con una regresión nueva de navegador. No se presentan como aprobadas pruebas de Safari, SMTP real, R2 real o PostgreSQL productivo.

## Seguridad, alcance y límites que siguen abiertos

No se colocaron credenciales de administrador en el selector ni se hicieron públicas las descargas. Se conservan la contraseña de denuncia y la búsqueda de aporte por denuncia e ID. La notificación contiene solo metadatos aprobados; la cola no almacena identidad, descripción, contraseña o archivo.

La deduplicación por evento evita duplicar avisos en la cola. SMTP sigue teniendo una ventana entre aceptación del proveedor y commit: no se promete exactamente una entrega ante caídas o aceptación parcial. Es un límite documentado, no un duplicado de la denuncia causado por SMTP.

Antes de producción permanecen **Pendientes**: verificar las variables efectivas y los tres destinatarios en el servicio, validar SQL aditivo en PostgreSQL de staging, probar SMTP y privacidad real R2 con autorización, rotar claves históricamente versionadas y aprobar la publicación. Safari/iOS y parámetros actuales JVM/PostgreSQL no se verificaron en este entorno. Los riesgos anteriores de CAPTCHA solo cliente, limitación de intentos y sesión administrativa persistente siguen documentados; esta auditoría no certifica seguridad absoluta.

Sucursales y alojamiento no se reclasifican como correcciones originales ni se cambia infraestructura facturable. La aceptación técnica no cambia los acuerdos de V3 ni el saldo anterior.

## Condición de aceptación

**La candidata responde al pedido funcional y queda respaldada por código y regresión. El cierre productivo aún requiere validación del entorno y del cliente.** Revisar las PR [frontend #6](https://github.com/aleberrot/canalEticov6/pull/6) y [backend #5](https://github.com/aleberrot/canal_etico/pull/5); no desplegar sin confirmación explícita.
