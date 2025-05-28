package main.java.proyectofinal.excepciones;
/*
 * Excepción lanzada cuando una solicitud no se encuentra.
 * Puede ser causada por problemas de persistencia o lógica de negocio.
 */
public class EstadoNoValidoException extends Exception {
    public EstadoNoValidoException(String mensaje) {
        super(mensaje);
    }
}