package main.java.proyectofinal.modelo;

import main.java.proyectofinal.excepciones.OperacionFallidaException;
import main.java.proyectofinal.utils.UtilRedSocial;
import java.util.List;
import java.util.Objects;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * Clase principal que coordina todas las operaciones de la red social
 * educativa.
 * Centraliza la gestión de usuarios, contenidos, grupos y solicitudes de ayuda.
 */
public class RedSocial {
    private final String nombre = "AprendeJuntos"; // Nombre definido
    private final transient UtilRedSocial utilRed;
    private List<Usuario> usuarios;
    private ArbolContenidos arbolContenidos;
    private GrafoAfinidad grafoAfinidad;
    private PriorityQueue<SolicitudAyuda> colaSolicitudes;

    public RedSocial(UtilRedSocial utilRed) {
        this.utilRed = Objects.requireNonNull(utilRed, "UtilRedSocial no puede ser nulo");
        this.usuarios = utilRed.obtenerUsuarios() != null ? utilRed.obtenerUsuarios() : new ArrayList<>();
        this.arbolContenidos = new ArbolContenidos(CriterioOrden.TEMA);
        this.grafoAfinidad = new GrafoAfinidad();
        this.colaSolicitudes = new PriorityQueue<>(Comparator.comparingInt(s -> s.getUrgencia().ordinal()));

        // Cargar datos iniciales
        cargarContenidosAlArbol();
        cargarRelacionesAfinidad();
    }

    private void cargarRelacionesAfinidad() {
        // 1. Agregar todos los estudiantes como nodos
        usuarios.stream()
                .filter(u -> u instanceof Estudiante)
                .map(u -> (Estudiante) u)
                .forEach(grafoAfinidad::agregarNodo);

        // 2. Cargar relaciones desde persistencia (si existe)
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

                    // Incrementar peso si ya existe la relación
                    int pesoActual = grafoAfinidad.obtenerPesoArista(e1, e2);
                    grafoAfinidad.agregarArista(e1, e2, pesoActual + 1);
                }
            }
        }
    }

    public void actualizarAfinidad(Estudiante e1, Estudiante e2, int incremento) {
        int pesoActual = grafoAfinidad.obtenerPesoArista(e1, e2);
        grafoAfinidad.agregarArista(e1, e2, pesoActual + incremento);
        // Opcional: guardar en persistencia
    }

    public List<Estudiante> obtenerRecomendacionesAmplias(String idEstudiante) {
        Usuario usuario = buscarUsuario(idEstudiante);
        if (!(usuario instanceof Estudiante)) {
            throw new IllegalArgumentException("El usuario no es un estudiante");
        }
        return grafoAfinidad.obtenerRecomendaciones((Estudiante) usuario);
    }

    private void cargarContenidosAlArbol() {
        List<Contenido> contenidos = utilRed.obtenerContenidos();
        if (contenidos != null) {
            for (Contenido contenido : contenidos) {
                arbolContenidos.insertar(contenido);
            }
        }
    }

    public void agregarContenido(Contenido contenido) {
        Objects.requireNonNull(contenido, "El contenido no puede ser nulo");
        utilRed.guardarContenido(contenido);
        arbolContenidos.insertar(contenido);
    }

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
        List<Contenido> todos = arbolContenidos.obtenerTodosEnOrden();
        List<Contenido> filtrados = new ArrayList<>();

        for (Contenido contenido : todos) {
            if (contenido.getTipo() == tipo) {
                filtrados.add(contenido);
            }
        }

        return filtrados;
    }

    public void cambiarCriterioOrden(CriterioOrden nuevoCriterio) {
        Objects.requireNonNull(nuevoCriterio, "El criterio no puede ser nulo");

        List<Contenido> contenidos = arbolContenidos.obtenerTodosEnOrden();
        this.arbolContenidos = new ArbolContenidos(nuevoCriterio);

        for (Contenido contenido : contenidos) {
            arbolContenidos.insertar(contenido);
        }
    }

    public void registrarUsuario(Usuario usuario) {
        validarUsuario(usuario);
        utilRed.registrarUsuario(usuario);
        usuarios.add(usuario);
        grafoAfinidad.agregarNodo((Estudiante) usuario); // Solo si es Estudiante
    }


    public void modificarUsuario(Usuario usuario) {
        validarUsuario(usuario);
        utilRed.modificarUsuario(usuario);
    }

    public List<Usuario> obtenerUsuarios() {
        return utilRed.obtenerUsuarios(); // Retorna copia para evitar modificaciones externas
    }

    public List<Contenido> obtenerContenidos() {
        return utilRed.obtenerContenidos(); // Retorna copia para evitar modificaciones externas
    }

    public void agregarSolicitud(SolicitudAyuda solicitud) {
        validarSolicitud(solicitud);
        colaSolicitudes.add(solicitud);
        utilRed.guardarSolicitud(solicitud);
    }

    public SolicitudAyuda atenderSolicitud() {
        SolicitudAyuda solicitud = colaSolicitudes.poll();
        if (solicitud != null) {
            utilRed.actualizarEstadoSolicitud(solicitud.getId(), Estado.RESUELTA);
        }
        return solicitud;
    }

    public Usuario iniciarSesion(String correo, String contrasenia) {
        return utilRed.iniciarSesion(correo, contrasenia);
    }

    public List<Estudiante> generarRecomendaciones(String idUsuario) {
        Usuario usuario = buscarUsuario(idUsuario);
        if (!(usuario instanceof Estudiante)) {
            throw new IllegalArgumentException("El usuario no es un estudiante");
        }
        return grafoAfinidad.obtenerRecomendaciones((Estudiante) usuario);
    }

    public List<GrupoEstudio> formarGruposAutomaticos() {
        // 1. Creación básica de grupos (delegado a UtilRedSocial)
        List<GrupoEstudio> grupos = utilRed.formarGruposAutomaticos(this.usuarios);

        // 2. Persistencia
        utilRed.guardarGrupos(grupos);

        // 3. Actualización de afinidades
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
                // Opcional: Actualizar en persistencia si es necesario
                actualizarInteresesCompartidos(e1, e2);
            }
        } catch (Exception e) {
            System.err.println("Error actualizando afinidad: " + e.getMessage());
        }
    }

    private void actualizarInteresesCompartidos(Estudiante e1, Estudiante e2) throws OperacionFallidaException {
        // Lógica adicional si necesitas registrar intereses compartidos
        String interesGrupo = "Colaboración en Grupo";
        e1.agregarInteres(interesGrupo);
        e2.agregarInteres(interesGrupo);
        utilRed.actualizarEstudiante(e1);
        utilRed.actualizarEstudiante(e2);
    }

    public Usuario buscarUsuario(String idUsuario) {
        return usuarios.stream()
                .filter(u -> u.getId().equals(idUsuario))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
    }

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

    private void actualizarGrafoAfinidad(List<GrupoEstudio> grupos) {
        grupos.forEach(grupo -> {
            List<String> miembros = grupo.getIdMiembros();
            for (int i = 0; i < miembros.size(); i++) {
                for (int j = i + 1; j < miembros.size(); j++) {
                    Estudiante estudiante1 = (Estudiante) buscarUsuario(miembros.get(i));
                    Estudiante estudiante2 = (Estudiante) buscarUsuario(miembros.get(j));
                    grafoAfinidad.agregarArista(estudiante1, estudiante2, 1); // Peso de afinidad básico
                }
            }
        });
    }

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

    public List<Contenido> getContenidosOrdenados() {
        return Collections.unmodifiableList(this.arbolContenidos.obtenerTodosEnOrden());
    }

    public List<Contenido> obtenerContenidosPorEstudiante(String userId) {
        List<Contenido> contenidos = new ArrayList<>();
        for (Contenido contenido : arbolContenidos.obtenerTodosEnOrden()) {
            if (contenido.getAutor().equals(userId)) {
                contenidos.add(contenido);
            }
        }
        return contenidos;
    }

    public List<SolicitudAyuda> obtenerSolicitudes() {
        List<SolicitudAyuda> solicitudes = new ArrayList<>();
        while (!colaSolicitudes.isEmpty()) {
            SolicitudAyuda solicitud = colaSolicitudes.poll();
            if (solicitud != null) {
                solicitudes.add(solicitud);
            }
        }
        return solicitudes;
    }

    public boolean actualizarUsuario(Usuario usuario) {
        try {
            // Verificar si el usuario es un Estudiante
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

            // 3. Eliminar el usuario
            return utilRed.eliminarUsuario(usuarioId);

        } catch (Exception e) {
            System.err.println("Error al eliminar usuario: " + e.getMessage());
            return false;
        }
    }

    private void eliminarContenidosUsuario(String usuarioId) {
        List<Contenido> contenidos = arbolContenidos.obtenerTodosEnOrden();
        List<Contenido> aEliminar = new ArrayList<>();

        for (Contenido contenido : contenidos) {
            if (contenido.getAutor().equals(usuarioId)) {
                aEliminar.add(contenido);
            }
        }

        for (Contenido contenido : aEliminar) {
            arbolContenidos.eliminar(contenido); // Este método debe estar implementado en tu ArbolContenidos
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


}