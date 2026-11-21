# Auditoría de correcciones esenciales — 21/11/2026

Estado: candidata en revisión. No desplegar ni fusionar sin validación.

## Orden de trabajo confirmado

La auditoría usa las prioridades expresas de la conversación y las observaciones de la PPT del 15/11/2026. No se recuperó una lista adicional atribuible a Christian que cambie ese orden.

| Prioridad | Necesidad esencial | Estado de esta candidata |
| --- | --- | --- |
| P0 | No arrastrar código, contraseña, teléfono, datos ni adjuntos anteriores | Aislamiento por empresa y sesión de seguimiento; teléfono tel y contraseña nueva diferenciados; borrador reiniciado; protección contra respuesta tardía de envío. Interacción real con gestores de contraseñas pendiente |
| P0 | GENERAL y LEY_KARIN; seguimiento; adjuntos; administración | Transporte y renderizado cubiertos por pruebas; seguimiento cargado y cabecera administrativa corregidos. Integración real pendiente |
| P1 | Medir y optimizar empresas emp-01 y emp-02 | Rutas diferidas y espera máxima de 15 s; script de medición incluido. Los seis GET reales fallaron por red: no hay latencias válidas ni mejora de LCP demostrada |
| P1 | Logos WEBETIKO y cliente | WEBETIKO en inicio, login y cabecera administrativa; logo del cliente en estado autenticado; falta archivo gráfico oficial WEBETIKO y logo emp-02 |
| P1 | Textos y términos de última PPT | Introducción personalizada y términos accesibles tanto en inicio como antes del formulario; botón Cerrar; recomendación Chrome. Aprobación del responsable confirmada por el usuario |
| P1 | Candidata para validar | Pruebas y compilación documentadas. No se modifica main/master ni se despliega a producción |

## Fallos detectados en la candidata local anterior

1. El logo del seguimiento estaba en ContentBlock, donde report no existía. Una compilación Vite puede pasar pese a este error; la vista cargada fallaba en ejecución. Se movió al componente de detalle autenticado y se añadió regresión para descripción, conclusión y un único logo por cliente.
2. WEBETIKO estaba dentro del modal de administración. Se movió a la cabecera permanente y se corrigió el contraste del texto de respaldo.
3. La introducción y los términos de la PPT solo aparecían antes del formulario. Ahora también aparecen en el inicio, mediante el mismo componente de términos.
4. El seguimiento podía conservar contenido y respuestas tardías al cambiar de denuncia. Cada navegación monta una sesión nueva y las lecturas antiguas se descartan.
5. Una creación pendiente podía vaciar el borrador de un canal nuevo tras abandonar el anterior. Se ignora su actualización de contexto cuando el formulario ya se desmontó.
6. Una respuesta tardía de edición administrativa podía reabrir datos después de cambiar o recargar empresa. Se verifica la operación vigente antes de aplicarla.
7. El backend permitía omitir el consentimiento con null. Ahora exige true y versión no vacía antes de guardar; la interfaz actual ya envía ambos.
8. La ampliación de los códigos a 12 caracteres no era necesaria para la observación de la lámina 10. Se conserva el formato existente y la generación segura con comprobación de colisión. No se cambia la columna ni los códigos históricos.

## Evidencia y límites

- 39 pruebas frontend aprobadas: contrato de API, multipart, límites, errores, renderizado de GENERAL/KARIN, términos, administración y detalle de seguimiento con ambos logos sintéticos.
- Comprobación TypeScript estricta de los módulos alcanzables desde src/main.tsx: aprobada con TypeScript 5.9.3 y tipos React 18.3.28. Es adicional a la compilación Vite.
- Compilación frontend: aprobada con Vite 6.3.5. Los tests SSR no equivalen a una prueba interactiva de navegador, ni ejecutan PostgreSQL, correo o R2.
- 7 pruebas backend preparadas: datos/contraseñas independientes, Karin sin teléfono, contraseña incorrecta, colisión de código, consentimiento omitido, branding autenticado y adjunto ajeno. Pendientes de ejecución: falta resolver el parent Spring Boot 3.5.11 en el entorno offline.
- No se puede declarar cerrado el rendimiento de ambas empresas ni el flujo integrado. La reducción de tamaño del JS inicial es una optimización de compilación, no una medición de carga real.

## Validación necesaria antes de producción

1. Instalar con el archivo de bloqueo y ejecutar npm test y npm run build con el backend de validación configurado. En el entorno restringido se compiló con --configLoader runner.
2. Ejecutar mvnw.cmd -Dtest=UrgentCorrectionsTest test (o ./mvnw) con Maven conectado.
3. Probar GENERAL y LEY_KARIN consecutivas en ambas empresas, cambiando canal, pestaña y denuncia; comprobar teléfono, contraseña, archivos y código. Repetir con Chrome y gestor de contraseñas.
4. Verificar creación, seguimiento, aportes permitidos, descargas, estados y descargos con PostgreSQL, almacenamiento y buzón de pruebas aislados.
5. Medir emp-01 y emp-02 en frío y caliente y registrar HTTP, recursos y LCP. El script benchmark-companies.mjs solo hace GET públicos.
6. Aportar/configurar los logos faltantes y revisar móvil/escritorio. Aprobar funcional y visualmente antes de fusionar y desplegar.

## Alcance separado

Sucursales (PostgreSQL, Spring Boot, formularios y administración) y diagnóstico/migración de alojamiento permanecen como requerimientos adicionales. No se implementan en esta corrección. La comunicación comercial se mantiene fuera de esta documentación técnica pública.

## Publicación para revisión

La candidata se prepara en audit-essential-fixes-20261121, fuera de main/master, con PR en borrador. No autoriza fusión, auto-merge ni despliegue. Los pendientes de integración y marca enumerados arriba son condiciones de aceptación.

El primer commit del frontend incluye git.deploymentEnabled=false para esta rama específica en vercel.json. No modifica el comportamiento de las otras ramas.
El commit del backend usa [skip render] y el título del PR [skip preview] para evitar auto-deploy y previews de Render.
No se ejecutan comandos de despliegue ni se cambian servicios, DNS o bases de datos.

Referencias de configuración: https://vercel.com/docs/project-configuration/git-configuration , https://render.com/docs/deploys y https://render.com/docs/preview-environments .
