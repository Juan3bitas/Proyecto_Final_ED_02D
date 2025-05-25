package main.java.proyectofinal.modelo;

/*
 * Representa una valoración (puntuación y comentario) hecha por un usuario sobre un contenido educativo.
 * El valor debe estar entre 1 y 5.
 */

import java.util.Date;
import java.util.Objects;

import main.java.proyectofinal.utils.UtilId;

public class Valoracion {
    String idValoracion;
    String tema;
    String descripcion;
    String idAutor;
    Integer valor;
    Date fecha;
    String comentario;

    public Valoracion(String id, String tema, String descripcion, String idAutor, Integer valor, Date fecha, String comentario) {
        this.idValoracion = (id == null || id.isEmpty()) ? UtilId.generarIdAleatorio() : id;
        this.tema = tema;
        this.descripcion = descripcion;
        this.idAutor = Objects.requireNonNull(idAutor, "El ID del autor no puede ser nulo");
        this.valor = Objects.requireNonNull(valor, "El valor no puede ser nulo");
        this.fecha = fecha;
        this.comentario = comentario;
    }

    public String getIdValoracion() {
        return idValoracion;
    }

    public String getTema() {
        return tema;
    }

    public void setTema(String tema) {
        this.tema = tema;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getIdAutor() {
        return idAutor;
    }

    public Integer getValor() {
        return valor;
    }

    public void setValor(Integer valor) {
        if (valor == null || valor < 1 || valor > 5) {
            throw new IllegalArgumentException("El valor debe estar entre 1 y 5");
        }
        this.valor = valor;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Valoracion that = (Valoracion) o;
        return idValoracion.equals(that.idValoracion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idValoracion);
    }

    @Override
    public String toString() {
        return String.format(
            "Valoracion [ID: %s, Tema: %s, Valor: %d/5, Autor: %s]", 
            idValoracion, tema, valor, idAutor
        );
    }

    public String getUsuarioId() {
        return idAutor;
    }

    public String getIdContenido() {
        return idValoracion;
    }

    public Double getPuntuacion() {
        return valor.doubleValue();
    }
}
