package main.java.proyectofinal.modelo;

/**
 * Representa un grupo de estudio con miembros y contenidos compartidos.
 * Delega la persistencia a UtilGrupoEstudiante.
 */

import java.util.Date;
import java.util.LinkedList;
import java.util.Objects;

import main.java.proyectofinal.excepciones.OperacionFallidaException;
import main.java.proyectofinal.utils.UtilGrupoEstudio;
import main.java.proyectofinal.utils.UtilId;

public class GrupoEstudio {
    private String idGrupo;
    private String nombre;
    private String descripcion;
    private LinkedList<String> idMiembros;
    private LinkedList<String> idContenidos;
    private Date fechaCreacion;
    private transient UtilGrupoEstudio utilGrupoEstudio;

    public GrupoEstudio(String id, String nombre, String descripcion, LinkedList<String> idMiembros, 
                LinkedList<String> idContenidos, Date fechaCreacion) {
        this.idGrupo = (id == null || id.isEmpty()) ? UtilId.generarIdAleatorio() : id;
        this.nombre = Objects.requireNonNull(nombre, "El nombre no puede ser nulo");
        this.descripcion = descripcion; // Opcional: validar si es requerido
        this.idMiembros = (idMiembros != null) ? idMiembros : new LinkedList<>();
        this.idContenidos = (idContenidos != null) ? idContenidos : new LinkedList<>();
        this.fechaCreacion = (fechaCreacion == null) ? new Date() : fechaCreacion;
        this.utilGrupoEstudio = UtilGrupoEstudio.getInstance(); // Singleton
    }

    //getters and setters
    public String getIdGrupo(){
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
        return this.idMiembros;
    }

    public void setMiembros(LinkedList<String> idMiembros) {
        this.idMiembros = idMiembros;
    }

    public Date getFechaCreacion() {
        return this.fechaCreacion;
    }

    public void setFechaCreacion(Date fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LinkedList<String> getIdContenidos() {
        return idContenidos;
    }

    public void agregarMiembro(String idEstudiante) throws OperacionFallidaException{
        if(utilGrupoEstudio.agregarMiembro(this.getIdGrupo(), idEstudiante)){
            idMiembros.add(idEstudiante);
        }
    }

    public void eliminarMiembro(String idEstudiante) throws OperacionFallidaException{
        if(utilGrupoEstudio.eliminarMiembro(this.getIdGrupo(), idEstudiante)){
            idMiembros.remove(idEstudiante);
        }
    }

    public void agregarContenido(String idContenido) throws OperacionFallidaException {
        if (utilGrupoEstudio.agregarContenido(this.getIdGrupo(), idContenido)) {
            idContenidos.add(idContenido); // Corregido: añade a idContenidos
        }
    }
}