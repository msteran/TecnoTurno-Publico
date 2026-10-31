-- Datos ficticios de demostración. Contraseña de las tres cuentas: Tecno2026!
-- Ejecutar después de 01_crear_base_datos.sql, una sola vez.
USE tecnoturno;
START TRANSACTION;
INSERT INTO usuarios (id,nombre,documento,email,telefono,password,rol,activo) VALUES
(1,'Administrador Demo','90000000','admin@tecnoturno.test','3000000000','$2a$10$b6K2w4txMnossj8S7TjO5.G4P1wDGQxSRQwEkt6CAxxqToUF/lmVi','ADMIN',TRUE),
(2,'Técnico Demo','90000001','tecnico@tecnoturno.test','3000000001','$2a$10$b6K2w4txMnossj8S7TjO5.G4P1wDGQxSRQwEkt6CAxxqToUF/lmVi','TECNICO',TRUE),
(3,'Cliente Demo','90000002','cliente@tecnoturno.test','3000000002','$2a$10$b6K2w4txMnossj8S7TjO5.G4P1wDGQxSRQwEkt6CAxxqToUF/lmVi','CLIENTE',TRUE);
INSERT INTO equipos (id,cliente_id,tipo,marca,modelo,serial,accesorios) VALUES
(1,3,'Portátil','Lenovo','IdeaPad','DEMO-001','Cargador'),
(2,3,'Teléfono','Samsung','Galaxy A','DEMO-002','Sin accesorios');
INSERT INTO ordenes (id,equipo_id,tecnico_id,estado,falla,diagnostico,costo,creada,actualizada,version) VALUES
(1,1,2,'RECIBIDO','El equipo no enciende','',0,'2026-10-31 09:00:00','2026-10-31 09:00:00',0),
(2,2,2,'EN_DIAGNOSTICO','La batería se descarga rápidamente','Evaluación de batería en curso',0,'2026-10-31 09:30:00','2026-10-31 10:00:00',1);
INSERT INTO historial (orden_id,responsable_id,estado,nota,fecha) VALUES
(1,1,'RECIBIDO','Recepción del equipo','2026-10-31 09:00:00'),
(2,1,'RECIBIDO','Recepción del teléfono','2026-10-31 09:30:00'),
(2,2,'EN_DIAGNOSTICO','Se inició revisión de consumo y batería','2026-10-31 10:00:00');
COMMIT;
