USE tecnoturno;
SELECT id,nombre,email,rol,activo FROM usuarios;
SELECT o.id,e.serial,u.nombre AS cliente,t.nombre AS tecnico,o.estado,o.costo
FROM ordenes o JOIN equipos e ON e.id=o.equipo_id
JOIN usuarios u ON u.id=e.cliente_id JOIN usuarios t ON t.id=o.tecnico_id;
SELECT h.orden_id,h.estado,h.nota,h.fecha,u.nombre AS responsable
FROM historial h JOIN usuarios u ON u.id=h.responsable_id ORDER BY h.orden_id,h.fecha,h.id;
-- Debe devolver cero filas: no debe haber equipos con dos ordenes abiertas.
SELECT equipo_id,COUNT(*) FROM ordenes WHERE estado NOT IN ('ENTREGADO','CANCELADO') GROUP BY equipo_id HAVING COUNT(*)>1;
