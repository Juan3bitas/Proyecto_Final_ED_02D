package main.java.proyectofinal.utils;

import main.java.proyectofinal.excepciones.SolicitudNoEncontradaException;

import java.util.Objects;

import main.java.proyectofinal.excepciones.EstadoNoValidoException;
import main.java.proyectofinal.modelo.SolicitudAyuda;
import main.java.proyectofinal.modelo.Estado;

public class UtilSolicitudAyuda {
    private static UtilSolicitudAyuda instancia;
    private final UtilRedSocial utilRedSocial;
    private final UtilLog utilLog;

    private UtilSolicitudAyuda() {
        this.utilRedSocial = UtilRedSocial.getInstance();
        this.utilLog = UtilLog.getInstance();
    }

    public static synchronized UtilSolicitudAyuda getInstance() {
        if (instancia == null) {
            instancia = new UtilSolicitudAyuda();
        }
        return instancia;
    }

    /**
     * Cambia el estado de una solicitud de ayuda
     * @param id ID de la solicitud
     * @param nuevoEstado Nuevo estado a asignar (PENDIENTE, EN_PROCESO, RESUELTA)
     * @throws SolicitudNoEncontradaException Si la solicitud no existe
     * @throws EstadoNoValidoException Si el estado no es válido
     */
    public void cambiarEstadoSolicitud(String id, Estado nuevoEstado) 
            throws SolicitudNoEncontradaException, EstadoNoValidoException {
        
        Objects.requireNonNull(id, "El ID no puede ser nulo");
        Objects.requireNonNull(nuevoEstado, "El estado no puede ser nulo");

        try {
            // 1. Buscar la solicitud en la red social
            SolicitudAyuda solicitud = utilRedSocial.buscarSolicitud(id);
            if (solicitud == null) {
                throw new SolicitudNoEncontradaException("Solicitud con ID " + id + " no encontrada");
            }

            // 2. Validar transición de estado
            if (!validarTransicionEstado(solicitud.getEstado(), nuevoEstado)) {
                throw new EstadoNoValidoException(
                    "No se puede cambiar de " + solicitud.getEstado() + " a " + nuevoEstado
                );
            }

            // 3. Actualizar y persistir
            solicitud.setEstado(nuevoEstado);
            utilRedSocial.actualizarSolicitud(solicitud);
            
            utilLog.logInfo("Solicitud " + id + " cambiada a estado: " + nuevoEstado);

        } catch (Exception e) {
            utilLog.logSevere("Error al cambiar estado de solicitud: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Valida las transiciones de estado permitidas:
     * PENDIENTE -> EN_PROCESO | RESUELTA
     * EN_PROCESO -> RESUELTA
     */
    private boolean validarTransicionEstado(Estado actual, Estado nuevo) {
        if (actual == nuevo) return true;
        
        return (actual == Estado.PENDIENTE && (nuevo == Estado.EN_PROCESO || nuevo == Estado.RESUELTA)) ||
               (actual == Estado.EN_PROCESO && nuevo == Estado.RESUELTA);
    }

    /**
     * Atiende una solicitud (cambia de PENDIENTE a EN_PROCESO)
     * @throws EstadoNoValidoException 
     */
    public void atenderSolicitud(String id) throws SolicitudNoEncontradaException, EstadoNoValidoException {
        cambiarEstadoSolicitud(id, Estado.EN_PROCESO);
    }

    /**
     * Marca una solicitud como resuelta
     * @throws EstadoNoValidoException 
     */
    public void resolverSolicitud(String id) throws SolicitudNoEncontradaException, EstadoNoValidoException {
        cambiarEstadoSolicitud(id, Estado.RESUELTA);
    }
}