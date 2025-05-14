package main.java.proyectofinal.modelo;

/*
 * Representa una solicitud de ayuda académica con un nivel de urgencia y estado (PENDIENTE/RESUELTA).
 * Delega la persistencia a UtilSolicitudAyuda.
 */

import java.util.Date;
import java.util.Objects;

import main.java.proyectofinal.excepciones.EstadoNoValidoException;
import main.java.proyectofinal.excepciones.SolicitudNoEncontradaException;
import main.java.proyectofinal.utils.UtilId;
import main.java.proyectofinal.utils.UtilSolicitudAyuda;

public class SolicitudAyuda {
    private String id;
    private String tema;
    private String descripcion;
    private Date fecha;
    private Urgencia urgencia;
    private String solicitanteId ;
    private Estado estado;
    private transient UtilSolicitudAyuda utilSolicitud;

    public SolicitudAyuda(){
    }

    public SolicitudAyuda(String id, String tema, String descripcion, Date fecha, Urgencia urgencia, String solicitanteId) {
        this.id = (id == null || id.isEmpty()) ? UtilId.generarIdAleatorio() : id;
        this.tema = Objects.requireNonNull(tema, "El tema no puede ser nulo");
        this.descripcion = Objects.requireNonNull(descripcion, "La descripción no puede ser nula");
        this.fecha = (fecha == null) ? new Date() : fecha;
        this.urgencia = Objects.requireNonNull(urgencia, "La urgencia no puede ser nula");
        this.solicitanteId = Objects.requireNonNull(solicitanteId, "El ID del solicitante no puede ser nulo");
        this.estado = Estado.PENDIENTE; 
        this.utilSolicitud = UtilSolicitudAyuda.getInstance(); 
    }

    //Getters y setters

    public String getId() {
        return id;
    }

    public String getTema() {
        return tema;
    }

    public void setTema(String tema) {
        this.tema = Objects.requireNonNull(tema, "El tema no puede ser nulo");
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = Objects.requireNonNull(descripcion, "La descripción no puede ser nula");
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

    public void cambiarEstado(Estado nuevoEstado) throws SolicitudNoEncontradaException, EstadoNoValidoException {
        this.setEstado(nuevoEstado);
        utilSolicitud.cambiarEstadoSolicitud(this.id, nuevoEstado);
    }

    public void setSolicitanteId(String solicitanteId) {
        this.solicitanteId = solicitanteId;
    }


    
}
