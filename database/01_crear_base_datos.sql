-- TecnoTurno | MySQL 8.0.16+ | Ejecutar una vez en una base nueva.
-- No borra tablas ni datos existentes. Collation coherente con las búsquedas.
CREATE DATABASE IF NOT EXISTS tecnoturno CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE tecnoturno;
CREATE TABLE usuarios (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 nombre VARCHAR(100) NOT NULL,
 documento VARCHAR(20) NOT NULL UNIQUE,
 email VARCHAR(120) NOT NULL UNIQUE,
 telefono VARCHAR(20) NOT NULL,
 password VARCHAR(100) NOT NULL,
 rol VARCHAR(15) NOT NULL,
 activo BOOLEAN NOT NULL DEFAULT TRUE,
 CONSTRAINT chk_usuario_rol CHECK (rol IN ('ADMIN','TECNICO','CLIENTE'))
) ENGINE=InnoDB;
CREATE TABLE equipos (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 cliente_id BIGINT NOT NULL,
 tipo VARCHAR(50) NOT NULL,
 marca VARCHAR(50) NOT NULL,
 modelo VARCHAR(80) NOT NULL,
 serial VARCHAR(80) NOT NULL UNIQUE,
 accesorios VARCHAR(300),
 CONSTRAINT fk_equipo_cliente FOREIGN KEY(cliente_id) REFERENCES usuarios(id)
) ENGINE=InnoDB;
CREATE TABLE ordenes (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 equipo_id BIGINT NOT NULL,
 tecnico_id BIGINT NOT NULL,
 estado VARCHAR(25) NOT NULL,
 falla VARCHAR(1000) NOT NULL,
 diagnostico VARCHAR(2000) NOT NULL,
 costo DECIMAL(12,2) NOT NULL DEFAULT 0,
 creada DATETIME(6) NOT NULL,
 actualizada DATETIME(6) NOT NULL,
 version BIGINT DEFAULT 0,
 CONSTRAINT fk_orden_equipo FOREIGN KEY(equipo_id) REFERENCES equipos(id),
 CONSTRAINT fk_orden_tecnico FOREIGN KEY(tecnico_id) REFERENCES usuarios(id),
 CONSTRAINT chk_costo CHECK(costo >= 0),
 CONSTRAINT chk_orden_estado CHECK(estado IN ('RECIBIDO','EN_DIAGNOSTICO','EN_REPARACION','LISTO','ENTREGADO','CANCELADO')),
 INDEX idx_orden_estado(estado), INDEX idx_orden_creada(creada)
) ENGINE=InnoDB;
CREATE TABLE historial (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 orden_id BIGINT NOT NULL,
 responsable_id BIGINT NOT NULL,
 estado VARCHAR(25) NOT NULL,
 nota VARCHAR(1000) NOT NULL,
 fecha DATETIME(6) NOT NULL,
 CONSTRAINT fk_historial_orden FOREIGN KEY(orden_id) REFERENCES ordenes(id),
 CONSTRAINT fk_historial_usuario FOREIGN KEY(responsable_id) REFERENCES usuarios(id),
 CONSTRAINT chk_historial_estado CHECK(estado IN ('RECIBIDO','EN_DIAGNOSTICO','EN_REPARACION','LISTO','ENTREGADO','CANCELADO')),
 INDEX idx_historial_fecha(orden_id,fecha)
) ENGINE=InnoDB;
