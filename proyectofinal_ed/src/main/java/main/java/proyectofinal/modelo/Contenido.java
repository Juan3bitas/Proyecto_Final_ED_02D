package main.java.proyectofinal.modelo;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.Objects;

import main.java.proyectofinal.utils.UtilId;

/**
 * Representa un contenido educativo (archivo, enlace, video) publicado por un usuario.
 * Incluye valoraciones y metadatos como tema y autor.
 */

public class Contenido {
    
    private String id;
    private String titulo;
    private String autor;
    private LocalDateTime fecha;
    private TipoContenido tipo;
    private String tema;
    private String descripcion;
    private String contenido;
    private LinkedList<Valoracion> valoraciones;

    /**
     * Constructor completo para Contenido educativo
     * @param id ID único (si es null o vacío, se genera uno)
     * @param titulo Título del contenido (no null)
     * @param autor Nombre del autor (no null)
     * @param fecha Fecha de publicación (no null)
     * @param tipo Tipo de contenido (no null)
     * @param descripcion Descripción detallada (no null)
     * @param tema Área de conocimiento (no null)
     * @param contenido Ruta/URL del contenido (no null)
     * @param valoraciones Lista de valoraciones (si es null, se crea una lista vacía)
     */
    public Contenido(String id, String titulo, String autor, LocalDateTime fecha,
                     TipoContenido tipo, String descripcion, String tema,
                     String contenido, LinkedList<Valoracion> valoraciones) {
        this.id = (id == null || id.isEmpty()) ? UtilId.generarIdAleatorio() : id;
        this.titulo = Objects.requireNonNull(titulo, "El título no puede ser nulo");
        this.autor = Objects.requireNonNull(autor, "El autor no puede ser nulo");
        this.fecha = Objects.requireNonNull(fecha, "La fecha no puede ser nula");
        this.tipo = Objects.requireNonNull(tipo, "El tipo no puede ser nulo");
        this.descripcion = Objects.requireNonNull(descripcion, "La descripción no puede ser nula");
        this.tema = Objects.requireNonNull(tema, "El tema no puede ser nulo");
        this.contenido = Objects.requireNonNull(contenido, "El contenido no puede ser nulo");
        this.valoraciones = (valoraciones != null) ? valoraciones : new LinkedList<>();
    }

    //getters and setters
    public String getId(){
        return this.id;
    }
    
    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getTitulo() {
        return this.titulo;
    }

    public void setTitulo(final String titulo) {
        this.titulo = titulo;
    }

    public String getAutor() {
        return this.autor;
    }

    public void setAutor(String autor) {
        if (autor == null || autor.trim().isEmpty()) {
            throw new IllegalArgumentException("El autor no puede estar vacío");
        }
        this.autor = autor;
    }

    public LocalDateTime getFecha() {
        return this.fecha;
    }

    public void setFecha(final LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public TipoContenido getTipo() {
        return this.tipo;
    }

    public void setTipo(final TipoContenido tipo) {
        this.tipo = tipo;
    }

    public String getTema() {
        return this.tema;
    }

    public void setTema(String tema) {
        if (tema == null || tema.trim().isEmpty()) {
            throw new IllegalArgumentException("El tema no puede estar vacío");
        }
        this.tema = tema;
    }

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = Objects.requireNonNull(contenido, "El contenido no puede ser nulo");
    }

    public LinkedList<Valoracion> getValoraciones() {
        return this.valoraciones;
    }

    public void setValoraciones(LinkedList<Valoracion> valoraciones) {
        this.valoraciones = valoraciones;
    }

    public double obtenerPromedioValoracion() {
        if (valoraciones.isEmpty()) return 0.0;
        double suma = 0;
        for (Valoracion v : valoraciones) {
            suma += v.getValor(); 
        }
        return suma / valoraciones.size();
    }

    public void agregarValoracion(Valoracion valoracion) {
        if (valoracion == null) {
            throw new IllegalArgumentException("La valoración no puede ser nula");
        }
        valoraciones.add(valoracion);
    }

    @Override
    public String toString() {
        return "Contenido{" +
                "id='" + id + '\'' +
                ", titulo='" + titulo + '\'' +
                ", autor='" + autor + '\'' +
                ", fecha=" + fecha +
                ", tipo=" + tipo +
                ", tema='" + tema + '\'' +
                ", descripcion='" + descripcion + '\'' +
                ", contenido='" + contenido + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Contenido contenido = (Contenido) o;
        return id.equals(contenido.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}