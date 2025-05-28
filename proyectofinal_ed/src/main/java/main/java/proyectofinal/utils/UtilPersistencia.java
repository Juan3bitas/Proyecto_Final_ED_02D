package main.java.proyectofinal.utils;

import java.io.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

import main.java.proyectofinal.excepciones.OperacionFallidaException;
import main.java.proyectofinal.modelo.*;

public class UtilPersistencia {
    private static UtilPersistencia instancia;
    private UtilProperties utilProperties;
    private UtilLog utilLog;
    private List<Usuario> listaUsuariosCache = new ArrayList<>();
    private List<Contenido> listaContenidosCache = new ArrayList<>();
    private List<SolicitudAyuda> listaSolicitudesCache = new ArrayList<>();
    private List<GrupoEstudio> listaGruposCache = new ArrayList<>();
    private List<Reporte> listaReportesCache = new ArrayList<>();


    // ✅ Constructor sin carga automática de datos
    private UtilPersistencia() {
        this.utilProperties = UtilProperties.getInstance();
        this.utilLog = UtilLog.getInstance();
        //inicializarDatos();
        utilLog.escribirLog("✅ UtilPersistencia instanciada.", Level.INFO);
    }



    public static synchronized UtilPersistencia getInstance() {
        if (instancia == null) {
            instancia = new UtilPersistencia();
            instancia.inicializarDatos();
        }
        return instancia;
    }

    public void inicializarDatos() {
        cargarUsuarios();
        cargarContenidos();
        cargarSolicitudes();
        cargarGrupos();
        cargarReportes();
        utilLog.escribirLog("✅ Datos iniciales cargados correctamente.", Level.INFO);
    }


    // ==================== MÉTODOS DE USUARIO ====================
    public Usuario buscarUsuarioCorreo(String correo) {
        try {
            utilLog.escribirLog("Buscando usuario por correo: " + correo, Level.INFO);
            return listaUsuariosCache.stream()
                    .filter(u -> u.getCorreo().equalsIgnoreCase(correo))
                    .findFirst()
                    .orElse(null);
        } catch (Exception e) {
            utilLog.escribirLog("Error buscando usuario: " + e.getMessage(), Level.SEVERE);
            return null;
        }
    }


    public void guardarUsuarioArchivo(Usuario usuario) {
        try {
            Objects.requireNonNull(usuario, "El usuario no puede ser nulo");
            utilLog.escribirLog("Intentando guardar usuario: " + usuario.getNombre(), Level.INFO);

            // Validar campos obligatorios
            if(usuario.getId() == null || usuario.getId().isEmpty()) {
                usuario.setId(UtilId.generarIdAleatorio());
            }

            listaUsuariosCache.add(usuario);
            guardarTodosUsuarios();

            utilLog.escribirLog("Usuario guardado exitosamente: " + usuario.getId(), Level.INFO);
        } catch (Exception e) {
            utilLog.escribirLog("Error crítico guardando usuario: " + e.getMessage(), Level.SEVERE);
            throw new PersistenciaException("Error al guardar usuario", e);
        }
    }

    public Usuario buscarUsuarioPorId(String usuarioId) {
        try {
            utilLog.escribirLog("Buscando usuario por ID: " + usuarioId, Level.INFO);
            return listaUsuariosCache.stream()
                    .filter(u -> u.getId().equals(usuarioId))
                    .findFirst()
                    .orElse(null);
        } catch (Exception e) {
            utilLog.escribirLog("Error buscando usuario: " + e.getMessage(), Level.SEVERE);
            return null;
        }
    }

    public void actualizarUsuario(Usuario usuario) {
        try {
            utilLog.escribirLog("Actualizando usuario: " + usuario.getNombre(), Level.INFO);
            listaUsuariosCache.removeIf(u -> u.getId().equals(usuario.getId()));
            listaUsuariosCache.add(usuario);
            guardarTodosUsuarios();
        } catch (Exception e) {
            utilLog.escribirLog("Error actualizando usuario: " + e.getMessage(), Level.SEVERE);
        }
    }

    public void eliminarUsuario(String id) {
        try {
            utilLog.escribirLog("Eliminando usuario: " + id, Level.INFO);
            listaUsuariosCache.removeIf(u -> u.getId().equals(id));
            guardarTodosUsuarios();
        } catch (Exception e) {
            utilLog.escribirLog("Error eliminando usuario: " + e.getMessage(), Level.SEVERE);
        }
    }

    public List<Usuario> obtenerTodosUsuarios() {
        utilLog.escribirLog("Obteniendo todos los usuarios", Level.INFO);
        return new ArrayList<>(listaUsuariosCache);
    }

    public List<Estudiante> obtenerTodosEstudiantes() {
        utilLog.escribirLog("Obteniendo todos los estudiantes", Level.INFO);
        return listaUsuariosCache.stream()
                .filter(u -> u instanceof Estudiante)
                .map(u -> (Estudiante) u)
                .collect(Collectors.toList());
    }

    // ==================== MÉTODOS DE CONTENIDO ====================
    public Contenido buscarContenidoPorId(String contId) {
        try {
            utilLog.escribirLog("Buscando contenido por ID: " + contId, Level.INFO);

            // Buscar en cache
            Contenido contenido = listaContenidosCache.stream()
                    .filter(c -> c.getId().equals(contId))
                    .findFirst()
                    .orElse(null);

            if (contenido != null) {
                // Cargar valoraciones si no están cargadas
                if (contenido.getValoraciones() == null) {
                    contenido.setValoraciones(cargarValoracionesDesdePersistencia(contId));
                }
            }

            return contenido;
        } catch (Exception e) {
            utilLog.escribirLog("Error buscando contenido: " + e.getMessage(), Level.SEVERE);
            return null;
        }
    }

    private LinkedList<Valoracion> cargarValoracionesDesdePersistencia(String contenidoId) {
        return listaContenidosCache.stream()
                .filter(c -> c.getId().equals(contenidoId))
                .findFirst()
                .map(Contenido::getValoraciones)
                .map(valoraciones -> {
                    if (valoraciones instanceof LinkedList) {
                        return (LinkedList<Valoracion>) valoraciones;
                    } else {
                        return new LinkedList<>(valoraciones); // Convertir a LinkedList si no lo es
                    }
                })
                .orElse(new LinkedList<>()); // Retorna LinkedList vacía por defecto
    }

    public List<Contenido> buscarContenidoPorAutor(String autorId) {
        try {
            utilLog.escribirLog("Buscando contenido por autor: " + autorId, Level.INFO);
            return listaContenidosCache.stream()
                    .filter(c -> c.getAutor().equals(autorId))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            utilLog.escribirLog("Error buscando contenido: " + e.getMessage(), Level.SEVERE);
            return Collections.emptyList();
        }
    }

    public List<Contenido> buscarContenidoPorTema(String tema) {
        try {
            utilLog.escribirLog("Buscando contenido por tema: " + tema, Level.INFO);
            return listaContenidosCache.stream()
                    .filter(c -> c.getTema().equals(tema))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            utilLog.escribirLog("Error buscando contenido: " + e.getMessage(), Level.SEVERE);
            return Collections.emptyList();
        }
    }

    public List<Contenido> buscarContenidoPorTipo(TipoContenido tipo) {
        try {
            utilLog.escribirLog("Buscando contenido por tipo: " + tipo, Level.INFO);
            return listaContenidosCache.stream()
                    .filter(c -> c.getTipo().equals(tipo))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            utilLog.escribirLog("Error buscando contenido: " + e.getMessage(), Level.SEVERE);
            return Collections.emptyList();
        }
    }

    public boolean eliminarContenido(String id) {
        try {
            utilLog.escribirLog("Eliminando contenido: " + id, Level.INFO);
            boolean removed = listaContenidosCache.removeIf(c -> c.getId().equals(id));
            guardarTodosContenidos();
            return removed;
        } catch (Exception e) {
            utilLog.escribirLog("Error eliminando contenido: " + e.getMessage(), Level.SEVERE);
            return false;
        }
    }

    public boolean guardarContenido(Contenido cont) {
        try {
            utilLog.escribirLog("Guardando contenido: " + cont.getTitulo(), Level.INFO);

            // Eliminar contenido existente con el mismo ID para evitar duplicados
            listaContenidosCache.removeIf(c -> c.getId().equals(cont.getId()));

            listaContenidosCache.add(cont);
            guardarTodosContenidos();
            return true;
        } catch (Exception e) {
            utilLog.escribirLog("Error guardando contenido: " + e.getMessage(), Level.SEVERE);
            return false;
        }
    }

    public void actualizarContenido(Contenido cont) {
        try {
            utilLog.escribirLog("Actualizando contenido: " + cont.getTitulo(), Level.INFO);
            listaContenidosCache.removeIf(c -> c.getId().equals(cont.getId()));
            listaContenidosCache.add(cont);
            guardarTodosContenidos();
        } catch (Exception e) {
            utilLog.escribirLog("Error actualizando contenido: " + e.getMessage(), Level.SEVERE);
        }
    }

    public boolean agregarValoracion(String contenidoId, Valoracion valoracion) {
        try {
            // 1. Buscar el contenido existente
            Contenido contenido = buscarContenidoPorId(contenidoId);
            if (contenido == null) {
                return false;
            }

            // 2. Verificar si el usuario ya valoró este contenido
            if (contenido.getValoraciones().stream()
                    .anyMatch(v -> v.getIdAutor().equals(valoracion.getIdAutor()))) {
                return false;
            }

            // 4. Agregar la valoración
            contenido.getValoraciones().add(valoracion);

            // 5. Actualizar el contenido en la persistencia
            actualizarContenido(contenido);

            return true;
        } catch (Exception e) {
            utilLog.escribirLog("Error agregando valoración: " + e.getMessage(), Level.SEVERE);
            return false;
        }
    }

    public double calcularPromedioValoraciones(String contenidoId) {
        Contenido contenido = buscarContenidoPorId(contenidoId);
        if (contenido == null || contenido.getValoraciones().isEmpty()) {
            return 0.0;
        }

        // Filtrar valoraciones duplicadas
        Set<String> idsValoraciones = new HashSet<>();
        double suma = 0;
        int contador = 0;

        for (Valoracion v : contenido.getValoraciones()) {
            if (idsValoraciones.add(v.getIdValoracion())) {
                suma += v.getValor();
                contador++;
            }
        }

        return contador > 0 ? suma / contador : 0.0;
    }

    public List<Contenido> obtenerContenidosPorUsuario(String idEstudiante) {
        try {
            utilLog.escribirLog("Obteniendo contenidos por usuario: " + idEstudiante, Level.INFO);
            return listaContenidosCache.stream()
                    .filter(c -> c.getAutor().equals(idEstudiante))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            utilLog.escribirLog("Error obteniendo contenidos: " + e.getMessage(), Level.SEVERE);
            return Collections.emptyList();
        }
    }

    // ==================== MÉTODOS DE SOLICITUDES ====================
    public void guardarSolicitud(SolicitudAyuda solicitud) {
        try {
            utilLog.escribirLog("Guardando solicitud: " + solicitud.getTema(), Level.INFO);
            listaSolicitudesCache.add(solicitud);
            guardarTodasSolicitudes();
        } catch (Exception e) {
            utilLog.escribirLog("Error guardando solicitud: " + e.getMessage(), Level.SEVERE);
        }
    }

    public void eliminarSolicitud(String idSolicitud) {
        try {
            utilLog.escribirLog("Eliminando solicitud: " + idSolicitud, Level.INFO);
            listaSolicitudesCache.removeIf(s -> s.getId().equals(idSolicitud));
            guardarTodasSolicitudes();
        } catch (Exception e) {
            utilLog.escribirLog("Error eliminando solicitud: " + e.getMessage(), Level.SEVERE);
        }
    }

    public void actualizarSolicitud(SolicitudAyuda solicitud) {
        try {
            utilLog.escribirLog("Actualizando solicitud: " + solicitud.getTema(), Level.INFO);
            listaSolicitudesCache.removeIf(s -> s.getId().equals(solicitud.getId()));
            listaSolicitudesCache.add(solicitud);
            guardarTodasSolicitudes();
        } catch (Exception e) {
            utilLog.escribirLog("Error actualizando solicitud: " + e.getMessage(), Level.SEVERE);
        }
    }

    public List<SolicitudAyuda> obtenerTodasSolicitudes() {
        utilLog.escribirLog("Obteniendo todas las solicitudes", Level.INFO);
        return new ArrayList<>(listaSolicitudesCache);
    }

    public SolicitudAyuda buscarSolicitudPorId(String id) {
        try {
            utilLog.escribirLog("Buscando solicitud por ID: " + id, Level.INFO);
            return listaSolicitudesCache.stream()
                    .filter(s -> s.getId().equals(id))
                    .findFirst()
                    .orElse(null);
        } catch (Exception e) {
            utilLog.escribirLog("Error buscando solicitud: " + e.getMessage(), Level.SEVERE);
            return null;
        }
    }

    // ==================== MÉTODOS DE GRUPOS ====================
    public void guardarGrupo(GrupoEstudio grupo) {
        try {
            utilLog.escribirLog("Guardando grupo: " + grupo.getNombre(), Level.INFO);
            listaGruposCache.add(grupo);
            guardarTodosGrupos();
        } catch (Exception e) {
            utilLog.escribirLog("Error guardando grupo: " + e.getMessage(), Level.SEVERE);
        }
    }

    public GrupoEstudio buscarGrupoPorId(String grupoId) {
        try {
            utilLog.escribirLog("Buscando grupo por ID: " + grupoId, Level.INFO);
            return listaGruposCache.stream()
                    .filter(g -> g.getIdGrupo().equals(grupoId))
                    .findFirst()
                    .orElse(null);
        } catch (Exception e) {
            utilLog.escribirLog("Error buscando grupo: " + e.getMessage(), Level.SEVERE);
            return null;
        }
    }

    public void actualizarGrupo(GrupoEstudio grupo) {
        try {
            utilLog.escribirLog("Actualizando grupo: " + grupo.getNombre(), Level.INFO);
            listaGruposCache.removeIf(g -> g.getIdGrupo().equals(grupo.getIdGrupo()));
            listaGruposCache.add(grupo);
            guardarTodosGrupos();
        } catch (Exception e) {
            utilLog.escribirLog("Error actualizando grupo: " + e.getMessage(), Level.SEVERE);
        }
    }

    public List<GrupoEstudio> obtenerTodosGrupos() {
        utilLog.escribirLog("Obteniendo todos los grupos", Level.INFO);
        return new ArrayList<>(listaGruposCache);
    }

    // ==================== MÉTODOS DE REPORTES ====================
    public void guardarReporte(String contenidoReporte) {
        try {
            // Crear nuevo reporte a partir del String
            Reporte nuevoReporte = new Reporte(
                    null,                // ID se generará automáticamente
                    contenidoReporte,    // Contenido del reporte
                    new Date()          // Fecha actual
            );

            utilLog.escribirLog("Guardando reporte: " + nuevoReporte.getIdReporte(), Level.INFO);
            listaReportesCache.add(nuevoReporte);
            guardarTodosReportes();
        } catch (Exception e) {
            utilLog.escribirLog("Error guardando reporte: " + e.getMessage(), Level.SEVERE);
            throw new RuntimeException("Error al transformar y guardar reporte", e);
        }
    }

    public List<Reporte> obtenerTodosReportes() {
        utilLog.escribirLog("Obteniendo todos los reportes", Level.INFO);
        return new ArrayList<>(listaReportesCache);
    }

    // MÉTODO UTIL PARA FORZAR GUARDADO COMPLETO
    public void gestionarArchivos(List<Usuario> listaUsuarios, List<SolicitudAyuda> listaSolicitudes,
                                  List<Contenido> listaContenidos, List<GrupoEstudio> listaGruposEstudio,
                                  List<Reporte> listaReportes) throws IOException {
        utilLog.escribirLog("Gestionando archivos de persistencia", Level.INFO);
        if (listaUsuarios != null) this.listaUsuariosCache = new ArrayList<>(listaUsuarios);
        if (listaSolicitudes != null) this.listaSolicitudesCache = new ArrayList<>(listaSolicitudes);
        if (listaContenidos != null) this.listaContenidosCache = new ArrayList<>(listaContenidos);
        if (listaGruposEstudio != null) this.listaGruposCache = new ArrayList<>(listaGruposEstudio);
        if (listaReportes != null) this.listaReportesCache = new ArrayList<>(listaReportes);

        guardarTodosUsuarios();
        guardarTodasSolicitudes();
        guardarTodosContenidos();
        guardarTodosGrupos();
        guardarTodosReportes();
    }

    // MÉTODOS DE GUARDADO COMPLETO INDIVIDUAL
    private void guardarTodosUsuarios() throws IOException {
        String ruta = utilProperties.obtenerPropiedad("rutaUsuarios.txt");
        utilLog.escribirLog("Guardando usuarios en: " + ruta, Level.FINE);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(ruta))) {
            for (Usuario usuario : listaUsuariosCache) {
                String linea = usuarioToCsv(usuario);
                writer.write(linea);
                writer.newLine();
            }
        } catch (IOException e) {
            utilLog.escribirLog("Error escribiendo archivo de usuarios: " + e.getMessage(), Level.SEVERE);
            throw e;
        }
    }


    private void guardarTodosContenidos() throws IOException {
        String ruta = utilProperties.obtenerPropiedad("rutaContenidos.txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(ruta))) {
            for (Contenido contenido : listaContenidosCache) {
                writer.write(contenidoToCsv(contenido));
                writer.newLine();
            }
        }
    }

    private void guardarTodasSolicitudes() throws IOException {
        String ruta = utilProperties.obtenerPropiedad("rutaSolicitudes.txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(ruta))) {
            for (SolicitudAyuda solicitud : listaSolicitudesCache) {
                writer.write(solicitudToCsv(solicitud));
                writer.newLine();
            }
        }
    }

    private void guardarTodosGrupos() throws IOException {
        String ruta = utilProperties.obtenerPropiedad("rutaGruposEstudio.txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(ruta))) {
            for (GrupoEstudio grupo : listaGruposCache) {
                writer.write(grupoToCsv(grupo));
                writer.newLine();
            }
        }
    }

    private void guardarTodosReportes() throws IOException {
        String ruta = utilProperties.obtenerPropiedad("rutaReportes.txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(ruta))) {
            for (Reporte reporte : listaReportesCache) {
                writer.write(reporteToCsv(reporte));
                writer.newLine();
            }
        }
    }

    private String usuarioToCsv(Usuario usuario) {
        // Procesar campos básicos
        System.out.println(usuario.getContrasenia());
        String contrasenia = (usuario.getContrasenia() == null || usuario.getContrasenia().isEmpty())
                ? "SIN_CONTRASEÑA"
                : escapeCsv(usuario.getContrasenia());

        String[] campos = {
                usuario.getId() != null ? usuario.getId() : "NULL_ID",
                usuario.getNombre() != null ? escapeCsv(usuario.getNombre()) : "",
                usuario.getCorreo() != null ? escapeCsv(usuario.getCorreo()) : "",
                contrasenia,
                String.valueOf(usuario.isSuspendido()),
                String.valueOf(usuario.getDiasSuspension()),
                usuario.getClass().getSimpleName()
        };

        // Procesar campos específicos de Estudiante
        String intereses = "";
        String contenidos = "";

        if (usuario instanceof Estudiante) {
            Estudiante est = (Estudiante) usuario;
            intereses = est.getIntereses() != null ? escapeCsv(String.join(";", est.getIntereses())) : "";
            contenidos = est.getIdsContenidosPublicados() != null ? escapeCsv(String.join(";", est.getIdsContenidosPublicados())) : "";
        }

        return String.join(",", campos) + "," + intereses + "," + contenidos;
    }



    // Métodos auxiliares para manejar CSV
    private String escapeCsv(String input) {
        if (input == null) return "";
        // Si contiene comas o saltos de línea, envolver en comillas
        if (input.contains(",") || input.contains("\n") || input.contains("\"")) {
            return "\"" + input.replace("\"", "\"\"") + "\"";
        }
        return input;
    }

    private String unescapeCsv(String input) {
        if (input == null || input.isEmpty()) return "";
        // Quitar comillas exteriores si existen
        if (input.startsWith("\"") && input.endsWith("\"")) {
            input = input.substring(1, input.length() - 1);
        }
        return input.replace("\"\"", "\"");
    }

    private String contenidoToCsv(Contenido contenido) {
        // Primero guardamos los campos básicos del contenido
        String[] camposBasicos = {
                contenido.getId(),
                escapeCsv(contenido.getTitulo()),
                escapeCsv(contenido.getAutor()),
                contenido.getFecha().toString(),
                contenido.getTipo().name(),
                escapeCsv(contenido.getDescripcion()),
                escapeCsv(contenido.getTema()),
                escapeCsv(contenido.getContenido()) // Este es el campo importante para la ruta/imagen
        };

        // Luego procesamos las valoraciones por separado
        String valoracionesStr = contenido.getValoraciones().stream()
                .map(v -> String.join("~",
                        v.getIdValoracion(),
                        v.getTema(),
                        v.getDescripcion(),
                        v.getIdAutor(),
                        String.valueOf(v.getValor()),
                        v.getFecha() != null ? String.valueOf(v.getFecha().getTime()) : "",
                        v.getComentario()
                ))
                .collect(Collectors.joining("|"));

        // Unimos todo con un separador diferente para evitar confusiones
        return String.join("§", camposBasicos) + "§VALORACIONES§" + valoracionesStr;
    }

    private String solicitudToCsv(SolicitudAyuda solicitud) {
        return String.join(";",
                solicitud.getId(),
                solicitud.getTema(),
                solicitud.getDescripcion(),
                String.valueOf(solicitud.getFecha().getTime()),
                solicitud.getUrgencia().name(),
                solicitud.getSolicitanteId(),
                solicitud.getEstado().name()
        );
    }

    private String grupoToCsv(GrupoEstudio grupo) {
        return String.join("#",
                grupo.getIdGrupo(),
                grupo.getNombre(),
                grupo.getDescripcion(),
                String.join(",", grupo.getIdMiembros()),
                String.join(",", grupo.getIdContenidos()),
                String.valueOf(grupo.getFechaCreacion().getTime())
        );
    }

    private String reporteToCsv(Reporte reporte) {
        return String.join("~",
                reporte.getIdReporte(),
                reporte.getContenido(),
                String.valueOf(reporte.getFechaGeneracion().getTime())
        );
    }

    // Métodos de carga de datos
    private void cargarUsuarios() {
        String ruta = utilProperties.obtenerPropiedad("rutaUsuarios.txt");
        File archivo = new File(ruta);
        if (!archivo.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(ruta))) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                Usuario usuario = parsearUsuario(linea);
                if (usuario != null) {
                    listaUsuariosCache.add(usuario);
                }
            }
        } catch (IOException e) {
            utilLog.escribirLog("Error cargando usuarios: " + e.getMessage(), Level.SEVERE);
        }
        for (Usuario usuario : listaUsuariosCache) {
            if (usuario instanceof Estudiante) {
                Estudiante estudiante = (Estudiante) usuario;
                estudiante.setUtilEstudiante(UtilEstudiante.getInstance());
            }
        }
    }

    private Usuario parsearUsuario(String csv) {
        String[] partes = csv.split(",");
        if (partes.length < 7) return null;

        String contrasena = partes[3];

        Usuario usuario;
        if (partes[6].equals("Estudiante")) {
            usuario = new Estudiante(
                    partes[0], partes[1], partes[2], contrasena,
                    Boolean.parseBoolean(partes[4]), Integer.parseInt(partes[5]),
                    new LinkedList<>(),
                    partes.length > 7 ? Arrays.asList(partes[7].split(";")) : new ArrayList<>()
            );
        } else {
            usuario = new Moderador(
                    partes[0], partes[1], partes[2], contrasena,
                    Boolean.parseBoolean(partes[4]), Integer.parseInt(partes[5])
            );
        }

        return usuario;
    }

    private void cargarContenidos() {
        String ruta = utilProperties.obtenerPropiedad("rutaContenidos.txt");
        System.out.println("🔄 Intentando cargar contenidos desde: " + ruta);

        File archivo = new File(ruta);
        if (!archivo.exists()) {
            System.out.println("❌ El archivo no existe en la ruta especificada");
            return;
        }
        if (!archivo.canRead()) {
            System.out.println("❌ El archivo existe pero no se puede leer");
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(ruta))) {
            System.out.println("📂 Archivo abierto correctamente");
            String linea;
            int contador = 0;

            while ((linea = reader.readLine()) != null) {
                contador++;
                System.out.println("\n📝 Línea " + contador + " leída: " + linea);

                Contenido contenido = parsearContenido(linea);
                if (contenido != null) {
                    System.out.println("✅ Contenido parseado correctamente:");
                    System.out.println("   ID: " + contenido.getId());
                    System.out.println("   Título: " + contenido.getTitulo());
                    System.out.println("   Autor: " + contenido.getAutor());
                    System.out.println("   Tipo: " + contenido.getTipo());
                    listaContenidosCache.add(contenido);
                } else {
                    System.out.println("❌ No se pudo parsear el contenido de esta línea");
                }
            }

            System.out.println("\n=================================");
            System.out.println("📊 Resumen de carga:");
            System.out.println("Total líneas leídas: " + contador);
            System.out.println("Contenidos cargados: " + listaContenidosCache.size());
            System.out.println("=================================");

        } catch (IOException e) {
            System.out.println("🔥 Error crítico al leer el archivo:");
            e.printStackTrace();
            utilLog.escribirLog("Error cargando contenidos: " + e.getMessage(), Level.SEVERE);
        }
    }

    private Contenido parsearContenido(String csv) {
        if (csv == null || csv.trim().isEmpty()) {
            System.out.println("⚠️ Línea vacía encontrada");
            return null;
        }

        // Dividir primero por el separador de valoraciones
        String[] partes = csv.split("§VALORACIONES§");
        String parteContenido = partes[0];
        String parteValoraciones = partes.length > 1 ? partes[1] : "";

        // Parsear el contenido principal
        String[] camposContenido = parteContenido.split("§");
        if (camposContenido.length < 8) {
            System.out.println("❌ Formato incorrecto. Campos esperados: 8, encontrados: " + camposContenido.length);
            return null;
        }

        try {
            // Parsear campos obligatorios
            String id = camposContenido[0].trim();
            String titulo = camposContenido[1].trim();
            String autor = camposContenido[2].trim();
            LocalDateTime fecha = LocalDateTime.parse(camposContenido[3].trim());
            TipoContenido tipo = TipoContenido.valueOf(camposContenido[4].trim());
            String descripcion = camposContenido[5].trim();
            String tema = camposContenido[6].trim();
            String contenidoStr = camposContenido[7].trim(); // Ruta de la imagen/contenido

            // Parsear valoraciones
            LinkedList<Valoracion> valoraciones = new LinkedList<>();
            if (!parteValoraciones.isEmpty()) {
                valoraciones = Arrays.stream(parteValoraciones.split("\\|"))
                        .filter(s -> !s.trim().isEmpty())
                        .map(this::parseValoracion)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toCollection(LinkedList::new));
            }

            return new Contenido(
                    id,
                    titulo,
                    autor,
                    fecha,
                    tipo,
                    descripcion,
                    tema,
                    contenidoStr, // Ruta preservada correctamente
                    valoraciones
            );

        } catch (Exception e) {
            System.out.println("🔥 Error al parsear contenido: " + e.getMessage());
            return null;
        }
    }

    private Valoracion parseValoracion(String valoracionStr) {
        String[] parts = valoracionStr.split("~");
        if (parts.length < 7) {
            // Valores por defecto para campos faltantes
            return new Valoracion(
                    parts.length > 0 ? parts[0] : UtilId.generarIdAleatorio(),
                    parts.length > 1 ? parts[1] : "",
                    parts.length > 2 ? parts[2] : "",
                    parts.length > 3 ? parts[3] : "anonimo",
                    parts.length > 4 ? Integer.parseInt(parts[4]) : 0,
                    parts.length > 5 && !parts[5].isEmpty() ? new Date(Long.parseLong(parts[5])) : new Date(),
                    parts.length > 6 ? parts[6] : ""
            );
        }

        return new Valoracion(
                parts[0],
                parts[1],
                parts[2],
                parts[3],
                Integer.parseInt(parts[4]),
                new Date(Long.parseLong(parts[5])),
                parts[6]
        );
    }

    private void cargarSolicitudes() {
        String ruta = utilProperties.obtenerPropiedad("rutaSolicitudes.txt");
        File archivo = new File(ruta);
        if (!archivo.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(ruta))) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                SolicitudAyuda solicitud = parsearSolicitud(linea);
                if (solicitud != null) {
                    listaSolicitudesCache.add(solicitud);
                }
            }
        } catch (IOException e) {
            utilLog.escribirLog("Error cargando solicitudes: " + e.getMessage(), Level.SEVERE);
        }
    }

    private SolicitudAyuda parsearSolicitud(String csv) {
        String[] partes = csv.split(";");
        if (partes.length < 7) return null;

        String id = partes[0];
        String tema = partes[1];
        String descripcion = partes[2];
        Date fecha = new Date(Long.parseLong(partes[3]));
        Urgencia urgencia = Urgencia.valueOf(partes[4]);
        String solicitanteId = partes[5];
        Estado estado = Estado.valueOf(partes[6]);

        SolicitudAyuda solicitud = new SolicitudAyuda(id, tema, descripcion, fecha, urgencia, solicitanteId);
        solicitud.setEstado(estado);
        return solicitud;
    }

    private void cargarGrupos() {
        String ruta = utilProperties.obtenerPropiedad("rutaGruposEstudio.txt");
        File archivo = new File(ruta);
        if (!archivo.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(ruta))) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                GrupoEstudio grupo = parsearGrupo(linea);
                if (grupo != null) {
                    listaGruposCache.add(grupo);
                }
            }
        } catch (IOException e) {
            utilLog.escribirLog("Error cargando grupos: " + e.getMessage(), Level.SEVERE);
        } catch (OperacionFallidaException e) {
            throw new RuntimeException(e);
        }
    }

    private GrupoEstudio parsearGrupo(String csv) throws OperacionFallidaException {
        String[] partes = csv.split("#");
        if (partes.length < 6) return null;

        String idGrupo = partes[0];
        String nombre = partes[1];
        String descripcion = partes[2];
        LinkedList<String> idMiembros = new LinkedList<>(Arrays.asList(partes[3].split(",")));
        LinkedList<String> idContenidos = new LinkedList<>(Arrays.asList(partes[4].split(",")));
        Date fechaCreacion = new Date(Long.parseLong(partes[5]));

        return new GrupoEstudio(idGrupo, nombre, descripcion, idMiembros, idContenidos, fechaCreacion);
    }

    private void cargarReportes() {
        String ruta = utilProperties.obtenerPropiedad("rutaReportes.txt");
        File archivo = new File(ruta);
        if (!archivo.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(ruta))) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                Reporte reporte = parsearReporte(linea);
                if (reporte != null) {
                    listaReportesCache.add(reporte);
                }
            }
        } catch (IOException e) {
            utilLog.escribirLog("Error cargando reportes: " + e.getMessage(), Level.SEVERE);
        }
    }

    private Reporte parsearReporte(String csv) {
        String[] partes = csv.split("~");
        if (partes.length < 3) return null;

        String idReporte = partes[0];
        String contenido = partes[1];
        Date fechaGeneracion = new Date(Long.parseLong(partes[2]));

        return new Reporte(idReporte, contenido, fechaGeneracion);
    }

    public void actualizarEstudiante(Estudiante estudiante) {
        try {
            utilLog.escribirLog("Actualizando estudiante: " + estudiante.getNombre(), Level.INFO);

            // Primero actualizamos la lista en memoria
            listaUsuariosCache.removeIf(u -> u.getId().equals(estudiante.getId()));
            listaUsuariosCache.add(estudiante);

            // Luego guardamos los cambios en el archivo
            guardarTodosUsuarios();

        } catch (Exception e) {
            utilLog.escribirLog("Error actualizando estudiante: " + e.getMessage(), Level.SEVERE);
            throw new RuntimeException("Error al actualizar estudiante", e);
        }
    }

    public List<Contenido> obtenerTodosContenidos() {
        utilLog.escribirLog("Obteniendo todos los contenidos", Level.INFO);
        return new ArrayList<>(listaContenidosCache);
    }

    public void guardarGrupos(List<GrupoEstudio> grupos) {
        try {
            utilLog.escribirLog("Guardando grupos", Level.INFO);
            listaGruposCache = new ArrayList<>(grupos);
            guardarTodosGrupos();
        } catch (IOException e) {
            utilLog.escribirLog("Error guardando grupos: " + e.getMessage(), Level.SEVERE);
        }
    }

    public List<Valoracion> obtenerValoracionesPorEstudiante(String id) {
        try {
            utilLog.escribirLog("Obteniendo valoraciones por estudiante: " + id, Level.INFO);
            return listaContenidosCache.stream()
                    .flatMap(c -> c.getValoraciones().stream())
                    .filter(v -> v.getIdAutor().equals(id))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            utilLog.escribirLog("Error obteniendo valoraciones: " + e.getMessage(), Level.SEVERE);
            return Collections.emptyList();
        }
    }

    public void agregarGrupos(List<GrupoEstudio> grupos) {
        try {
            utilLog.escribirLog("Agregando grupos", Level.INFO);
            listaGruposCache.addAll(grupos);
            guardarTodosGrupos();
        } catch (IOException e) {
            utilLog.escribirLog("Error agregando grupos: " + e.getMessage(), Level.SEVERE);
            throw new PersistenciaException("Error al agregar grupos", e);
        }
    }

    public List<Contenido> obtenerContenidosPorGrupo(String idGrupo) {
        try {
            utilLog.escribirLog("Obteniendo contenidos por grupo: " + idGrupo, Level.INFO);
            GrupoEstudio grupo = buscarGrupoPorId(idGrupo);
            if (grupo == null) {
                return Collections.emptyList();
            }
            return listaContenidosCache.stream()
                    .filter(c -> grupo.getIdContenidos().contains(c.getId()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            utilLog.escribirLog("Error obteniendo contenidos por grupo: " + e.getMessage(), Level.SEVERE);
            return Collections.emptyList();
        }
    }

    /**
     * Obtiene mensajes de un grupo específico (generalmente el actual)
     * @param idGrupo ID del grupo (opcional, usa el grupo actual si es null)
     * @return Colección inmodificable de mensajes
     * @throws OperacionFallidaException Si el grupo no existe o hay errores de acceso
     */
    /**
     * Obtiene todos los mensajes asociados a un grupo de estudio desde la persistencia
     * @param idGrupo ID del grupo del cual se quieren obtener los mensajes
     * @return Colección inmodificable de mensajes (nunca null)
     * @throws OperacionFallidaException Si ocurre un error al acceder a la persistencia
     */
    public Collection<Object> obtenerMensajesPorGrupo(String idGrupo) throws OperacionFallidaException {
        // 1. Validación inicial
        if (idGrupo == null || idGrupo.trim().isEmpty()) {
            utilLog.escribirLog("ID de grupo inválido al obtener mensajes: " + idGrupo, Level.WARNING);
            return Collections.emptyList();
        }

        // 2. Verificar existencia del grupo
        GrupoEstudio grupo = buscarGrupoPorId(idGrupo);
        if (grupo == null) {
            utilLog.escribirLog("No se encontró el grupo con ID: " + idGrupo, Level.WARNING);
            return Collections.emptyList();
        }

        // 3. Obtener mensajes del archivo de persistencia
        String rutaMensajes = utilProperties.obtenerPropiedad("rutaMensajes.txt");
        File archivoMensajes = new File(rutaMensajes);

        if (!archivoMensajes.exists()) {
            utilLog.escribirLog("Archivo de mensajes no encontrado en: " + rutaMensajes, Level.INFO);
            return Collections.emptyList();
        }

        List<Object> mensajes = new LinkedList<>();
        long inicio = System.currentTimeMillis();

        try (BufferedReader reader = new BufferedReader(new FileReader(archivoMensajes))) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                if (linea.startsWith(idGrupo + "|")) {
                    Object mensaje = parsearMensaje(linea);
                    if (mensaje != null) {
                        mensajes.add(mensaje);
                    }
                }
            }
        } catch (IOException e) {
            String errorMsg = "Error leyendo mensajes del grupo " + idGrupo;
            utilLog.escribirLog(errorMsg + ": " + e.getMessage(), Level.SEVERE);
        }

        // 4. Registrar resultados
        long duracion = System.currentTimeMillis() - inicio;
        utilLog.escribirLog(String.format(
                "Obtenidos %d mensajes para el grupo %s en %d ms",
                mensajes.size(), idGrupo, duracion
        ), Level.FINE);

        return Collections.unmodifiableCollection(mensajes);
    }

    /**
     * Parsea una línea del archivo de mensajes a un objeto Mensaje
     * Formato esperado: grupoId|mensajeId|autorId|contenido|timestamp
     */
    private Object parsearMensaje(String linea) {
        try {
            String[] partes = linea.split("\\|");
            if (partes.length < 5) {
                utilLog.escribirLog("Formato de mensaje inválido: " + linea, Level.WARNING);
                return null;
            }

            // Crear estructura básica del mensaje (puedes usar una clase específica si la tienes)
            Map<String, Object> mensaje = new HashMap<>();
            mensaje.put("grupoId", partes[0]);
            mensaje.put("mensajeId", partes[1]);
            mensaje.put("autorId", partes[2]);
            mensaje.put("contenido", partes[3]);
            mensaje.put("fecha", new Date(Long.parseLong(partes[4])));

            // Si hay más campos (como tipo de mensaje, estado, etc.)
            if (partes.length > 5) {
                mensaje.put("tipo", partes[5]);
            }

            return mensaje;
        } catch (Exception e) {
            utilLog.escribirLog("Error parseando mensaje: " + e.getMessage(), Level.WARNING);
            return null;
        }
    }

    public void eliminarGrupo(String grupoId) {
        try {
            utilLog.escribirLog("Eliminando grupo: " + grupoId, Level.INFO);
            listaGruposCache.removeIf(g -> g.getIdGrupo().equals(grupoId));
            guardarTodosGrupos();
        } catch (IOException e) {
            utilLog.escribirLog("Error eliminando grupo: " + e.getMessage(), Level.SEVERE);
            throw new PersistenciaException("Error al eliminar grupo", e);
        }
    }

    public static class PersistenciaException extends RuntimeException {
        public PersistenciaException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}