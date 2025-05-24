package main.java.proyectofinal.modelo;

import main.java.proyectofinal.excepciones.OperacionFallidaException;
import main.java.proyectofinal.utils.UtilProperties;
import main.java.proyectofinal.utils.UtilRedSocial;

import java.time.LocalDateTime;
import java.util.*;

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

    // Constructor
    public RedSocial(UtilRedSocial utilRed) {
        verificarArchivosPersistencia();
        this.utilRed = Objects.requireNonNull(utilRed, "UtilRedSocial no puede ser nulo");
        this.usuarios = utilRed.obtenerUsuarios() != null ? utilRed.obtenerUsuarios() : new ArrayList<>();
        this.arbolContenidos = new ArbolContenidos(CriterioOrden.TEMA);
        this.grafoAfinidad = new GrafoAfinidad();
        this.colaSolicitudes = new PriorityQueue<>(Comparator.comparingInt(s -> s.getUrgencia().ordinal()));
        this.colaSolicitudes.addAll(utilRed.obtenerSolicitudes());
        //this.arbolContenidos.inicializarConLista(utilRed.obtenerContenidos());
        cargarContenidosAlArbol();
        cargarRelacionesAfinidad();
    }

    private void verificarArchivosPersistencia() {
        UtilProperties utilProperties = UtilProperties.getInstance();
        String[] archivos = {
                utilProperties.obtenerPropiedad("rutaUsuarios.txt"),
                utilProperties.obtenerPropiedad("rutaContenidos.txt"),
                utilProperties.obtenerPropiedad("rutaSolicitudes.txt"),
                utilProperties.obtenerPropiedad("rutaGruposEstudio.txt"), // Corregido el nombre
                utilProperties.obtenerPropiedad("rutaReportes.txt")
        };

    }

    // Métodos de inicialización
    private void cargarRelacionesAfinidad() {
        usuarios.stream()
                .filter(u -> u instanceof Estudiante)
                .map(u -> (Estudiante) u)
                .forEach(grafoAfinidad::agregarNodo);

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

                    int pesoActual = grafoAfinidad.obtenerPesoArista(e1, e2);
                    grafoAfinidad.agregarArista(e1, e2, pesoActual + 1);
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
            grafoAfinidad.agregarNodo((Estudiante) usuario);
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

    // Métodos de gestión de contenidos
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
        int pesoActual = grafoAfinidad.obtenerPesoArista(e1, e2);
        grafoAfinidad.agregarArista(e1, e2, pesoActual + incremento);
    }

    public List<Estudiante> obtenerRecomendacionesAmplias(String idEstudiante) {
        Usuario usuario = buscarUsuario(idEstudiante);
        if (!(usuario instanceof Estudiante)) {
            throw new IllegalArgumentException("El usuario no es un estudiante");
        }
        return grafoAfinidad.obtenerRecomendaciones((Estudiante) usuario);
    }

    public List<Estudiante> generarRecomendaciones(String idUsuario) {
        Usuario usuario = buscarUsuario(idUsuario);
        if (!(usuario instanceof Estudiante)) {
            throw new IllegalArgumentException("El usuario no es un estudiante");
        }
        return grafoAfinidad.obtenerRecomendaciones((Estudiante) usuario);
    }

    // Métodos de grupos de estudio
    public List<GrupoEstudio> formarGruposAutomaticos() {
        List<GrupoEstudio> grupos = utilRed.formarGruposAutomaticos(this.usuarios);
        utilRed.guardarGrupos(grupos);
        actualizarAfinidadPorGrupos(grupos);
        return grupos;
    }

    private void actualizarAfinidadPorGrupos(List<GrupoEstudio> grupos) {
        for (GrupoEstudio grupo : grupos) {
            List<String> idsMiembros = grupo.getIdMiembros();
            for (int i = 0; i < idsMiembros.size(); i++) {
                for (int j = i + 1; j < idsMiembros.size(); j++) {
                    actualizarRelacionAfinidad(idsMiembros.get(i), idsMiembros.get(j));
                }
            }
        }
    }

    private void actualizarRelacionAfinidad(String id1, String id2) {
        try {
            Estudiante e1 = (Estudiante) buscarUsuario(id1);
            Estudiante e2 = (Estudiante) buscarUsuario(id2);

            if (e1 != null && e2 != null) {
                grafoAfinidad.agregarArista(
                        e1,
                        e2,
                        grafoAfinidad.obtenerPesoArista(e1, e2) + 1
                );
                actualizarInteresesCompartidos(e1, e2);
            }
        } catch (Exception e) {
            System.err.println("Error actualizando afinidad: " + e.getMessage());
        }
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

    public List<String> obtenerGruposEstudio(String userId) {
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
        // 1. Buscar el contenido en el árbol
        Contenido contenido = arbolContenidos.buscarPorId(contenidoId);

        if (contenido == null) {
            throw new IllegalArgumentException("Contenido no encontrado");
        }

        // 2. Cargar las valoraciones asociadas desde la base de datos
        List<Valoracion> valoraciones = cargarValoracionesParaContenido(contenidoId);
        contenido.setValoraciones(new LinkedList<>(valoraciones));

        System.out.println("[DEBUG] Contenido encontrado - ID: " + contenidoId +
                " | Valoraciones: " + valoraciones.size());

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
}