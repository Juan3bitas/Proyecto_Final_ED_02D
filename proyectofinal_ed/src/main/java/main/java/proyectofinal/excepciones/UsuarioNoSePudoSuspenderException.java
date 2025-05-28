package main.java.proyectofinal.excepciones;
/**
 * Excepción lanzada cuando un estado no es válido.
 * Puede ser causada por problemas de lógica de negocio o validación de datos.
 */
public class UsuarioNoSePudoSuspenderException extends Exception {
    public UsuarioNoSePudoSuspenderException() {
        super("No se pudo suspender al usuario");
    }

    public UsuarioNoSePudoSuspenderException(String mensaje) {
        super(mensaje);
    }

    public UsuarioNoSePudoSuspenderException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}