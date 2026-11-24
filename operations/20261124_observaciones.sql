-- Apply with a privileged migration account after taking a verified backup.
-- Additive only: do NOT convert or rewrite existing created_at/updated_at DATEs.
BEGIN;
ALTER TABLE denuncias ADD COLUMN IF NOT EXISTS registered_at timestamp(6) with time zone;
ALTER TABLE denuncias ADD COLUMN IF NOT EXISTS last_activity_at timestamp(6) with time zone;
CREATE TABLE IF NOT EXISTS notificaciones_denuncia (
 id uuid PRIMARY KEY,
 evento varchar(100) NOT NULL UNIQUE,
 empresa varchar(255) NOT NULL,
 sucursal varchar(255),
 codigo varchar(255) NOT NULL,
 tipo varchar(255) NOT NULL,
 aporte boolean NOT NULL,
 ocurrido_at timestamp(6) with time zone NOT NULL,
 estado varchar(255) NOT NULL,
 intentos integer NOT NULL DEFAULT 0,
 proximo_intento timestamp(6) with time zone NOT NULL,
 enviado_at timestamp(6) with time zone,
 ultimo_error varchar(255)
);
CREATE INDEX IF NOT EXISTS idx_notificacion_pendiente ON notificaciones_denuncia(estado,proximo_intento);
COMMIT;
