package main.java.proyectofinal.excepciones;
/*
 * Excepción lanzada cuando una solicitud no se encuentra.
 * Puede ser causada por problemas de persistencia o lógica de negocio.
 */
public class SolicitudNoEncontradaException extends Exception {
    public SolicitudNoEncontradaException(String mensaje) {
        super(mensaje);
    }
}
