package main.java.proyectofinal.modelo;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import main.java.proyectofinal.excepciones.OperacionFallidaException;
import main.java.proyectofinal.utils.UtilGrupoEstudio;
import main.java.proyectofinal.utils.UtilId;

/**
 * Representa un grupo de estudio con miembros y contenidos compartidos.
 * Delega la persistencia a UtilGrupoEstudiante.
 */
public class GrupoEstudio {
    public static final int MAX_MIEMBROS = 6;
    private String idGrupo;
    private String nombre;
    private String descripcion;
    private LinkedList<String> idMiembros;
    private LinkedList<String> idContenidos;
    private Date fechaCreacion;
    private transient UtilGrupoEstudio utilGrupoEstudio;
    private String interes;

    private static final Logger LOGGER = Logger.getLogger(GrupoEstudio.class.getName());

    public GrupoEstudio(String id, String nombre, String descripcion, LinkedList<String> idMiembros,
                        LinkedList<String> idContenidos, Date fechaCreacion) throws OperacionFallidaException {
        // Validaciones básicas
        this.idGrupo = (id == null || id.isEmpty()) ? UtilId.generarIdAleatorio() : id;
        this.nombre = Objects.requireNonNull(nombre, "El nombre no puede ser nulo");
        this.descripcion = descripcion;

        // Validación de miembros
        final int MIN_MIEMBROS = 2;
        final int MAX_MIEMBROS = 6;

        if (idMiembros == null || idMiembros.size() < MIN_MIEMBROS) {
            throw new OperacionFallidaException(
                    String.format("Se requieren al menos %d miembros para formar un grupo", MIN_MIEMBROS)
            );
        }

        if (idMiembros.size() > MAX_MIEMBROS) {
            throw new OperacionFallidaException(
                    String.format("Un grupo no puede tener más de %d miembros", MAX_MIEMBROS)
            );
        }

        this.idMiembros = new LinkedList<>(idMiembros);
        this.idContenidos = (idContenidos != null) ? new LinkedList<>(idContenidos) : new LinkedList<>();
        this.fechaCreacion = (fechaCreacion != null) ? fechaCreacion : new Date();
        this.utilGrupoEstudio = UtilGrupoEstudio.getInstance();

        LOGGER.log(Level.FINE, "Nuevo grupo creado: {0} (ID: {1})",
                new Object[]{nombre, idGrupo});
    }

    public GrupoEstudio() {
        this.idMiembros = new LinkedList<>();
        this.idContenidos = new LinkedList<>();
        this.fechaCreacion = new Date();
        this.utilGrupoEstudio = UtilGrupoEstudio.getInstance();
        this.idGrupo = UtilId.generarIdAleatorio();
    }

    // Getters y setters
    public String getIdGrupo() {
        return this.idGrupo;
    }

    public String getNombre() {
        return this.nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = Objects.requireNonNull(nombre, "El nombre no puede ser nulo");
    }

    public String getDescripcion() {
        return this.descripcion;
    }

    public void setDescripcion(final String descripcion) {
        this.descripcion = descripcion;
    }

    public LinkedList<String> getIdMiembros() {
        return new LinkedList<>(this.idMiembros); // Defensive copy
    }

    public void setMiembros(LinkedList<String> idMiembros) {
        this.idMiembros = new LinkedList<>(idMiembros); // Defensive copy
    }

    public Date getFechaCreacion() {
        return new Date(this.fechaCreacion.getTime()); // Defensive copy
    }

    public void setFechaCreacion(Date fechaCreacion) {
        this.fechaCreacion = new Date(fechaCreacion.getTime()); // Defensive copy
    }

    public LinkedList<String> getIdContenidos() {
        return new LinkedList<>(idContenidos); // Defensive copy
    }

    // Métodos de operación
    public void agregarMiembro(String idEstudiante) throws OperacionFallidaException {
        if (utilGrupoEstudio.agregarMiembro(this.getIdGrupo(), idEstudiante)) {
            idMiembros.add(idEstudiante);
            LOGGER.log(Level.INFO, "Miembro {0} agregado al grupo {1}",
                    new Object[]{idEstudiante, idGrupo});
        }
    }

    public void eliminarMiembro(String idEstudiante) throws OperacionFallidaException {
        if (utilGrupoEstudio.eliminarMiembro(this.getIdGrupo(), idEstudiante)) {
            idMiembros.remove(idEstudiante);
            LOGGER.log(Level.INFO, "Miembro {0} eliminado del grupo {1}",
                    new Object[]{idEstudiante, idGrupo});
        }
    }

    public void agregarContenido(String idContenido) throws OperacionFallidaException {
        if (utilGrupoEstudio.agregarContenido(this.getIdGrupo(), idContenido)) {
            idContenidos.add(idContenido);
            LOGGER.log(Level.FINE, "Contenido {0} agregado al grupo {1}",
                    new Object[]{idContenido, idGrupo});
        }
    }

    public String getId() {
        return this.idGrupo;
    }

    public Collection<Object> getContenidos() {
        try {
            return utilGrupoEstudio.getContenidos(this.idGrupo);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al obtener contenidos del grupo " + idGrupo, e);
            return Collections.emptyList();
        }
    }

    public Collection<Object> getMensajes() {
        try {
            return utilGrupoEstudio.getMensajes(this.idGrupo);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al obtener mensajes del grupo " + idGrupo, e);
            return Collections.emptyList();
        }
    }

    /**
     * Obtiene todos los mensajes asociados a un grupo de estudio
     * @param idGrupo ID del grupo
     * @return Colección de mensajes (puede estar vacía pero nunca null)
     */
    public Collection<Object> obtenerMensajesPorGrupo(String idGrupo) {
        try {
            // Validación básica
            if (idGrupo == null || idGrupo.trim().isEmpty()) {
                LOGGER.warning("ID de grupo inválido al obtener mensajes");
                return Collections.emptyList();
            }

            // Verificar que el grupo existe
            if (!idGrupo.equals(this.idGrupo)) {
                LOGGER.warning("Intento de obtener mensajes de grupo no coincidente");
                return Collections.emptyList();
            }

            // Delegar a UtilGrupoEstudio
            Collection<Object> mensajes = utilGrupoEstudio.getMensajes(idGrupo);

            LOGGER.log(Level.FINE, "Obtenidos {0} mensajes para el grupo {1}",
                    new Object[]{mensajes.size(), idGrupo});

            return mensajes;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error al obtener mensajes para el grupo " + idGrupo, e);
            return Collections.emptyList();
        }
    }

    @Override
    public String toString() {
        return String.format("GrupoEstudio[id=%s, nombre=%s, miembros=%d, contenidos=%d]",
                idGrupo, nombre, idMiembros.size(), idContenidos.size());
    }

    public void setParticipantes(List<Estudiante> estudiantes) {
        if (estudiantes == null || estudiantes.isEmpty()) {
            throw new IllegalArgumentException("La lista de estudiantes no puede ser nula o vacía");
        }

        // Crear nueva lista en lugar de clear() para evitar problemas
        LinkedList<String> nuevosMiembros = new LinkedList<>();

        for (Estudiante estudiante : estudiantes) {
            if (estudiante != null && estudiante.getId() != null) {
                nuevosMiembros.add(estudiante.getId());
            } else {
                throw new IllegalArgumentException("Estudiante inválido en la lista");
            }
        }

        this.idMiembros = nuevosMiembros;
        LOGGER.log(Level.INFO, "Participantes actualizados para el grupo {0}: {1}",
                new Object[]{idGrupo, idMiembros});
    }

    // Unificar métodos de configuración de miembros
    public void setMiembros(List<String> idMiembros) {
        if (idMiembros == null || idMiembros.isEmpty()) {
            throw new IllegalArgumentException("La lista de IDs de miembros no puede ser nula o vacía");
        }
        this.idMiembros = new LinkedList<>(idMiembros);
    }

    public void setInteres(String interes) {
        if (interes == null || interes.trim().isEmpty()) {
            throw new IllegalArgumentException("El interés no puede ser nulo o vacío");
        }

        // Aquí podrías agregar lógica para manejar el interés, por ejemplo, guardarlo en la base de datos
        LOGGER.log(Level.INFO, "Interés del grupo {0} actualizado a: {1}",
                new Object[]{idGrupo, interes});
    }

    public String getInteres() {
        return interes;
    }


}