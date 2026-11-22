# Sucursales — ampliación según PPT 15/11/2026

## Alcance implementado

Láminas 8–9: selector obligatorio «Dónde ocurrió el caso?» en denuncias GENERAL y LEY_KARIN de Mathiesen. Catálogo de 17 nombres literales, incluidos Mathiesen Mexico, Mathiesen CAM y Empresa Argentina CDV S.A. Los códigos son identificadores internos generados; la PPT no especifica códigos.

Administración por empresa: crear, renombrar, activar/desactivar. El código es estable. No se eliminan sucursales ni se reasignan denuncias. Listado público solo de sucursales activas y empresa activa; administración autenticada con el rol ADMIN existente. Se rechazan códigos de otra empresa, códigos inexistentes, sucursales inactivas y selección vacía cuando la empresa tiene sucursales; Mathiesen siempre requiere selección. Empresas sin sucursales conservan compatibilidad. El cliente bloquea el envío si falla el catálogo.

La denuncia guarda la referencia y una copia del nombre al crearla. El detalle administrativo, tabla y seguimiento autenticado muestran esa copia histórica. Las denuncias anteriores permanecen sin ubicación informada. El borrador se reinicia al cambiar de empresa o iniciar otra denuncia.

## Base de datos

22/11/2026: migración aditiva aplicada en Aiven canal-etico-pg/defaultdb/public. Tabla sucursales, columnas denuncias.sucursal_id y sucursal_nombre, unicidad por empresa/código, índice y clave foránea compuesta sucursal/empresa. Carga confirmada: mathiesen, 17 sucursales, todas activas. No se actualizaron denuncias anteriores ni empresas de ejemplo. Se verificaron los nombres y restricciones en el editor SQL.

El editor limita a 10 sentencias por ejecución; se aplicó un lote de nueve sentencias con BEGIN/COMMIT. docs/sql/20261122_sucursales.sql conserva la versión idempotente para clientes SQL que acepten DO. El alta de arranque usa ON CONFLICT DO NOTHING y respeta desactivaciones/renombres posteriores.

## Contrato

- GET /api/empresas/{empresa}/sucursales: catálogo público activo.
- GET /api/admin/empresas/{empresa}/sucursales: catálogo completo, Basic ADMIN.
- POST /api/admin/empresas/{empresa}/sucursales: {codigo,nombre,activa}, 201.
- PUT /api/admin/empresas/{empresa}/sucursales/{codigo}: edición o cambio de actividad, 200.
- Crear denuncia agrega codigoSucursal; administración devuelve codigoSucursal/sucursalNombre; seguimiento autenticado devuelve sucursalNombre.

## Validación

24 pruebas Java aprobadas (UrgentCorrectionsTest, MathiesenCompanyTest, SucursalServiceTest y SucursalesIntegrationTest). En Windows se precargó byte-buddy-agent 1.17.8 con -DargLine=-javaagent:RUTA para evitar el bloqueo de auto-attach del entorno. No se modificó la aplicación para omitir pruebas. Se ejecutó el contexto completo de Spring Boot y MockMvc con H2 aislada: autenticación ADMIN, catálogo por empresa, creación/edición/desactivación, multipart GENERAL/LEY_KARIN, seguimiento con nombre histórico y rechazo de sucursales ajenas/inactivas. Correo y archivos se sustituyeron por mocks. La prueba preexistente de contexto con configuración externa no se ejecutó; la nueva integración arranca el contexto completo sin servicios reales.

51 pruebas frontend aprobadas, TypeScript estricto y compilación Vite aprobados. Cobertura de transporte multipart en ambos tipos, selección requerida, autenticación administrativa, errores de catálogo, aislamiento por empresa, desactivación y nombre histórico. No se crearon denuncias reales ni enviaron correos de prueba. Se completó integración de API/seguridad/persistencia en H2. La navegación completa de navegador con frontend y backend candidatos conectados y la validación de aceptación de la candidata quedan antes de la publicación del módulo.

## Publicación coordinada pendiente

Mantener esta ampliación en PR separadas de las correcciones urgentes. No fusionar automáticamente. Base de datos preparada; publicar backend y frontend coordinadamente tras validar la candidata. El backend nuevo exige la ubicación para Mathiesen: reducir al mínimo el intervalo con frontend antiguo. Comprobar GET sucursales=17 y ambos formularios. Para revertir aplicación, restaurar frontend y backend anteriores sin borrar tabla/columnas ni datos históricos. No bajar el esquema de producción.

## Nota para Christian (borrador, no enviado)

Las correcciones de datos reutilizados, carga de canales, logos, términos aprobados, seguimiento y adjuntos corresponden al cierre del desarrollo original. Sucursales es una ampliación funcional separada, con cambios en PostgreSQL, backend, formularios y administración; debe registrarse como adicional. El diagnóstico y eventual migración de alojamiento también se cotizan por separado. Permanecen pendientes los $150.000 CLP de la V3 y el saldo del desarrollo original; no se ha informado el monto de ese saldo ni se inventa aquí. Este documento no fija precio nuevo para sucursales ni modifica acuerdos comerciales.
