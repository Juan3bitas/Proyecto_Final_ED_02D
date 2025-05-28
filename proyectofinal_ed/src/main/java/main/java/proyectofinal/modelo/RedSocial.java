package main.java.proyectofinal.modelo;

import main.java.proyectofinal.excepciones.OperacionFallidaException;
import main.java.proyectofinal.utils.UtilProperties;
import main.java.proyectofinal.utils.UtilRedSocial;

import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Clase principal que coordina todas las operaciones de la red social
 * educativa.
 * Centraliza la gestión de usuarios, contenidos, grupos y solicitudes de ayuda.
 */
public class RedSocial {
    // Constantes y atributos
    private final String nombre = "AprendeJuntos";
    private final transient UtilRedSocial utilRed;
    private List<Usuario> usuarios;
    private ArbolContenidos arbolContenidos;
    private GrafoAfinidad grafoAfinidad;
    private PriorityQueue<SolicitudAyuda> colaSolicitudes;
    private List<GrupoEstudio> gruposEstudio;
    private Map<String, GrupoEstudio> mapaGruposPorId;
    private Map<String, Set<String>> miembrosPorGrupo;
    private Map<String, Set<String>> gruposPorMiembro;
    private static final Logger LOGGER = Logger.getLogger(RedSocial.class.getName());

    // Constructor
    public RedSocial(UtilRedSocial utilRed) throws OperacionFallidaException {
        verificarArchivosPersistencia();
        this.utilRed = Objects.requireNonNull(utilRed, "UtilRedSocial no puede ser nulo");
        this.usuarios = utilRed.obtenerUsuarios() != null ? utilRed.obtenerUsuarios() : new ArrayList<>();
        this.arbolContenidos = new ArbolContenidos(CriterioOrden.TEMA);
        this.grafoAfinidad = new GrafoAfinidad();
        this.colaSolicitudes = new PriorityQueue<>(Comparator.comparingInt(s -> s.getUrgencia().ordinal()));
        this.colaSolicitudes.addAll(utilRed.obtenerSolicitudes());
        this.gruposEstudio = new ArrayList<>();
        this.mapaGruposPorId = new HashMap<>();
        //this.arbolContenidos.inicializarConLista(utilRed.obtenerContenidos());
        cargarContenidosAlArbol();
        cargarRelacionesAfinidad();
        validarIntegridadGrupos();
        limpiarGruposDuplicados();
        consolidarGrupos();
        grafoAfinidad.calcularAfinidades();
        List<GrupoEstudio> gruposExistentes = utilRed.obtenerGrupos();
        if (gruposExistentes != null) {
            gruposExistentes.forEach(grupo -> {
                this.gruposEstudio.add(grupo);
                this.mapaGruposPorId.put(grupo.getId(), grupo);
            });
        }
    }

    private void verificarArchivosPersistencia() {
        UtilProperties utilProperties = UtilProperties.getInstance();
        String[] archivos = {
                utilProperties.obtenerPropiedad("rutaUsuarios.txt"),
                utilProperties.obtenerPropiedad("rutaContenidos.txt"),
                utilProperties.obtenerPropiedad("rutaSolicitudes.txt"),
                utilProperties.obtenerPropiedad("rutaGruposEstudio.txt"),
                utilProperties.obtenerPropiedad("rutaReportes.txt")
        };

    }

    // Métodos de inicialización
    private void cargarRelacionesAfinidad() throws OperacionFallidaException {
        usuarios.stream()
                .filter(u -> u instanceof Estudiante)
                .map(u -> (Estudiante) u)
                .forEach(grafoAfinidad::agregarEstudiante);

        List<GrupoEstudio> grupos = utilRed.obtenerGrupos();
        if (grupos != null) {
            grupos.forEach(this::agregarRelacionesDeGrupo);
        }
    }

    private void agregarRelacionesDeGrupo(GrupoEstudio grupo) {
        List<String> miembrosIds = grupo.getIdMiembros();
        for (int i = 0; i < miembrosIds.size(); i++) {
            for (int j = i + 1; j < miembrosIds.size(); j++) {
                Usuario u1 = buscarUsuario(miembrosIds.get(i));
                Usuario u2 = buscarUsuario(miembrosIds.get(j));

                if (u1 instanceof Estudiante && u2 instanceof Estudiante) {
                    Estudiante e1 = (Estudiante) u1;
                    Estudiante e2 = (Estudiante) u2;

                    int pesoActual = grafoAfinidad.obtenerPesoAfinidad(e1, e2);
                    actualizarAfinidad(e1, e2, pesoActual + 1);
                }
            }
        }
    }

    private void cargarContenidosAlArbol() {
        List<Contenido> contenidos = utilRed.obtenerContenidos();
        if (contenidos != null && !contenidos.isEmpty()) {
            System.out.println("Cargando " + contenidos.size() + " contenidos al árbol");
            contenidos.forEach(c -> {
                System.out.println("Insertando contenido: " + c.getTitulo());
                arbolContenidos.insertar(c);
            });
        } else {
            System.out.println("No se encontraron contenidos para cargar");
        }
    }

    // Métodos de gestión de usuarios
    public boolean registrarUsuario(Usuario usuario) {
        validarUsuario(usuario);
        utilRed.registrarUsuario(usuario);
        usuarios.add(usuario);
        if (usuario instanceof Estudiante) {
            grafoAfinidad.agregarEstudiante((Estudiante) usuario);
        }
        return true;
    }

    public void modificarUsuario(Usuario usuario) {
        validarUsuario(usuario);
        utilRed.modificarUsuario(usuario);
    }

    public boolean actualizarUsuario(Usuario usuario) {
        try {
            if (usuario instanceof Estudiante) {
                Estudiante estudiante = (Estudiante) usuario;
                return utilRed.actualizarEstudiante(estudiante);
            } else {
                System.err.println("Error: El usuario no es un Estudiante");
                return false;
            }
        } catch (Exception e) {
            System.err.println("Error al actualizar usuario: " + e.getMessage());
            return false;
        }
    }

    public boolean eliminarUsuario(String usuarioId) {
        try {
            Usuario usuario = buscarUsuario(usuarioId);
            if (usuario == null) {
                return false;
            }

            if (usuario instanceof Estudiante) {
                grafoAfinidad.removerEstudiante((Estudiante) usuario);
            }

            eliminarContenidosUsuario(usuarioId);
            eliminarSolicitudesUsuario(usuarioId);

            return utilRed.eliminarUsuario(usuarioId);
        } catch (Exception e) {
            System.err.println("Error al eliminar usuario: " + e.getMessage());
            return false;
        }
    }

    private void eliminarContenidosUsuario(String usuarioId) {
        List<Contenido> contenidos = arbolContenidos.obtenerTodosEnOrden(obtenerContenidosSinArbol());
        List<Contenido> aEliminar = new ArrayList<>();

        for (Contenido contenido : contenidos) {
            if (contenido.getAutor().equals(usuarioId)) {
                aEliminar.add(contenido);
            }
        }

        for (Contenido contenido : aEliminar) {
            arbolContenidos.eliminar(contenido);
            utilRed.eliminarContenido(contenido.getId());
        }
    }

    private void eliminarSolicitudesUsuario(String usuarioId) {
        PriorityQueue<SolicitudAyuda> nuevaCola = new PriorityQueue<>(Comparator.comparingInt(s -> s.getUrgencia().ordinal()));

        while (!colaSolicitudes.isEmpty()) {
            SolicitudAyuda solicitud = colaSolicitudes.poll();
            if (!solicitud.getSolicitanteId().equals(usuarioId)) {
                nuevaCola.add(solicitud);
            } else {
                utilRed.eliminarSolicitud(solicitud.getId());
            }
        }

        this.colaSolicitudes = nuevaCola;
    }
    public boolean crearContenido(String estudianteId, String titulo, String descripcion,
                                  TipoContenido tipo, String tema, String contenidoRuta) throws OperacionFallidaException {
        Objects.requireNonNull(estudianteId, "ID de estudiante no puede ser nulo");
        Objects.requireNonNull(titulo, "Título no puede ser nulo");
        Objects.requireNonNull(tipo, "Tipo de contenido no puede ser nulo");
        Objects.requireNonNull(tema, "Tema no puede ser nulo");
        Objects.requireNonNull(contenidoRuta, "Ruta/URL de contenido no puede ser nula");
        Objects.requireNonNull(descripcion, "Descripción no puede ser nula");

        if (estudianteId.trim().isEmpty() || titulo.trim().isEmpty()) {
            throw new IllegalArgumentException("ID de estudiante y título no pueden estar vacíos");
        }

        if (tipo == TipoContenido.ENLACE && !contenidoRuta.matches("^(https?|ftp)://.*$")) {
            throw new IllegalArgumentException("Formato de enlace inválido. Debe comenzar con http://, https:// o ftp://");
        }

        Usuario usuario = buscarUsuario(estudianteId);
        if (!(usuario instanceof Estudiante)) {
            throw new IllegalArgumentException("El ID no corresponde a un estudiante registrado");
        }
        Estudiante estudiante = (Estudiante) usuario;

        Contenido nuevoContenido = new Contenido(
                null,
                titulo,
                estudiante.getNombre(),
                LocalDateTime.now(),
                tipo,
                descripcion,
                tema,
                contenidoRuta,
                null
        );

        estudiante.agregarContenido(nuevoContenido.getId());
        arbolContenidos.insertar(nuevoContenido);
        utilRed.guardarContenido(nuevoContenido);

        return true;
    }

    public void cambiarCriterioOrden(CriterioOrden nuevoCriterio) {
        Objects.requireNonNull(nuevoCriterio, "El criterio no puede ser nulo");

        List<Contenido> contenidos = arbolContenidos.obtenerTodosEnOrden(obtenerContenidosSinArbol());
        this.arbolContenidos = new ArbolContenidos(nuevoCriterio);

        for (Contenido contenido : contenidos) {
            arbolContenidos.insertar(contenido);
        }
    }

    // Métodos de búsqueda de contenidos
    public List<Contenido> getContenidoPorAutor(String autor) {
        Objects.requireNonNull(autor, "El autor no puede ser nulo");
        return arbolContenidos.buscarPorAutor(autor);
    }

    public List<Contenido> getContenidoPorTema(String tema) {
        Objects.requireNonNull(tema, "El tema no puede ser nulo");
        return arbolContenidos.buscarPorTema(tema);
    }

    public List<Contenido> getContenidoPorTipo(TipoContenido tipo) {
        Objects.requireNonNull(tipo, "El tipo no puede ser nulo");
        List<Contenido> todos = arbolContenidos.obtenerTodosEnOrden(obtenerContenidosSinArbol());
        List<Contenido> filtrados = new ArrayList<>();

        for (Contenido contenido : todos) {
            if (contenido.getTipo() == tipo) {
                filtrados.add(contenido);
            }
        }

        return filtrados;
    }

    public List<Contenido> obtenerContenidosPorEstudiante(String userId) {
        List<Contenido> contenidos = new ArrayList<>();
        for (Contenido contenido : arbolContenidos.obtenerTodosEnOrden(obtenerContenidosSinArbol())) {
            if (contenido.getAutor().equals(userId)) {
                contenidos.add(contenido);
            }
        }
        return contenidos;
    }

    // Métodos de gestión de solicitudes
    public void agregarSolicitud(SolicitudAyuda solicitud) {
        validarSolicitud(solicitud);
        colaSolicitudes.add(solicitud);
        utilRed.guardarSolicitud(solicitud);
    }

    public boolean crearSolicitud(SolicitudAyuda solicitud) {
        if (solicitud == null || solicitud.getSolicitanteId() == null) {
            throw new IllegalArgumentException("La solicitud o el ID del solicitante no pueden ser nulos");
        }

        colaSolicitudes.add(solicitud);
        utilRed.guardarSolicitud(solicitud);
        return true;
    }

    public SolicitudAyuda atenderSolicitud() {
        SolicitudAyuda solicitud = colaSolicitudes.poll();
        if (solicitud != null) {
            utilRed.actualizarEstadoSolicitud(solicitud.getId(), Estado.RESUELTA);
        }
        return solicitud;
    }

    // Métodos de recomendaciones y afinidad
    public void actualizarAfinidad(Estudiante e1, Estudiante e2, int incremento) {
        int pesoActual = grafoAfinidad.obtenerPesoAfinidad(e1, e2);
        actualizarRelacionAfinidad(e1, e2, pesoActual + incremento);
    }

    public List<Estudiante> obtenerRecomendacionesAmplias(String idEstudiante, int limite) {
        Usuario usuario = buscarUsuario(idEstudiante);
        if (!(usuario instanceof Estudiante)) {
            throw new IllegalArgumentException("El usuario no es un estudiante");
        }
        return grafoAfinidad.obtenerRecomendaciones((Estudiante) usuario, limite);
    }

    public List<Estudiante> generarRecomendaciones(String idUsuario, int limite) {
        Usuario usuario = buscarUsuario(idUsuario);
        if (!(usuario instanceof Estudiante)) {
            throw new IllegalArgumentException("El usuario no es un estudiante");
        }
        return grafoAfinidad.obtenerRecomendaciones((Estudiante) usuario, limite);
    }

    // Métodos de grupos de estudio
    public List<GrupoEstudio> formarGruposAutomaticos(List<Usuario> usuarios) throws OperacionFallidaException {
        System.out.println("[DEBUG] Iniciando formación de grupos...");

        // 1. Filtrar estudiantes válidos (con intereses)
        List<Estudiante> estudiantesValidos = usuarios.stream()
                .filter(u -> u instanceof Estudiante)
                .map(u -> (Estudiante) u)
                .filter(e -> e.getIntereses() != null && !e.getIntereses().isEmpty())
                .collect(Collectors.toList());

        System.out.println("[DEBUG] Estudiantes válidos: " + estudiantesValidos.size());
        if (estudiantesValidos.isEmpty()) {
            System.out.println("[WARN] No hay estudiantes válidos para formar grupos");
            return Collections.emptyList();
        }

        // 2. Mapa de intereses
        Map<String, List<Estudiante>> interesAEstudiantes = new HashMap<>();
        Set<String> gruposUnicos = new HashSet<>();

        // 3. Formar grupos
        List<GrupoEstudio> gruposFormados = new ArrayList<>();

        for (Estudiante estudiante : estudiantesValidos) {
            for (String interes : estudiante.getIntereses()) {
                interesAEstudiantes.computeIfAbsent(interes, k -> new ArrayList<>()).add(estudiante);
            }
        }

        for (Map.Entry<String, List<Estudiante>> entry : interesAEstudiantes.entrySet()) {
            String interes = entry.getKey();
            List<Estudiante> miembros = entry.getValue();

            if (miembros.size() < 3) {
                System.out.println("[DEBUG] No suficientes miembros (" + miembros.size() + ") para: " + interes);
                continue;
            }

            // Crear clave única basada en miembros e interés
            String claveUnica = interes + "_" + miembros.stream()
                    .map(Estudiante::getId)
                    .sorted()
                    .collect(Collectors.joining("_"));

            // Verificar si el grupo ya existe en esta ejecución
            if (gruposUnicos.contains(claveUnica)) {
                System.out.println("[DEBUG] Grupo duplicado detectado: " + claveUnica);
                continue;
            }

            // Crear lista de IDs de miembros
            LinkedList<String> idsMiembros = miembros.stream()
                    .map(Estudiante::getId)
                    .collect(Collectors.toCollection(LinkedList::new));

            try {
                // Crear nuevo grupo usando el constructor adecuado
                GrupoEstudio grupo = new GrupoEstudio(
                        null, // ID será generado automáticamente
                        "Grupo de " + interes + " #" + (gruposFormados.size() + 1),
                        "Grupo para " + interes + " con " + miembros.size() + " miembros",
                        idsMiembros,
                        new LinkedList<>(), // Lista vacía de contenidos
                        new Date()
                );

                // Establecer el interés (si tu clase tiene este campo)
                // grupo.setInteres(interes); // Descomentar si existe este método

                // Registrar grupo
                gruposFormados.add(grupo);
                gruposUnicos.add(claveUnica);

                System.out.println("[DEBUG] Nuevo grupo creado: " + grupo.getIdGrupo());
            } catch (OperacionFallidaException e) {
                System.err.println("Error al crear grupo: " + e.getMessage());
            }
        }

        return gruposFormados;
    }

    private void actualizarRelacionAfinidad(Estudiante estudiante, Estudiante estudiante1) {
        int pesoActual = grafoAfinidad.obtenerPesoAfinidad(estudiante, estudiante1);
        grafoAfinidad.establecerAfinidad(estudiante, estudiante1, pesoActual + 1);
        System.out.printf("[DEBUG] Afinidad actualizada entre %s y %s: %d%n",
                estudiante.getNombre(), estudiante1.getNombre(), pesoActual + 1);
    }

    private GrupoEstudio crearGrupoPorInteres(List<Estudiante> miembros, String interes, int numeroGrupo)
            throws OperacionFallidaException {

        // 1. Validaciones exhaustivas
        final int MIN_MIEMBROS = 3;
        final int MAX_MIEMBROS = 5;

        if (miembros == null) {
            throw new OperacionFallidaException("La lista de miembros no puede ser nula");
        }

        if (miembros.size() < MIN_MIEMBROS) {
            throw new OperacionFallidaException(
                    String.format("Se requieren al menos %d miembros para formar un grupo. Actual: %d",
                            MIN_MIEMBROS, miembros.size())
            );
        }

        if (miembros.size() > MAX_MIEMBROS) {
            throw new OperacionFallidaException(
                    String.format("Un grupo no puede tener más de %d miembros. Actual: %d",
                            MAX_MIEMBROS, miembros.size())
            );
        }

        if (interes == null || interes.trim().isEmpty()) {
            throw new OperacionFallidaException("El interés del grupo no puede estar vacío");
        }

        // 2. Preparar datos del grupo
        String nombreGrupo = String.format("Grupo de %s #%d", interes, numeroGrupo);
        String descripcion = String.format("Grupo autogenerado para %s con %d miembros",
                interes, miembros.size());

        LinkedList<String> idsMiembros = miembros.stream()
                .map(Estudiante::getId)
                .collect(Collectors.toCollection(LinkedList::new));

        // 3. Crear instancia del grupo
        GrupoEstudio grupo;
        try {
            grupo = new GrupoEstudio(
                    null, // ID autogenerado
                    nombreGrupo,
                    descripcion,
                    idsMiembros,
                    new LinkedList<>(), // Lista vacía de contenidos
                    new Date()
            );
        } catch (Exception e) {
            throw new OperacionFallidaException("Error al crear el grupo: " + e.getMessage());
        }

        // 4. Actualizar relaciones de afinidad (completo)
        for (int i = 0; i < miembros.size(); i++) {
            for (int j = i + 1; j < miembros.size(); j++) {
                actualizarRelacionAfinidad(miembros.get(i).getId(), miembros.get(j).getId());
            }
        }

        // 5. Persistir y retornar
        utilRed.guardarGrupo(grupo);
        System.out.printf("[DEBUG] Grupo creado: %s (%d miembros)%n",
                nombreGrupo, miembros.size());

        return grupo;
    }


    private void actualizarRelacionAfinidad(String id1, String id2) {
        try {
            Usuario usuario1 = buscarUsuario(id1);
            Usuario usuario2 = buscarUsuario(id2);

            if (usuario1 instanceof Estudiante && usuario2 instanceof Estudiante) {
                Estudiante e1 = (Estudiante) usuario1;
                Estudiante e2 = (Estudiante) usuario2;

                // Obtener peso actual y aumentar en 1
                int pesoActual = grafoAfinidad.obtenerPesoAfinidad(e1, e2);
                grafoAfinidad.establecerAfinidad(e1, e2, pesoActual + 1);

                // Actualizar intereses comunes
                actualizarInteresesCompartidos(e1, e2);
            }
        } catch (Exception e) {
            System.out.println("Error actualizando afinidad entre " + id1 + " y " + id2 + ": " + e.getMessage());
        }
    }

    /**
     * Detecta comunidades de estudiantes en el grafo
     */
    public List<List<Estudiante>> detectarComunidades() {
        return grafoAfinidad.detectarComunidades();
    }

    /**
     * Encuentra el camino más corto entre dos estudiantes
     */
    public List<Estudiante> encontrarCaminoAfinidad(String idOrigen, String idDestino) {
        Estudiante origen = (Estudiante) buscarUsuario(idOrigen);
        Estudiante destino = (Estudiante) buscarUsuario(idDestino);
        return grafoAfinidad.encontrarCaminoMasCorto(origen, destino);
    }

    /**
     * Muestra el estado actual del grafo de afinidad
     */
    public void mostrarEstadoGrafoAfinidad() {
        grafoAfinidad.mostrarEstado();
    }

    private void actualizarInteresesCompartidos(Estudiante e1, Estudiante e2) throws OperacionFallidaException {
        String interesGrupo = "Colaboración en Grupo";
        e1.agregarInteres(interesGrupo);
        e2.agregarInteres(interesGrupo);
        utilRed.actualizarEstudiante(e1);
        utilRed.actualizarEstudiante(e2);
    }

    // Métodos de obtención de datos
    public Usuario buscarUsuario(String idUsuario) {
        return usuarios.stream()
                .filter(u -> u.getId().equals(idUsuario))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
    }

    public Usuario iniciarSesion(String correo, String contrasenia) {
        return utilRed.iniciarSesion(correo, contrasenia);
    }

    public List<Usuario> obtenerUsuarios() {
        return utilRed.obtenerUsuarios();
    }

    public List<Contenido> obtenerContenidos() {
        return utilRed.obtenerContenidos();
    }

    public List<Contenido> obtenerContenidosSinArbol() {
        return utilRed.obtenerContenidos();
    }

    public List<Contenido> getContenidosOrdenados() {
        return Collections.unmodifiableList(this.arbolContenidos.obtenerTodosEnOrden(utilRed.obtenerContenidos()));
    }

    /**
     * Obtiene todos los contenidos combinando los del árbol con los de utilRed
     * @return Lista combinada y no modificable de todos los contenidos
     */
    public List<Contenido> obtenerTodosContenidos() {
        try {
            List<Contenido> contenidosExternos = Optional.ofNullable(utilRed.obtenerContenidos())
                    .orElseGet(ArrayList::new);

            List<Contenido> contenidosArbol = arbolContenidos.obtenerTodosEnOrden(utilRed.obtenerContenidos());

            // Simplificar la combinación
            List<Contenido> todosContenidos = new ArrayList<>(contenidosArbol);
            contenidosExternos.stream()
                    .filter(c -> !todosContenidos.contains(c))
                    .forEach(todosContenidos::add);

            System.out.println("Total de contenidos obtenidos: " + todosContenidos.size());
            return todosContenidos;
        } catch (Exception e) {
            System.err.println("Error al obtener contenidos: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    // Versión mejorada de obtenerTodasSolicitudes()
    public List<SolicitudAyuda> obtenerTodasSolicitudes() {
        // 1. Crear lista combinada
        List<SolicitudAyuda> todasSolicitudes = new ArrayList<>();

        // 2. Agregar solicitudes de utilRed primero
        List<SolicitudAyuda> solicitudesPersistidas = utilRed.obtenerSolicitudes();
        if (solicitudesPersistidas != null) {
            todasSolicitudes.addAll(solicitudesPersistidas);
        }

        // 3. Agregar solicitudes en memoria (sin vaciar la cola)
        todasSolicitudes.addAll(new ArrayList<>(colaSolicitudes));

        // 4. Ordenar por urgencia (para mantener prioridad)
        todasSolicitudes.sort(Comparator.comparingInt(s -> s.getUrgencia().ordinal()));

        return todasSolicitudes;
    }

    public List<String> obtenerGruposEstudio(String userId) throws OperacionFallidaException {
        List<String> gruposEstudio = new ArrayList<>();
        for (GrupoEstudio grupo : utilRed.obtenerGrupos()) {
            if (grupo.getIdMiembros().contains(userId)) {
                gruposEstudio.add(grupo.getNombre());
            }
        }
        return gruposEstudio;
    }

    // Métodos de estadísticas
    public String obtenerTotalUsuarios() {
        return String.valueOf(usuarios.size());
    }

    public String obtenerTotalContenidos() {
        return String.valueOf(arbolContenidos.obtenerTodosEnOrden(obtenerContenidosSinArbol()).size());
    }

    public String obtenerTotalSolicitudes() {
        return String.valueOf(colaSolicitudes.size());
    }

    public String obtenerTotalContenidosUsuario(String userId) {
        List<Contenido> contenidos = obtenerContenidosPorEstudiante(userId);
        return String.valueOf(contenidos.size());
    }

    public String obtenerTotalSolicitudesUsuario(String userId) {
        List<SolicitudAyuda> solicitudes = new ArrayList<>();
        for (SolicitudAyuda solicitud : colaSolicitudes) {
            if (solicitud.getSolicitanteId().equals(userId)) {
                solicitudes.add(solicitud);
            }
        }
        return String.valueOf(solicitudes.size());
    }

    // Getters
    public String getNombre() {
        return this.nombre;
    }

    public List<Usuario> getUsuarios() {
        return new ArrayList<>(this.usuarios);
    }

    public PriorityQueue<SolicitudAyuda> getSolicitudesAyuda() {
        return new PriorityQueue<>(this.colaSolicitudes);
    }

    public GrafoAfinidad getGrafoAfinidad() {
        return this.grafoAfinidad;
    }

    // Métodos de validación
    private void validarUsuario(Usuario usuario) {
        Objects.requireNonNull(usuario, "El usuario no puede ser nulo");
        if (usuario.getNombre() == null || usuario.getCorreo() == null) {
            throw new IllegalArgumentException("Nombre y correo son obligatorios");
        }
    }

    private void validarSolicitud(SolicitudAyuda solicitud) {
        Objects.requireNonNull(solicitud, "La solicitud no puede ser nula");
        if (!usuarios.stream().anyMatch(u -> u.getId().equals(solicitud.getSolicitanteId()))) {
            throw new IllegalArgumentException("El solicitante no existe");
        }
    }

    public double obtenerPromedioValoraciones(String contenidoId) {
        Contenido contenido = arbolContenidos.buscarPorId(contenidoId);
        if (contenido == null) {
            throw new IllegalArgumentException("Contenido no encontrado");
        }
        return contenido.obtenerPromedioValoracion();
    }

    public int obtenerTotalValoraciones(String contenidoId) {
        Contenido contenido = arbolContenidos.buscarPorId(contenidoId);
        if (contenido == null) {
            throw new IllegalArgumentException("Contenido no encontrado");
        }
        return contenido.getValoraciones().size();
    }


    public boolean usuarioYaValoroContenido(String usuarioId, String contenidoId) {
        Contenido contenido = arbolContenidos.buscarPorId(contenidoId);
        if (contenido == null) {
            throw new IllegalArgumentException("Contenido no encontrado");
        }
        return contenido.getValoraciones().stream()
                .anyMatch(v -> v.getIdAutor().equals(usuarioId));
    }

    public Contenido buscarContenido(String contenidoId) {
        // Input validation
        if (contenidoId == null || contenidoId.trim().isEmpty()) {
            throw new IllegalArgumentException("ID de contenido inválido");
        }

        // 1. Try tree search first
        Contenido contenido = arbolContenidos.buscarPorId(contenidoId);

        // 2. If not found, try database fallback
        if (contenido == null) {
            System.out.println("[DEBUG] Contenido no encontrado en árbol, intentando base de datos");
            contenido = utilRed.obtenerContenidoPorId(contenidoId);

            if (contenido != null) {
                // Optional: Add to tree for future accesses
                arbolContenidos.insertar(contenido);
                System.out.println("[DEBUG] Contenido cargado desde base de datos");
            }
        }

        if (contenido == null) {
            throw new IllegalArgumentException("El contenido solicitado no existe");
        }

        // Load ratings
        try {
            List<Valoracion> valoraciones = cargarValoracionesParaContenido(contenidoId);
            contenido.setValoraciones(new LinkedList<>(valoraciones));
        } catch (Exception e) {
            System.err.println("Error cargando valoraciones, continuando sin ellas");
            contenido.setValoraciones(new LinkedList<>());
        }

        return contenido;
    }

    public List<Valoracion> cargarValoracionesParaContenido(String contenidoId) {
        try {
            // 1. Verificar parámetro de entrada
            Objects.requireNonNull(contenidoId, "El ID de contenido no puede ser nulo");
            if (contenidoId.trim().isEmpty()) {
                throw new IllegalArgumentException("El ID de contenido no puede estar vacío");
            }

            System.out.println("Cargando valoraciones para contenido: " + contenidoId);

            // 2. Buscar el contenido en la persistencia
            Contenido contenido = utilRed.buscarContenidoPorId(contenidoId);
            if (contenido == null) {
                System.out.println("No se encontró contenido con ID: " + contenidoId);
                return Collections.emptyList();
            }

            // 3. Obtener valoraciones directamente del contenido
            List<Valoracion> valoraciones = contenido.getValoraciones();

            // 4. Loggear resultados
            System.out.println("Encontradas " + valoraciones.size() + " valoraciones para contenido: " + contenidoId);

            // 5. Retornar copia defensiva
            return new ArrayList<>(valoraciones);

        } catch (Exception e) {
            System.out.println("Error al cargar valoraciones para contenido " + contenidoId + ": " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public boolean agregarValoracion(String contenidoId, Valoracion valoracion) {
        Contenido contenido = arbolContenidos.buscarPorId(contenidoId);
        if (contenido == null) {
            throw new IllegalArgumentException("Contenido no encontrado");
        }
        contenido.agregarValoracion(valoracion);
        utilRed.guardarValoracion(contenidoId, valoracion);
        return true;
    }

    public List<Contenido> obtenerContenidosPorUsuario(String userId) {
        //obtener contenidos por usuario
        List<Contenido> contenidos = new ArrayList<>();
        for (Contenido contenido : arbolContenidos.obtenerTodosEnOrden(obtenerContenidosSinArbol())) {
            if (contenido.getAutor().equals(userId)) {
                contenidos.add(contenido);
            }
        }
        return contenidos;
    }

    public List<SolicitudAyuda> obtenerSolicitudesPorUsuario(String userId) {
        //obtener solicitudes por usuario
        List<SolicitudAyuda> solicitudes = new ArrayList<>();
        for (SolicitudAyuda solicitud : colaSolicitudes) {
            if (solicitud.getSolicitanteId().equals(userId)) {
                solicitudes.add(solicitud);
            }
        }
        return solicitudes;
    }

    public List<Estudiante> obtenerSugerenciasCompaneros(String userId) {
        try {
            Usuario usuario = buscarUsuario(userId);
            if (!(usuario instanceof Estudiante)) {
                throw new IllegalArgumentException("El usuario no es un estudiante");
            }
            // Añadir un parámetro para el límite de recomendaciones (ej. 10)
            return grafoAfinidad.obtenerRecomendaciones((Estudiante) usuario, 10);
        } catch (Exception e) {
            System.out.println("Error obteniendo sugerencias de compañeros: " + e.getMessage());
            return Collections.emptyList();
        }
    }


    public String obtenerGruposEstudioPorUsuario(String id) throws OperacionFallidaException {
        // Se inicializa la lista de grupos
        List<GrupoEstudio> grupos = utilRed.obtenerGrupos();

        StringBuilder sb = new StringBuilder();
        // Se hace un ciclo para buscar los grupos a los que pertenece el usuario
        for (GrupoEstudio grupo : grupos) {
            // Si el grupo contiene al usuario, se agrega a la lista
            if (grupo.getIdMiembros().contains(id)) {
                sb.append(grupo.getNombre()).append(", ");
            }
        }
        // Se eliminan los últimos dos caracteres (", "), si existen
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 2); // Eliminar la última coma y espacio
        }
        // Se retorna la lista de grupos
        return sb.toString();
    }

    public List<Usuario> obtenerTodosUsuarios() {
        return utilRed.obtenerUsuarios();
    }

    /**
     * Actualiza la relación de afinidad entre dos estudiantes
     */
    public void actualizarRelacionAfinidad(Estudiante e1, Estudiante e2, int nuevoPeso) {
        Objects.requireNonNull(e1, "El estudiante 1 no puede ser nulo");
        Objects.requireNonNull(e2, "El estudiante 2 no puede ser nulo");

        grafoAfinidad.establecerAfinidad(e1, e2, nuevoPeso);
        System.out.println("Relación de afinidad actualizada entre " + e1.getId() + " y " + e2.getId() + " con peso " + nuevoPeso);
    }

    /**
     * Obtiene el peso de afinidad entre dos estudiantes
     */
    public int obtenerPesoAfinidad(String idEstudiante1, String idEstudiante2) {
        Estudiante e1 = (Estudiante) buscarUsuario(idEstudiante1);
        Estudiante e2 = (Estudiante) buscarUsuario(idEstudiante2);
        return grafoAfinidad.obtenerPesoAfinidad(e1, e2);
    }

    /**
     * Calcula y actualiza todas las afinidades en el grafo
     */
    public void calcularAfinidades() {
        grafoAfinidad.calcularAfinidades();
        System.out.println("Afinidades recalculadas para todos los estudiantes");
    }

    /**
     * Obtiene estadísticas del grafo de afinidad
     */
    public String obtenerEstadisticasAfinidad() {
        return grafoAfinidad.obtenerEstadisticas();
    }

    public boolean suspenderUsuario(String correo, int dias) {
        Usuario usuario = utilRed.buscarUsuarioPorCorreo(correo);
        if (usuario != null) {
            usuario.setSuspendido(true);
            usuario.setDiasSuspension(dias);
            utilRed.modificarUsuario(usuario);
            return true;
        }
        return false;
    }

    public boolean eliminarContenido(String contenidoId) {
        // Validación de entrada más robusta
        if (contenidoId == null || contenidoId.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID de contenido no puede ser nulo o vacío");
        }

        System.out.println("[DEBUG] Iniciando proceso de eliminación para contenido ID: " + contenidoId);

        try {
            // 1. Verificar existencia primero
            boolean existeEnArbol = arbolContenidos.buscarPorId(contenidoId) != null;
            boolean existeEnDB = utilRed.existeContenido(contenidoId);

            if (!existeEnArbol && !existeEnDB) {
                System.out.println("[WARN] Eliminación fallida: El contenido no existe en ningún almacenamiento");
                return false;
            }

            // 2. Eliminar de ambos lugares independientemente
            boolean eliminadoDelArbol = true; // Asumir éxito si no estaba en árbol
            if (existeEnArbol) {
                System.out.println("[DEBUG] Eliminando del árbol binario");
                eliminadoDelArbol = arbolContenidos.eliminar(buscarContenido(contenidoId)); // Versión simplificada
            }

            System.out.println("[DEBUG] Eliminando de base de datos");
            boolean eliminadoDeDB = utilRed.eliminarContenidoPorId(contenidoId); // Método mejorado

            // 3. Determinar resultado
            if (existeEnArbol && !eliminadoDelArbol) {
                System.out.println("[ERROR] Falló eliminación del árbol para contenido existente");
                return false;
            }

            if (!eliminadoDeDB) {
                System.out.println("[WARN] El contenido no pudo ser eliminado de la base de datos");
                // Puede considerarse éxito si al menos se eliminó del árbol
                return existeEnArbol && eliminadoDelArbol;
            }

            System.out.println("[INFO] Eliminación exitosa de todos los almacenamientos");
            return true;

        } catch (IllegalArgumentException e) {
            System.err.println("[ERROR] Problema con el ID del contenido: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("[ERROR CRÍTICO] Error inesperado al eliminar: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean reactivarUsuario(String correo) {
        Usuario usuario = utilRed.buscarUsuarioPorCorreo(correo);
        if (usuario != null && usuario.isSuspendido()) {
            usuario.setSuspendido(false);
            usuario.setDiasSuspension(0);
            utilRed.modificarUsuario(usuario);
            return true;
        }
        return false;
    }

    public boolean actualizarContenido(Contenido contenidoActualizado) {
        Objects.requireNonNull(contenidoActualizado, "El contenido no puede ser nulo");
        Contenido contenidoExistente = arbolContenidos.buscarPorId(contenidoActualizado.getId());
        if (contenidoExistente != null) {
            arbolContenidos.modificar(contenidoActualizado);
            utilRed.modificarContenido(contenidoActualizado);
            return true;
        }
        return false;
    }

    public List<GrupoEstudio> obtenerTodosGrupos() throws OperacionFallidaException {
        return utilRed.obtenerGrupos();
    }

    public void limpiarGruposDuplicados() {
        Map<String, GrupoEstudio> gruposUnicos = new HashMap<>();
        List<GrupoEstudio> gruposAEliminar = new ArrayList<>();

        for (GrupoEstudio grupo : this.gruposEstudio) {
            String claveUnica = generarClaveUnica(grupo);
            if (gruposUnicos.containsKey(claveUnica)) {
                gruposAEliminar.add(grupo);
                LOGGER.warning("Grupo duplicado detectado: " + grupo.getId());
            } else {
                gruposUnicos.put(claveUnica, grupo);
            }
        }

        gruposAEliminar.forEach(grupo -> {
            this.gruposEstudio.remove(grupo);
            LOGGER.info("Grupo eliminado: " + grupo.getId());
        });

        this.reconstruirIndicesGrupos();
    }

    private String generarClaveUnica(GrupoEstudio grupo) {
        List<String> miembrosOrdenados = new ArrayList<>(grupo.getIdMiembros());
        Collections.sort(miembrosOrdenados);
        return grupo.getNombre() + "|" + String.join(",", miembrosOrdenados);
    }

    public void validarIntegridadGrupos() {
        this.gruposEstudio.forEach(grupo -> {

            grupo.getIdMiembros().removeIf(idMiembro ->
                    this.buscarUsuario(idMiembro) == null
            );


            if (!grupo.getId().startsWith("GRP-") && !grupo.getId().matches("\\d+")) {
                LOGGER.severe("ID de grupo inválido: " + grupo.getId());
            }

        });
    }

    public void consolidarGrupos() {

        Map<String, List<GrupoEstudio>> gruposPorClave = this.gruposEstudio.stream()
                .collect(Collectors.groupingBy(this::generarClaveUnica));


        gruposPorClave.values().stream()
                .filter(lista -> lista.size() > 1)
                .forEach(this::procesarDuplicados);
    }

    private void procesarDuplicados(List<GrupoEstudio> duplicados) {

        GrupoEstudio grupoPrincipal = duplicados.stream()
                .max(Comparator.comparing(g -> g.getFechaCreacion().getTime())) // Convertir Date a long
                .orElseThrow(() -> new IllegalStateException("No se pudo determinar el grupo principal"));
        duplicados.stream()
                .filter(g -> !g.equals(grupoPrincipal))
                .forEach(grupo -> {
                    if (!grupo.getContenidos().isEmpty()) {
                        LOGGER.info(String.format(
                                "Transfiriendo %d contenidos de %s a %s",
                                grupo.getContenidos().size(),
                                grupo.getId(),
                                grupoPrincipal.getId()
                        ));
                        grupoPrincipal.getContenidos().addAll(grupo.getContenidos());
                    }

                    transferirMensajes(grupo, grupoPrincipal);

                    LOGGER.info(String.format(
                            "Consolidado grupo %s (%s) en %s (%s)",
                            grupo.getId(),
                            new Date(grupo.getFechaCreacion().getTime()),
                            grupoPrincipal.getId(),
                            new Date(grupoPrincipal.getFechaCreacion().getTime())
                    ));

                    this.gruposEstudio.remove(grupo);
                });
    }

    private void transferirMensajes(GrupoEstudio origen, GrupoEstudio destino) {
        if (origen.getMensajes() != null && !origen.getMensajes().isEmpty()) {
            LOGGER.info(String.format(
                    "Transfiriendo %d mensajes de %s a %s",
                    origen.getMensajes().size(),
                    origen.getId(),
                    destino.getId()
            ));
            destino.getMensajes().addAll(origen.getMensajes());
        }
    }

    /**
     * Reconstruye todos los índices y estructuras auxiliares de grupos
     */
    public void reconstruirIndicesGrupos() {
        LOGGER.info("Iniciando reconstrucción de índices de grupos...");
        long inicio = System.currentTimeMillis();

        try {
            mapaGruposPorId.clear();
            gruposEstudio.forEach(grupo -> {
                if (grupo.getId() != null && !grupo.getId().isEmpty()) {
                    mapaGruposPorId.put(grupo.getId(), grupo);
                } else {
                    LOGGER.warning("Grupo con ID nulo o vacío encontrado: " + grupo.getNombre());
                }
            });
            miembrosPorGrupo.clear();
            for (GrupoEstudio grupo : gruposEstudio) {
                for (String idMiembro : grupo.getIdMiembros()) {
                    miembrosPorGrupo.computeIfAbsent(grupo.getId(), k -> new HashSet<>())
                            .add(idMiembro);
                }
            }

            gruposPorMiembro.clear();
            for (GrupoEstudio grupo : gruposEstudio) {
                for (String idMiembro : grupo.getIdMiembros()) {
                    gruposPorMiembro.computeIfAbsent(idMiembro, k -> new HashSet<>())
                            .add(grupo.getId());
                }
            }

            long duracion = System.currentTimeMillis() - inicio;
            LOGGER.log(Level.INFO, "Índices reconstruidos. Grupos: {0}, Miembros indexados: {1}, Tiempo: {2}ms",
                    new Object[]{
                            gruposEstudio.size(),
                            miembrosPorGrupo.values().stream().mapToInt(Set::size).sum(),
                            duracion
                    });
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error reconstruyendo índices: " + e.getMessage(), e);
        }
    }

    public boolean eliminarContenidoDeGrupo(String grupoId, String contenidoId) {
        // Validación de entrada
        if (grupoId == null || grupoId.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID del grupo no puede ser nulo o vacío");
        }
        if (contenidoId == null || contenidoId.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID del contenido no puede ser nulo o vacío");
        }

        // Buscar el grupo
        GrupoEstudio grupo = mapaGruposPorId.get(grupoId);
        if (grupo == null) {
            throw new IllegalArgumentException("Grupo no encontrado con ID: " + grupoId);
        }

        // Buscar el contenido en el grupo
        Contenido contenido = arbolContenidos.buscarPorId(contenidoId);
        if (contenido == null) {
            throw new IllegalArgumentException("Contenido no encontrado con ID: " + contenidoId);
        }

        // Eliminar el contenido del grupo
        boolean eliminado = grupo.getContenidos().removeIf(c -> false);
        System.out.println("[WARN] El contenido no estaba presente en el grupo: " + grupoId);
        return false;
    }

    public GrupoEstudio buscarGrupoEstudio(String grupoId) {
        if (mapaGruposPorId == null) {
            throw new IllegalStateException("mapaGruposPorId no ha sido inicializado");
        }
        return mapaGruposPorId.get(grupoId);
    }

    public boolean unirUsuarioAGrupo(String usuarioId, String grupoId) {
        // Validación de entrada
        if (usuarioId == null || usuarioId.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID del usuario no puede ser nulo o vacío");
        }
        if (grupoId == null || grupoId.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID del grupo no puede ser nulo o vacío");
        }

        // Buscar el usuario
        Usuario usuario = buscarUsuario(usuarioId);
        if (usuario == null) {
            throw new IllegalArgumentException("Usuario no encontrado con ID: " + usuarioId);
        }

        // Buscar el grupo
        GrupoEstudio grupo = buscarGrupoEstudio(grupoId);
        if (grupo == null) {
            throw new IllegalArgumentException("Grupo no encontrado con ID: " + grupoId);
        }

        // Agregar el usuario al grupo
        if (!grupo.getIdMiembros().contains(usuarioId)) {
            grupo.getIdMiembros().add(usuarioId);
            LOGGER.log(Level.INFO, "Usuario {0} unido al grupo {1}", new Object[]{usuarioId, grupoId});
            return true;
        } else {
            LOGGER.log(Level.WARNING, "El usuario {0} ya está en el grupo {1}", new Object[]{usuarioId, grupoId});
            return false;
        }
    }

    public boolean actualizarGrupo(GrupoEstudio grupo) {
        Objects.requireNonNull(grupo, "El grupo no puede ser nulo");
        if (grupo.getId() == null || grupo.getId().trim().isEmpty()) {
            throw new IllegalArgumentException("El ID del grupo no puede ser nulo o vacío");
        }

        // Verificar si el grupo existe
        GrupoEstudio grupoExistente = buscarGrupoEstudio(grupo.getId());
        if (grupoExistente == null) {
            throw new IllegalArgumentException("Grupo no encontrado con ID: " + grupo.getId());
        }

        // Actualizar los datos del grupo
        grupoExistente.setNombre(grupo.getNombre());
        grupoExistente.setDescripcion(grupo.getDescripcion());

        grupoExistente.setFechaCreacion(grupo.getFechaCreacion());

        // Guardar los cambios en la base de datos
        utilRed.modificarGrupo(grupoExistente);
        LOGGER.log(Level.INFO, "Grupo {0} actualizado correctamente", grupo.getId());
        return true;
    }

    public boolean eliminarGrupo(String grupoId) {
        // Validación de entrada
        if (grupoId == null || grupoId.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID del grupo no puede ser nulo o vacío");
        }

        // Buscar el grupo
        GrupoEstudio grupo = buscarGrupoEstudio(grupoId);
        if (grupo == null) {
            throw new IllegalArgumentException("Grupo no encontrado con ID: " + grupoId);
        }

        // Eliminar el grupo de la lista y de la base de datos
        boolean eliminado = this.gruposEstudio.remove(grupo);
        if (eliminado) {
            utilRed.eliminarGrupoPorId(grupoId);
            LOGGER.log(Level.INFO, "Grupo {0} eliminado correctamente", grupoId);
            return true;
        } else {
            LOGGER.log(Level.WARNING, "No se pudo eliminar el grupo {0}", grupoId);
            return false;
        }
    }

}