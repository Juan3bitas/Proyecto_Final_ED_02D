package main.java.proyectofinal.excepciones;


/**
 * Excepción lanzada cuando una operación de persistencia o negocio falla.
 * Incluye detalles técnicos para logging y mensajes claros para el usuario.
 */
public class OperacionFallidaException extends Exception {
    private final String codigoError;  

    public OperacionFallidaException(String mensaje) {
        super(mensaje);
        this.codigoError = "ERR-000";  
    }

    public OperacionFallidaException(String mensaje, String codigoError, Throwable causa) {
        super(mensaje, causa);
        this.codigoError = codigoError;
    }


    public String getCodigoError() {
        return this.codigoError;
    }

    
    public static OperacionFallidaException crearErrorPersistencia(String operacion, Throwable causa) {
        String mensaje = "Fallo al " + operacion + ". Contacte al administrador.";
        return new OperacionFallidaException(mensaje, "ERR-DB-100", causa);
    }
}