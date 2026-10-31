package co.edu.uts.tecnoturno.config;

import co.edu.uts.tecnoturno.model.*;
import co.edu.uts.tecnoturno.repository.*;
import co.edu.uts.tecnoturno.service.TallerService;
import co.edu.uts.tecnoturno.web.OrdenForm;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/** Carga local de ejemplos ficticios; nunca modifica registros existentes. */
@Configuration
@Profile("demo")
public class DemoData {
  @Bean
  CommandLineRunner seed(
      UsuarioRepository users,
      EquipoRepository equipos,
      OrdenServicioRepository ordenes,
      HistorialEstadoRepository historial,
      TallerService service,
      PasswordEncoder encoder,
      PlatformTransactionManager transactions,
      @Value("${tecnoturno.demo.datos-ampliados:true}") boolean ampliar) {
    return args ->
        new TransactionTemplate(transactions)
            .executeWithoutResult(
                tx -> {
                  if (users.count() == 0) {
                    String hash = encoder.encode("Tecno2026!");
                    Usuario admin =
                        usuario(
                            users,
                            "Administrador Demo",
                            "90000000",
                            "admin@tecnoturno.test",
                            Rol.ADMIN,
                            hash);
                    Usuario tecnico =
                        usuario(
                            users,
                            "Técnico Demo",
                            "90000001",
                            "tecnico@tecnoturno.test",
                            Rol.TECNICO,
                            hash);
                    Usuario cliente =
                        usuario(
                            users,
                            "Cliente Demo",
                            "90000002",
                            "cliente@tecnoturno.test",
                            Rol.CLIENTE,
                            hash);
                    Equipo e =
                        equipo(
                            equipos,
                            cliente,
                            "Portátil",
                            "Lenovo",
                            "IdeaPad",
                            "DEMO-001",
                            "Cargador");
                    OrdenForm form = new OrdenForm();
                    form.setEquipoId(e.getId());
                    form.setTecnicoId(tecnico.getId());
                    form.setFalla("El equipo no enciende");
                    service.crear(form, admin);
                  }
                  if (!ampliar) return;
                  Usuario admin =
                      users
                          .findByEmail("admin@tecnoturno.test")
                          .filter(u -> u.isActivo() && u.getRol() == Rol.ADMIN)
                          .orElse(null);
                  if (admin == null) return;
                  String hash = encoder.encode("Tecno2026!");
                  String[] nombres = {
                    "Laura Méndez",
                    "Andrés Ríos",
                    "Camila Torres",
                    "Daniel Pardo",
                    "Valentina Duarte",
                    "Sergio Molina",
                    "Natalia Salas",
                    "Mateo León"
                  };
                  List<Usuario> clientes = new ArrayList<>();
                  for (int i = 0; i < nombres.length; i++)
                    clientes.add(
                        usuario(
                            users,
                            nombres[i],
                            "9900000" + (10 + i),
                            "cliente" + (i + 1) + "@tecnoturno.test",
                            Rol.CLIENTE,
                            hash));
                  List<Usuario> tecnicos = new ArrayList<>();
                  users
                      .findByEmail("tecnico@tecnoturno.test")
                      .filter(u -> u.isActivo() && u.getRol() == Rol.TECNICO)
                      .ifPresent(tecnicos::add);
                  String[] especialistas = {"Sofía Vargas", "Julián Herrera", "Paula Castaño"};
                  for (int i = 0; i < especialistas.length; i++)
                    tecnicos.add(
                        usuario(
                            users,
                            especialistas[i],
                            "9900000" + (30 + i),
                            "especialista" + (i + 1) + "@tecnoturno.test",
                            Rol.TECNICO,
                            hash));
                  for (int i = 0; i < REPARACIONES.length; i++) {
                    String serial = String.format("VIVA-%04d", i + 1);
                    // La presencia del serial evita duplicar y conserva cualquier cambio posterior
                    // del usuario.
                    if (equipos.existsBySerial(serial)) continue;
                    Usuario
                        cliente =
                            (i % 4 == 0
                                ? users
                                    .findByEmail("cliente@tecnoturno.test")
                                    .filter(u -> u.getRol() == Rol.CLIENTE)
                                    .orElse(clientes.get((i - 1 + clientes.size()) % clientes.size()))
                                : clientes.get((i - 1 + clientes.size()) % clientes.size())),
                        tecnico = tecnicos.get(i % tecnicos.size());
                    if (!cliente.isActivo() || !tecnico.isActivo()) continue;
                    String[] datos = REPARACIONES[i];
                    Equipo equipo =
                        equipo(equipos, cliente, datos[0], datos[1], datos[2], serial, datos[3]);
                    Estado destino = Estado.values()[i % 6];
                    LocalDateTime ingreso =
                        LocalDateTime.now()
                            .minusDays(2 + i % 13)
                            .withHour(8 + i % 5)
                            .withMinute((i % 4) * 15)
                            .withSecond(0)
                            .withNano(0);
                    List<Estado> pasos = new ArrayList<>(List.of(Estado.RECIBIDO));
                    if (destino == Estado.CANCELADO)
                      pasos.addAll(List.of(Estado.EN_DIAGNOSTICO, Estado.CANCELADO));
                    else
                      for (int paso = 1; paso <= destino.ordinal(); paso++)
                        pasos.add(Estado.values()[paso]);
                    OrdenServicio o = new OrdenServicio();
                    o.setEquipo(equipo);
                    o.setTecnico(tecnico);
                    o.setEstado(destino);
                    o.setFalla(datos[4]);
                    o.setDiagnostico(destino == Estado.RECIBIDO ? "" : datos[5]);
                    o.setCosto(
                        destino == Estado.RECIBIDO ? BigDecimal.ZERO : new BigDecimal(datos[6]));
                    o.setCreada(ingreso);
                    o.setActualizada(ingreso.plusHours((pasos.size() - 1) * 2L));
                    ordenes.save(o);
                    for (int n = 0; n < pasos.size(); n++) {
                      Estado estado = pasos.get(n);
                      HistorialEstado h = new HistorialEstado();
                      h.setOrden(o);
                      h.setEstado(estado);
                      h.setFecha(ingreso.plusHours(n * 2L));
                      h.setResponsable(
                          estado == Estado.RECIBIDO
                                  || estado == Estado.ENTREGADO
                                  || estado == Estado.CANCELADO
                              ? admin
                              : tecnico);
                      h.setNota(
                          switch (estado) {
                            case RECIBIDO ->
                                "Equipo recibido con "
                                    + datos[3].toLowerCase()
                                    + ". Falla reportada: "
                                    + datos[4];
                            case EN_DIAGNOSTICO -> "Revisión técnica: " + datos[5];
                            case EN_REPARACION -> datos[7];
                            case LISTO ->
                                "Pruebas de funcionamiento completadas. Equipo listo para recoger.";
                            case ENTREGADO ->
                                "Equipo entregado; se explicó el servicio y se verificó el"
                                    + " funcionamiento con el cliente.";
                            case CANCELADO ->
                                "El cliente decidió no continuar con la reparación después de"
                                    + " recibir el diagnóstico.";
                          });
                      historial.save(h);
                    }
                  }
                });
  }

  private Usuario usuario(
      UsuarioRepository repo, String nombre, String documento, String email, Rol rol, String hash) {
    var existente = repo.findByEmail(email);
    if (existente.isPresent()) return existente.get();
    // Los números son ficticios. Si uno ya existe, se elige otro sin modificar la cuenta
    // registrada.
    while (repo.existsByDocumento(documento))
      documento = Long.toString(Long.parseLong(documento) + 1000);
    Usuario u = new Usuario();
    u.setNombre(nombre);
    u.setDocumento(documento);
    u.setEmail(email);
    u.setTelefono("300000" + documento.substring(Math.max(0, documento.length() - 4)));
    u.setPassword(hash);
    u.setRol(rol);
    u.setActivo(true);
    return repo.save(u);
  }

  private Equipo equipo(
      EquipoRepository repo,
      Usuario cliente,
      String tipo,
      String marca,
      String modelo,
      String serial,
      String accesorios) {
    Equipo e = new Equipo();
    e.setCliente(cliente);
    e.setTipo(tipo);
    e.setMarca(marca);
    e.setModelo(modelo);
    e.setSerial(serial);
    e.setAccesorios(accesorios);
    return repo.save(e);
  }

  // Tipo, marca, modelo, accesorios, falla, diagnóstico, costo COP y avance de reparación.
  private static final String[][] REPARACIONES = {
    {
      "Portátil",
      "ASUS",
      "VivoBook 15",
      "Cargador y funda",
      "Se apaga al conectar el cargador",
      "Pendiente de revisión de la alimentación",
      "0",
      "Revisión del circuito de alimentación."
    },
    {
      "Teléfono",
      "Samsung",
      "Galaxy A54",
      "Funda azul",
      "La pantalla no responde en la parte inferior",
      "Digitalizador con daño en la zona inferior; requiere cambio de módulo",
      "185000",
      "Cambio del módulo de pantalla y limpieza de conectores."
    },
    {
      "Portátil",
      "HP",
      "Pavilion 14",
      "Cargador original",
      "Trabaja muy lento y tarda varios minutos en iniciar",
      "Disco mecánico con sectores defectuosos; se recomienda SSD",
      "240000",
      "Instalación de SSD de 480 GB y migración del sistema."
    },
    {
      "Impresora",
      "Epson",
      "EcoTank L3250",
      "Cable de corriente y USB",
      "Imprime con líneas y colores incompletos",
      "Inyectores obstruidos por falta de uso",
      "75000",
      "Limpieza de cabezal y alineación de impresión."
    },
    {
      "Consola",
      "Sony",
      "PlayStation 4 Slim",
      "Cable de corriente y control",
      "Se calienta y hace demasiado ruido",
      "Ventilación obstruida y pasta térmica deteriorada",
      "110000",
      "Mantenimiento interno y cambio de pasta térmica."
    },
    {
      "Teléfono",
      "Apple",
      "iPhone 11",
      "Funda transparente",
      "No carga después de contacto con líquido",
      "Corrosión en placa y puerto; reparación sujeta a recuperación de componentes",
      "95000",
      "Limpieza de conectores y evaluación de placa."
    },
    {
      "Computador de escritorio",
      "Dell",
      "OptiPlex 7060",
      "Cable de corriente",
      "Enciende pero no muestra imagen",
      "Pendiente de revisión de memoria y salida de video",
      "0",
      "Comprobación de módulos de memoria RAM."
    },
    {
      "Tableta",
      "Lenovo",
      "Tab M10",
      "Cargador USB-C",
      "La batería dura menos de una hora",
      "Batería con capacidad reducida; requiere reemplazo",
      "135000",
      "Instalación de batería y calibración de carga."
    },
    {
      "Portátil",
      "Acer",
      "Aspire 5",
      "Cargador y maletín",
      "Algunas teclas escriben doble",
      "Teclado con desgaste y humedad en membrana",
      "145000",
      "Reemplazo del teclado y revisión de flex."
    },
    {
      "Monitor",
      "LG",
      "24MP400",
      "Base y adaptador",
      "La imagen parpadea después de unos minutos",
      "Adaptador de corriente inestable",
      "65000",
      "Sustitución del adaptador y prueba prolongada de imagen."
    },
    {
      "Teléfono",
      "Xiaomi",
      "Redmi Note 12",
      "Funda y cable USB",
      "El conector se mueve y la carga se interrumpe",
      "Puerto USB-C con soldaduras deterioradas",
      "85000",
      "Cambio del puerto USB-C y comprobación de carga rápida."
    },
    {
      "Impresora",
      "HP",
      "DeskJet 2775",
      "Cable de corriente",
      "No reconoce los cartuchos",
      "Contactos dañados en el carro; reparación superior al presupuesto disponible",
      "55000",
      "Revisión de contactos y mecanismo del carro."
    },
    {
      "Portátil",
      "Lenovo",
      "ThinkPad T480",
      "Cargador USB-C",
      "El ventilador suena al iniciar",
      "Pendiente de diagnóstico del sistema de refrigeración",
      "0",
      "Inspección del ventilador y conductos de aire."
    },
    {
      "Consola",
      "Microsoft",
      "Xbox One S",
      "Control y cable HDMI",
      "No detecta el control por conexión inalámbrica",
      "Módulo inalámbrico con funcionamiento intermitente",
      "125000",
      "Reemplazo del módulo inalámbrico y emparejamiento del control."
    },
    {
      "Computador de escritorio",
      "ASUS",
      "Prime H510",
      "Torre y cable de corriente",
      "Se reinicia al abrir programas exigentes",
      "Fuente de alimentación inestable bajo carga",
      "195000",
      "Instalación de fuente nueva y pruebas de estabilidad."
    },
    {
      "Tableta",
      "Samsung",
      "Galaxy Tab A8",
      "Funda tipo libro",
      "La pantalla está quebrada",
      "Cristal y panel con fractura; requiere módulo completo",
      "220000",
      "Reemplazo de pantalla y comprobación de respuesta táctil."
    },
    {
      "Portátil",
      "Dell",
      "Inspiron 3501",
      "Cargador original",
      "La bisagra derecha está suelta",
      "Anclaje de bisagra deteriorado; requiere refuerzo",
      "90000",
      "Reconstrucción de anclajes y ajuste de ambas bisagras."
    },
    {
      "Monitor",
      "Samsung",
      "SyncMaster S22",
      "Base y cable VGA",
      "Aparecen franjas verticales permanentes",
      "Falla del panel; el cliente no autoriza reemplazo por costo",
      "45000",
      "Comprobación del panel y de la placa de video."
    },
    {
      "Teléfono",
      "Motorola",
      "Moto G84",
      "Funda negra",
      "No se escucha el micrófono en llamadas",
      "Pendiente de revisión de micrófono y subplaca",
      "0",
      "Evaluación de micrófono y entradas de audio."
    },
    {
      "Impresora",
      "Canon",
      "PIXMA G3110",
      "Cable de corriente y USB",
      "No toma el papel de la bandeja",
      "Rodillos de arrastre desgastados",
      "80000",
      "Cambio de rodillos y limpieza de sensores de papel."
    },
    {
      "Portátil",
      "MSI",
      "Modern 14",
      "Cargador y funda gris",
      "La batería no aumenta del 2 por ciento",
      "Batería agotada; circuito de carga en buen estado",
      "210000",
      "Instalación de batería y prueba de autonomía."
    },
    {
      "Consola",
      "Nintendo",
      "Switch",
      "Dock y cargador",
      "El joystick izquierdo se mueve solo",
      "Desgaste del módulo analógico del Joy-Con",
      "70000",
      "Cambio del módulo analógico y calibración del control."
    },
    {
      "Computador de escritorio",
      "HP",
      "ProDesk 400",
      "Teclado y cable de corriente",
      "No se conecta a la red inalámbrica",
      "Adaptador Wi-Fi con falla de hardware",
      "60000",
      "Instalación de adaptador Wi-Fi y configuración de red."
    },
    {
      "Teléfono",
      "Huawei",
      "P30 Lite",
      "Funda roja",
      "Se apaga antes de llegar al 30 por ciento",
      "Batería deteriorada; el cliente solicita devolución sin reparación",
      "40000",
      "Diagnóstico de batería y revisión del consumo."
    }
  };
}
