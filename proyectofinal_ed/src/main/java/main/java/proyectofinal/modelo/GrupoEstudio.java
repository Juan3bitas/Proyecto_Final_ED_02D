package main.java.proyectofinal.modelo;

import java.util.Date;
import java.util.LinkedList;

import main.java.proyectofinal.utils.UtilGrupoEstudiante;
import main.java.proyectofinal.utils.UtilId;

public class GrupoEstudio {
    private String idGrupo;
    private String nombre;
    private String descripcion;
    private LinkedList<String> idMiembros;
    private LinkedList<String> idContenidos;
    private Date fechaCreacion;
    private UtilGrupoEstudiante utilGrupoEstudiante;

    public GrupoEstudio(String id, String nombre, String descripcion,LinkedList<String> idMiembros,LinkedList<String> idContenidos, Date fechaCreacion) {
        this.idGrupo = (id == null || id.isEmpty()) ? UtilId.generarIdAleatorio() : id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.idMiembros = (idMiembros != null) ? idMiembros : new LinkedList<>();
        this.idContenidos = (idContenidos != null) ? idContenidos : new LinkedList<>();
        this.fechaCreacion = fechaCreacion == null  ? new Date() : fechaCreacion;
    }

    //getters and setters
    public String getIdGrupo(){
        return this.idGrupo;
    }
    public String getNombre() {
        return this.nombre;
    }

    public void setNombre(final String nombre) {
        this.nombre = nombre;
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

    public void setIdMiembros(LinkedList<String> idMiembros) {
        this.idMiembros = idMiembros;
    }

    public LinkedList<String> getIdContenidos() {
        return idContenidos;
    }

    public void setIdContenidos(LinkedList<String> idContenidos) {
        this.idContenidos = idContenidos;
    }

    public void agregarMiembro(String idEstudiante){
        if(utilGrupoEstudiante.agregarMiembro(idEstudiante)){
            idMiembros.add(idEstudiante);
        }
    }

    public void eliminarMiembro(String idEstudiante){
        if(utilGrupoEstudiante.eliminarMiembro(idEstudiante)){
            idMiembros.remove(idEstudiante);
        }
    }

    public void agregarContenido(String idContenido){
        if(utilGrupoEstudiante.agregarContenido(idContenido)){
            idMiembros.add(idContenido);
        }
    }
}