-- Mejoras OPCIONALES para gym_db (tu esquema funciona tal cual; esto solo agrega integridad y velocidad).
-- Ejecutar una sola vez. Requiere MySQL 8.0.16+ para que los CHECK se apliquen.
USE gym_db;

-- 1) Reglas de integridad que hoy solo valida la aplicación
ALTER TABLE pagos       ADD CONSTRAINT chk_pagos_monto      CHECK (monto > 0);
ALTER TABLE membresias  ADD CONSTRAINT chk_memb_fechas      CHECK (fecha_fin >= fecha_inicio);
ALTER TABLE rutina_ejercicio ADD CONSTRAINT chk_re_series   CHECK (series > 0 AND repeticiones > 0);

-- 2) Índices para las consultas del dashboard, asistencias y reportes
CREATE INDEX idx_membresias_estado_fin ON membresias (estado, fecha_fin);
CREATE INDEX idx_asistencias_fecha     ON asistencias (fecha_entrada);
CREATE INDEX idx_pagos_fecha           ON pagos (fecha_pago);

-- 3) Estado de membresía sin valor nulo (hoy permite NULL si se inserta explícitamente)
ALTER TABLE membresias MODIFY estado ENUM('ACTIVA','VENCIDA','CANCELADA') NOT NULL DEFAULT 'ACTIVA';
