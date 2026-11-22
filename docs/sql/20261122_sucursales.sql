-- Ampliación sucursales. Ejecutar en una transacción antes de publicar el backend.
DO $migration$ BEGIN
PERFORM set_config('lock_timeout','5s',true);
PERFORM set_config('statement_timeout','30s',true);
IF NOT EXISTS (SELECT 1 FROM empresas WHERE codigo='mathiesen') THEN RAISE EXCEPTION 'Mathiesen no existe'; END IF;
CREATE TABLE IF NOT EXISTS sucursales (
 id uuid PRIMARY KEY, empresa_id uuid NOT NULL REFERENCES empresas(id),
 codigo varchar(80) NOT NULL, nombre varchar(200) NOT NULL, activa boolean NOT NULL DEFAULT true,
 CONSTRAINT sucursales_empresa_codigo_key UNIQUE(empresa_id,codigo),
 CONSTRAINT sucursales_id_empresa_key UNIQUE(id,empresa_id)
);
ALTER TABLE denuncias ADD COLUMN IF NOT EXISTS sucursal_id uuid;
ALTER TABLE denuncias ADD COLUMN IF NOT EXISTS sucursal_nombre varchar(255);
 IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname='denuncias_sucursal_empresa_fk') THEN
  ALTER TABLE denuncias ADD CONSTRAINT denuncias_sucursal_empresa_fk
   FOREIGN KEY(sucursal_id,empresa_id) REFERENCES sucursales(id,empresa_id);
 END IF;
CREATE INDEX IF NOT EXISTS denuncias_sucursal_idx ON denuncias(sucursal_id);
INSERT INTO sucursales(id,empresa_id,codigo,nombre,activa) SELECT gen_random_uuid(),id,'mathiesen-argentina','Mathiesen Argentina',true FROM empresas WHERE codigo='mathiesen' ON CONFLICT (empresa_id,codigo) DO NOTHING;
INSERT INTO sucursales(id,empresa_id,codigo,nombre,activa) SELECT gen_random_uuid(),id,'mathiesen-bolivia','Mathiesen Bolivia',true FROM empresas WHERE codigo='mathiesen' ON CONFLICT (empresa_id,codigo) DO NOTHING;
INSERT INTO sucursales(id,empresa_id,codigo,nombre,activa) SELECT gen_random_uuid(),id,'mathiesen-cam','Mathiesen CAM',true FROM empresas WHERE codigo='mathiesen' ON CONFLICT (empresa_id,codigo) DO NOTHING;
INSERT INTO sucursales(id,empresa_id,codigo,nombre,activa) SELECT gen_random_uuid(),id,'mathiesen-colombia','Mathiesen Colombia',true FROM empresas WHERE codigo='mathiesen' ON CONFLICT (empresa_id,codigo) DO NOTHING;
INSERT INTO sucursales(id,empresa_id,codigo,nombre,activa) SELECT gen_random_uuid(),id,'mathiesen-espana','Mathiesen España',true FROM empresas WHERE codigo='mathiesen' ON CONFLICT (empresa_id,codigo) DO NOTHING;
INSERT INTO sucursales(id,empresa_id,codigo,nombre,activa) SELECT gen_random_uuid(),id,'mathiesen-mexico','Mathiesen Mexico',true FROM empresas WHERE codigo='mathiesen' ON CONFLICT (empresa_id,codigo) DO NOTHING;
INSERT INTO sucursales(id,empresa_id,codigo,nombre,activa) SELECT gen_random_uuid(),id,'mathiesen-paraguay','Mathiesen Paraguay',true FROM empresas WHERE codigo='mathiesen' ON CONFLICT (empresa_id,codigo) DO NOTHING;
INSERT INTO sucursales(id,empresa_id,codigo,nombre,activa) SELECT gen_random_uuid(),id,'mathiesen-peru','Mathiesen Perú',true FROM empresas WHERE codigo='mathiesen' ON CONFLICT (empresa_id,codigo) DO NOTHING;
INSERT INTO sucursales(id,empresa_id,codigo,nombre,activa) SELECT gen_random_uuid(),id,'mathiesen-uruguay','Mathiesen Uruguay',true FROM empresas WHERE codigo='mathiesen' ON CONFLICT (empresa_id,codigo) DO NOTHING;
INSERT INTO sucursales(id,empresa_id,codigo,nombre,activa) SELECT gen_random_uuid(),id,'mathiesen-chile','Mathiesen Chile',true FROM empresas WHERE codigo='mathiesen' ON CONFLICT (empresa_id,codigo) DO NOTHING;
INSERT INTO sucursales(id,empresa_id,codigo,nombre,activa) SELECT gen_random_uuid(),id,'mathiesen-brasil','Mathiesen Brasil',true FROM empresas WHERE codigo='mathiesen' ON CONFLICT (empresa_id,codigo) DO NOTHING;
INSERT INTO sucursales(id,empresa_id,codigo,nombre,activa) SELECT gen_random_uuid(),id,'empresa-argentina-cdv','Empresa Argentina CDV S.A.',true FROM empresas WHERE codigo='mathiesen' ON CONFLICT (empresa_id,codigo) DO NOTHING;
INSERT INTO sucursales(id,empresa_id,codigo,nombre,activa) SELECT gen_random_uuid(),id,'soolimpex','Soolimpex',true FROM empresas WHERE codigo='mathiesen' ON CONFLICT (empresa_id,codigo) DO NOTHING;
INSERT INTO sucursales(id,empresa_id,codigo,nombre,activa) SELECT gen_random_uuid(),id,'sercoin','Sercoin',true FROM empresas WHERE codigo='mathiesen' ON CONFLICT (empresa_id,codigo) DO NOTHING;
INSERT INTO sucursales(id,empresa_id,codigo,nombre,activa) SELECT gen_random_uuid(),id,'luval','Luval',true FROM empresas WHERE codigo='mathiesen' ON CONFLICT (empresa_id,codigo) DO NOTHING;
INSERT INTO sucursales(id,empresa_id,codigo,nombre,activa) SELECT gen_random_uuid(),id,'quimatic','Quimatic',true FROM empresas WHERE codigo='mathiesen' ON CONFLICT (empresa_id,codigo) DO NOTHING;
INSERT INTO sucursales(id,empresa_id,codigo,nombre,activa) SELECT gen_random_uuid(),id,'agrovet','Agrovet',true FROM empresas WHERE codigo='mathiesen' ON CONFLICT (empresa_id,codigo) DO NOTHING;
END; $migration$;
