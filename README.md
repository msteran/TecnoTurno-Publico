# TecnoTurno

Proyecto académico de gestión de reparaciones para un taller tecnológico. La aplicación registra equipos y órdenes de servicio, asigna técnicos y conserva el historial de cada reparación.

## Perfiles

- **Administrador:** gestiona usuarios y equipos, registra órdenes y asigna técnicos.
- **Técnico:** consulta sus órdenes asignadas y registra diagnósticos, costos y avances.
- **Cliente:** consulta el estado y el historial de sus propias reparaciones.

## Ejecutar el proyecto

Necesitas **Java 17** y **Maven**. Descarga el código desde **Code → Download ZIP** y extráelo, o clona este repositorio. Desde la carpeta que contiene `pom.xml`, ejecuta:

```sh
mvn spring-boot:run
```

También puedes importar esa carpeta en Spring Tools mediante **File → Import → Maven → Existing Maven Projects** y ejecutar `TecnoTurnoApplication` con **Run As → Spring Boot App**.

Abre [http://localhost:8080](http://localhost:8080). El perfil predeterminado usa H2 y carga datos ficticios para la demostración, sin necesidad de configurar MySQL.

| Perfil | Correo de demostración |
| --- | --- |
| Administrador | `admin@tecnoturno.test` |
| Técnico | `tecnico@tecnoturno.test` |
| Cliente | `cliente1@tecnoturno.test` |

La contraseña de estas cuentas ficticias es `Tecno2026!`.

## Documentación y pruebas

- [Informe inicial en PDF](docs/Informe_Inicial_TecnoTurno.pdf)
- [Diagrama entidad relación](docs/diagramas/DER.png) y [demás diagramas](docs/diagramas)
- [Scripts de creación e inserción para MySQL](database)

El proyecto usa Spring Boot, Spring Security, Spring Data JPA, Thymeleaf y Bootstrap. Para ejecutar las pruebas, usa `mvn test`.
