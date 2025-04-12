package main.java.proyectofinal.clases;

import java.time.LocalDateTime;
import java.util.LinkedList;

public class GrupoEstudio {
    private String nombre;
    private String descripcion;
    private LinkedList<Estudiante> miembros;
    private LocalDateTime fechaCreacion;

    public GrupoEstudio(String nombre, String descripcion, LocalDateTime fechaCreacion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.fechaCreacion = fechaCreacion;
        this.miembros = new LinkedList<>();
    }

    //getters and setters
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

    public LinkedList<Estudiante> getMiembros() {
        return this.miembros;
    }

    public void setMiembros(final LinkedList<Estudiante> miembros) {
        this.miembros = miembros;
    }

    public LocalDateTime getFechaCreacion() {
        return this.fechaCreacion;
    }

    public void setFechaCreacion(final LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
}