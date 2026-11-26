-- Read-only operational check. Run only with authorized read access.
-- Does not select complainant identities, complaint bodies, addresses or secrets.
BEGIN TRANSACTION READ ONLY;
SELECT table_name, column_name, data_type
FROM information_schema.columns
WHERE table_schema = current_schema()
  AND ((table_name = 'denuncias' AND column_name IN ('registered_at','last_activity_at'))
       OR table_name = 'notificaciones_denuncia')
ORDER BY table_name, ordinal_position;
ROLLBACK;

-- Run the following separately only if notificaciones_denuncia exists.
BEGIN TRANSACTION READ ONLY;
SELECT estado, ultimo_error, count(*) AS total,
       min(ocurrido_at) AS evento_mas_antiguo,
       max(intentos) AS mayor_numero_intentos,
       max(enviado_at) AS ultima_aceptacion_transporte
FROM notificaciones_denuncia
GROUP BY estado, ultimo_error
ORDER BY estado, ultimo_error;
ROLLBACK;
