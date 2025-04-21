package main.java.proyectofinal.modelo;

import java.util.Date;

import main.java.proyectofinal.utils.UtilId;

public class Valoracion {
    String idValoracion;
    String tema;
    String descripcion;
    String idAutor;
    Date fecha;
    String comentario;

    public Valoracion(String id, String tema, String descripcion, String idAutor, Date fecha, String comentario) {
        this.idValoracion = (id == null || id.isEmpty()) ? UtilId.generarIdAleatorio() : id;
        this.tema = tema;
        this.descripcion = descripcion;
        this.idAutor = idAutor;
        this.fecha = fecha;
        this.comentario = comentario;
    }

    @Override
    public String toString() {
        return "Valoracion [idValoracion=" + idValoracion + ", tema=" + tema + ", descripcion=" + descripcion
                + ", idAutor=" + idAutor + ", fecha=" + fecha + ", comentario=" + comentario + "]";
    }

    
    
}
