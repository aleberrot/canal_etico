# Envío desde la cuenta de Google mediante Gmail API

El backend puede transportar las mismas notificaciones MIME por Gmail API sobre HTTPS, manteniendo los cuatro asuntos, destinatarios centrales y cola transaccional existentes. No lee la bandeja de entrada, no mueve mensajes y no pide acceso a Drive. El único permiso requerido es `https://www.googleapis.com/auth/gmail.send`.

No hay cambios de base de datos, nuevos servicios de pago ni dependencias Maven. SMTP sigue siendo el transporte predeterminado para instalaciones que lo usan. Seleccionar Gmail API reemplaza el remitente SMTP de Spring y nunca intenta SMTP como alternativa.

## Configuración de Google a cargo del titular

1. Abrir https://console.cloud.google.com/ y crear o elegir un proyecto WEBETIKO. Habilitar Gmail API en APIs y servicios. Esta integración no requiere contratar un servicio de hosting ni activar facturación por iniciativa del código.
2. En Google Auth Platform, completar la marca y el correo de soporte del titular. Configurar la audiencia adecuada para la cuenta. Solicitar únicamente el permiso gmail.send. Para una prueba externa, registrar al titular como usuario de prueba.
3. Crear un cliente OAuth de tipo Aplicación web. Para obtener la autorización inicial usando la herramienta oficial de Google, registrar exactamente `https://developers.google.com/oauthplayground` como URI de redirección autorizada.
4. Abrir https://developers.google.com/oauthplayground/ . En su configuración activar **Use your own OAuth credentials** e ingresar allí el ID y secreto del cliente anterior. No usar las credenciales compartidas del Playground: no son las de WEBETIKO. No pegar secretos ni tokens en conversaciones, tickets o repositorios.
5. Autorizar únicamente `https://www.googleapis.com/auth/gmail.send` con la cuenta que enviará los avisos. Intercambiar el código por tokens. Esta autorización no envía correos. Transferir el refresh token directamente a las variables privadas del backend, junto con las credenciales del cliente. No usar un access token temporal como refresh token.
6. Antes de dar por cerrado el envío permanente, resolver el estado de publicación y los requisitos de verificación que Google muestre para esta aplicación y su uso. Los refresh tokens de aplicaciones externas en estado Testing con gmail.send expiran a los siete días. No entregar esa configuración como autorización duradera. Una vez resuelto el estado definitivo, volver a autorizar y actualizar el refresh token. Los tokens también pueden revocarse o caducar por otras políticas de Google.

El Playground es una herramienta manual de autorización, no el servicio que enviará los avisos. La aplicación no depende de una sesión abierta del navegador ni del conector Gmail de ChatGPT. Su conexión a Google usa las credenciales privadas del backend.

## Variables privadas del backend

| Variable | Valor o propósito |
| --- | --- |
| `NOTIFICATION_TRANSPORT` | `gmail-api` |
| `GMAIL_CLIENT_ID` | ID del cliente OAuth propio |
| `GMAIL_CLIENT_SECRET` | Secreto de ese cliente, almacenado únicamente como variable privada |
| `GMAIL_REFRESH_TOKEN` | Autorización offline del titular para gmail.send |
| `GMAIL_SENDER` | Dirección exacta de la cuenta que autorizó el envío |
| `NOTIFICATION_FROM` | Vacío para usar GMAIL_SENDER, o la misma dirección exacta |
| `NOTIFICATION_RECIPIENTS` | Conservar la lista central ya aprobada |
| `NOTIFICATIONS_ENABLED` | Mantener `false` durante la configuración; activar únicamente al validar el envío y los eventos pendientes |

El titular ingresa los valores privados directamente en el panel autorizado del proveedor. No se solicitan contraseñas de Google ni contraseñas de aplicación. Guardar variables puede reiniciar el servicio: comprobar que siga activo tras el cambio.

## Comprobación y activación

La ruta administrativa de estado continúa protegida y no revela direcciones, secretos ni tokens. La presencia de configuración no prueba que Google la haya aceptado: se requiere una prueba controlada de entrega con destinatario y contenido expresamente autorizados.

Primero revisar el número de eventos pendientes y fallidos; activar la cola puede enviar avisos históricos pendientes. No reabrir ni reenviar eventos fallidos automáticamente como parte de esta configuración. Acordar qué eventos deben enviarse antes de habilitar el dispatcher. No crear denuncias reales para probar ni reintentar envíos sin conocer su estado.

El transporte renueva access tokens en memoria con el refresh token, admite respuestas únicamente de los endpoints HTTPS fijos de Google y no sigue redirecciones. Nunca registra cuerpos OAuth ni errores devueltos por el proveedor. El código de diagnóstico indica falta de configuración, autorización revocada, fallo de autenticación, permiso rechazado, cuota, timeout o entrega fallida mediante constantes sanitizadas.

La aceptación de `messages.send` se registra como aceptada por el transporte; no acredita recepción en bandeja de entrada. Una desconexión después de aceptar un mensaje puede hacer que un reintento genere un duplicado: la API no ofrece una clave de idempotencia para este método. Se conserva el identificador de evento existente, sin prometer entrega exactamente una vez.

## Límites de una instalación gratuita

Gmail impone cuotas y límites de envío; esta solución es para avisos de bajo volumen, sin garantía de entrega o disponibilidad. Render Free bloquea SMTP pero permite HTTPS. También puede suspender el proceso por inactividad: la cola conserva los eventos en la base, pero no se procesa mientras la aplicación está suspendida. No se configuran pings artificiales, recursos de pago ni mecanismos para eludir límites del proveedor.

## Referencias oficiales consultadas

Validación local del cambio: 66 casos encontrados, 65 aprobados, cero errores o fallos y un servidor de navegador optativo omitido. Seis pruebas nuevas ejercen la construcción MIME de los cuatro avisos con tres destinatarios sintéticos, caché y renovación de tokens, autorización revocada, cuota, configuración incompleta y selección del transporte sin SMTP. El servidor HTTP de prueba escucha únicamente en 127.0.0.1. JAR compilado correctamente. No se contactó Google con credenciales reales ni se enviaron correos reales. La autorización y recepción productivas quedan pendientes.

- https://developers.google.com/workspace/gmail/api/guides/sending
- https://developers.google.com/workspace/gmail/api/reference/rest/v1/users.messages/send
- https://developers.google.com/workspace/gmail/api/auth/scopes
- https://developers.google.com/identity/protocols/oauth2/web-server
- https://developers.google.com/identity/protocols/oauth2#expiration
- https://render.com/docs/free
