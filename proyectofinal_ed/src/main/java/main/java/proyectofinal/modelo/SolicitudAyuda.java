package main.java.proyectofinal.modelo;

import java.util.Date;

import main.java.proyectofinal.utils.UtilId;

public class SolicitudAyuda {
    private String id;
    private String tema;
    private String descripcion;
    private Date fecha;
    private Urgencia urgencia;
    private String solicitanteId ;
    private Estado estado;

    public SolicitudAyuda(){
    }

    public SolicitudAyuda (String id, String tema, String descripcion, Date fecha, Urgencia urgencia, String solicitanteId){
        this.id = (id == null || id.isEmpty()) ? UtilId.generarIdAleatorio() : id;
        this.tema=tema;
        this.descripcion=descripcion;
        this.fecha = fecha == null  ? new Date() : fecha;
        this.urgencia=urgencia;
        this.solicitanteId=solicitanteId;
    }

    public void cambiarEstado(){
        if(this.estado==Estado.PENDIENTE){
            setEstado(Estado.RESUELTA);
        } else {
            setEstado(Estado.PENDIENTE);
        }
    }

    //Getters y setters

    public String getId() {
        return id;
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

    public Date getFecha() {
        return fecha;
    }

    public Urgencia getUrgencia() {
        return urgencia;
    }

    public void setUrgencia(Urgencia urgencia) {
        this.urgencia = urgencia;
    }

    public String getSolicitanteId() {
        return solicitanteId;
    }

    public Estado getEstado() {
        return estado;
    }

    public void setEstado(Estado estado) {
        this.estado = estado;
    }
    
}
