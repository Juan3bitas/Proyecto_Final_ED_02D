package main.java.proyectofinal.modelo;

import java.util.Date;
import java.util.Objects;
import main.java.proyectofinal.utils.UtilId;

public class Reporte {

    private String idReporte;
    private TipoReporte tipo;
    private String contenido;
    private Date fechaGeneracion;
    
    // Constructor vacío (para frameworks que lo necesiten)
    public Reporte() {
        this.idReporte = UtilId.generarIdAleatorio();
        this.fechaGeneracion = new Date();
    }

    // Constructor principal
    public Reporte(String idReporte, TipoReporte tipo, String contenido, Date fechaGeneracion) {
        this.idReporte = (idReporte == null || idReporte.isEmpty()) ? 
                         UtilId.generarIdAleatorio() : idReporte;
        this.tipo = Objects.requireNonNull(tipo, "El tipo de reporte no puede ser nulo");
        this.contenido = Objects.requireNonNull(contenido, "El contenido no puede ser nulo");
        this.fechaGeneracion = (fechaGeneracion == null) ? new Date() : fechaGeneracion;
    }

    // ---- Getters y Setters ----
    public String getIdReporte() {
        return idReporte;
    }

    public TipoReporte getTipo() {
        return tipo;
    }

    public void setTipo(TipoReporte tipo) {
        this.tipo = Objects.requireNonNull(tipo);
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
            "Reporte [ID: %s, Tipo: %s, Fecha: %s, Contenido: %s]",
            idReporte, tipo.name(), fechaGeneracion, contenido
        );
    }
}