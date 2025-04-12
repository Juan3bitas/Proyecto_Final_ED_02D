package main.java.proyectofinal.clases;

import java.time.LocalDateTime;
import java.util.LinkedList;

public class Contenido {
    private String titulo;
    private String autor;
    private LocalDateTime fecha;
    private String tipo;
    private String tema;
    private LinkedList<String> valoraciones;

    public Contenido(String titulo, String autor, LocalDateTime fecha, String tipo, String tema) {
        this.titulo = titulo;
        this.autor = autor;
        this.fecha = fecha;
        this.tipo = tipo;
        this.tema = tema;
        this.valoraciones = new LinkedList<>();
    }

    //getters and setters
    public String getTitulo() {
        return this.titulo;
    }

    public void setTitulo(final String titulo) {
        this.titulo = titulo;
    }

    public String getAutor() {
        return this.autor;
    }

    public void setAutor(final String autor) {
        this.autor = autor;
    }

    public LocalDateTime getFecha() {
        return this.fecha;
    }

    public void setFecha(final LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public String getTipo() {
        return this.tipo;
    }

    public void setTipo(final String tipo) {
        this.tipo = tipo;
    }

    public String getTema() {
        return this.tema;
    }

    public void setTema(final String tema) {
        this.tema = tema;
    }

    public LinkedList<String> getValoraciones() {
        return this.valoraciones;
    }

    public void setValoraciones(final LinkedList<String> valoraciones) {
        this.valoraciones = valoraciones;
    }
}