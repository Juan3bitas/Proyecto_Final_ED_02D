package main.java.proyectofinal.modelo;

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
    private final UtilRedSocial utilRed;
    private List<Usuario> usuarios;
    private ArbolContenidos arbolContenidos;
    private GrafoAfinidad grafoAfinidad;
    private PriorityQueue<SolicitudAyuda> colaSolicitudes;

    public RedSocial(UtilRedSocial utilRed) {
        this.utilRed = Objects.requireNonNull(utilRed, "UtilRedSocial no puede ser nulo");
        this.usuarios = utilRed.obtenerUsuarios() != null ? utilRed.obtenerUsuarios() : new ArrayList<>();
        this.arbolContenidos = new ArbolContenidos(CriterioOrden.TEMA); // Ordenado por tema
        this.grafoAfinidad = new GrafoAfinidad();
        this.colaSolicitudes = new PriorityQueue<>(Comparator.comparingInt(s -> s.getUrgencia().ordinal()));
    }

    public void registrarUsuario(Usuario usuario) {
        validarUsuario(usuario);
        utilRed.registrarUsuario(usuario);
        usuarios.add(usuario);
        grafoAfinidad.agregarNodo((Estudiante) usuario); // Solo si es Estudiante
    }

    public void eliminarUsuario(String idUsuario) {
        Usuario usuario = buscarUsuario(idUsuario);
        utilRed.eliminarUsuario(idUsuario);
        usuarios.remove(usuario);
        grafoAfinidad.removerNodo((Estudiante) usuario); // Solo si es Estudiante
    }

    public void modificarUsuario(Usuario usuario) {
        validarUsuario(usuario);
        utilRed.modificarUsuario(usuario);
    }

    public List<Usuario> obtenerUsuarios() {
        return new ArrayList<>(usuarios); // Retorna copia para evitar modificaciones externas
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
        Estudiante estudiante = (Estudiante) usuario;
        return grafoAfinidad.obtenerRecomendaciones(estudiante);
    }

    public void formarGruposAutomaticos() {
        List<GrupoEstudio> grupos = utilRed.formarGruposAutomaticos(usuarios);
        actualizarGrafoAfinidad(grupos);
    }

    private Usuario buscarUsuario(String idUsuario) {
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

    protected GrafoAfinidad getGrafoAfinidad() {
        return this.grafoAfinidad;
    }

    public List<Contenido> getContenidosOrdenados() {
        return Collections.unmodifiableList(this.arbolContenidos.obtenerTodosEnOrden());
    }

}