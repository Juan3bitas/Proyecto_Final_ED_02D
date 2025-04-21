package main.java.proyectofinal.modelo;

import java.util.Date;

import main.java.proyectofinal.utils.UtilId;

public class Reporte {
    private String idReporte;
    private String tipo;
    private String contenido;
    private Date fechaGeneracion;
    
    public Reporte(){
    }

    public Reporte(String idReporte, String tipo, String contenido, Date fechaCreacion){
        this.idReporte = (idReporte == null || idReporte.isEmpty()) ? UtilId.generarIdAleatorio() : idReporte;
        this.tipo=tipo;
        this.contenido=contenido;
        this.fechaGeneracion = fechaGeneracion == null  ? new Date() : fechaGeneracion;
    }

    @Override
    public String toString() {
        return "Reporte [idReporte=" + idReporte + ", tipo=" + tipo + ", contenido=" + contenido + ", fechaGeneracion="
                + fechaGeneracion + "]";
    }
    
}
