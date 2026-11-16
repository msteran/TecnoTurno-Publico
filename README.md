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

## Cómo se desarrolló el inicio de sesión

1. **Spring Boot muestra el formulario.** El controlador `TallerController` atiende `GET /login` y devuelve la vista `login`. Thymeleaf renderiza la plantilla `templates/login.html` con los campos de correo y contraseña.
2. **Spring Security autentica al usuario.** La plantilla envía las credenciales por `POST /login`. La configuración `SecurityConfig` usa un `UserDetailsService` para buscar el usuario por correo en `UsuarioRepository`, recuperar su rol y comprobar que su cuenta esté activa. Spring Security compara la contraseña ingresada con el hash guardado mediante BCrypt.
3. **La sesión da acceso según el rol.** Si la autenticación es válida, Spring Security establece la sesión y redirige al panel principal. Las rutas de usuarios y equipos están reservadas al administrador. El servidor también comprueba qué órdenes puede consultar o modificar cada perfil.
4. **La interfaz informa el resultado.** Si las credenciales son incorrectas o la cuenta está inactiva, el formulario muestra un mensaje de error. El botón **Cerrar sesión** envía un `POST /logout`, finaliza la sesión y devuelve al formulario con un mensaje de confirmación.

Para comprobar el flujo, inicia la demo, entra con la cuenta de administrador y revisa el menú **Usuarios**. Cierra sesión, intenta entrar con una contraseña incorrecta y comprueba el mensaje. Después entra con la cuenta de cliente para ver las opciones disponibles para ese perfil.

## Documentación y pruebas

- [Informe inicial en PDF](docs/Informe_Inicial_TecnoTurno.pdf)
- [Diagrama entidad relación](docs/diagramas/DER.png) y [demás diagramas](docs/diagramas)
- [Scripts de creación e inserción para MySQL](database)

El proyecto usa Spring Boot, Spring Security, Spring Data JPA, Thymeleaf y Bootstrap. Para ejecutar las pruebas, usa `mvn test`.
