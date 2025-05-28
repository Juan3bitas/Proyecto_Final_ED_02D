package main.java.proyectofinal.modelo;

import java.util.Date;
import java.util.Objects;
import main.java.proyectofinal.utils.UtilId;

public class Reporte {

    private String idReporte;
    private String contenido;
    private Date fechaGeneracion;

    public Reporte() {
        this.idReporte = UtilId.generarIdAleatorio();
        this.fechaGeneracion = new Date();
    }


    public Reporte(String idReporte, String contenido, Date fechaGeneracion) {
        this.idReporte = (idReporte == null || idReporte.isEmpty()) ? 
                         UtilId.generarIdAleatorio() : idReporte;
        this.contenido = Objects.requireNonNull(contenido, "El contenido no puede ser nulo");
        this.fechaGeneracion = (fechaGeneracion == null) ? new Date() : fechaGeneracion;
    }

    public String getIdReporte() {
        return idReporte;
    }



    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = Objects.requireNonNull(contenido);
    }

    public Date getFechaGeneracion() {
        return fechaGeneracion;
    }

    public void setFechaGeneracion(Date fechaGeneracion) {
        this.fechaGeneracion = (fechaGeneracion == null) ? new Date() : fechaGeneracion;
    }

    @Override
    public String toString() {
        return String.format(
            "Reporte [ID: %s, Fecha: %s, Contenido: %s]",
            idReporte, fechaGeneracion, contenido
        );
    }
}