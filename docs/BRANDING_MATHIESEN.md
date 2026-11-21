# Identidad WEBETIKO y canal real de Mathiesen

Implementación basada en la PPT OBSERVACIONES CANAL DE DENUNCIAS_WEBETIKO_15112026 y en los tres PNG suministrados por el usuario. Los archivos se incorporan sin recortar, redibujar, recomprimir ni alterar sus colores; las proporciones se conservan con CSS.

## Ubicaciones

| Lámina | Pantalla | Implementación |
| --- | --- | --- |
| 1 | Inicio del canal | Logo del cliente y WEBETIKO horizontal, centrados |
| 2 | Aceptación de términos | Logo del cliente en la cabecera |
| 7 | Formulario GENERAL y LEY_KARIN | Logo del cliente junto al título |
| 11 | Acceso y panel administrativo | WEBETIKO vertical dentro del acceso; horizontal sobre blanco en la cabecera del panel |
| 12 | Estado autenticado de denuncia | Logo de la empresa de la denuncia; no se sustituye por el de otra empresa |

La consulta genérica muestra WEBETIKO. La confirmación de envío conserva la marca del cliente. Los canales de ejemplo emp-01 y emp-02 no se convierten en Mathiesen ni usan su logo.

## Archivos y configuración

- /marca/webetiko-horizontal.png: original image(9).png, 660 × 296.
- /marca/webetiko-vertical.png: original image(10).png, 452 × 316.
- /empresas/mathiesen/logo.png: original image(20261121-170823).png, 398 × 72.
- VITE_WEBETIKO_LOGO_URL sigue admitido como sustitución opcional. Si falla, se usa el archivo incorporado.
- El logo del cliente procede del registro de empresa devuelto por el backend; no se infiere desde códigos de denuncias ni se aplica un logo común a todos los clientes.

## Canal independiente

Código: mathiesen. Nombre: Grupo Mathiesen.
URL prevista al publicar ambos componentes: https://canaletico.absoftwareworks.com/canal/mathiesen .

El backend incluye un alta idempotente del registro usando el esquema existente. Copia app.notifications.denuncias-recipient, ya configurado en el backend, al campo de notificación del nuevo cliente. No contiene una nueva dirección literal ni cambia el destinatario global; no se ha enviado correo de prueba.

El alta ocurre al iniciar la nueva versión del backend. Puede deshabilitarse con app.clients.mathiesen.enabled=false; esa opción solo evita el alta, no desactiva ni modifica registros que ya existen. ON CONFLICT (codigo) DO NOTHING conserva las empresas ya registradas y no toca emp-01 ni emp-02.

## Verificación

- 42 pruebas frontend aprobadas sobre la base actual de main: incluyen marca correcta por cliente, ausencia del logo de Mathiesen en los ejemplos y variantes de administración.
- TypeScript estricto y compilación Vite aprobados.
- Revisión en navegador local, con API simulada de solo lectura: inicio y login administrativo en escritorio; inicio y términos en viewport de 375 × 812, sin desbordamiento horizontal ni distorsión de los logos.
- Las proporciones observadas en móvil coinciden con las originales: Mathiesen 5,5278 y WEBETIKO horizontal 2,2297.
- Los dos tests backend del alta están preparados, pero su ejecución está bloqueada porque Maven offline no puede resolver spring-boot-starter-parent:3.5.11. Validar con PostgreSQL y Maven conectado antes de publicar.

No se crearon denuncias, se alteraron servicios, se fusionaron ramas ni se desplegó a producción. La vista local de Mathiesen usa datos simulados y no demuestra que el registro exista ya en la base de datos real.

La corrección de arranque en frío del PR #2 del frontend es independiente; esta entrega solo incluye identidad y alta del cliente. El módulo de sucursales y una eventual migración de alojamiento siguen fuera del alcance.
