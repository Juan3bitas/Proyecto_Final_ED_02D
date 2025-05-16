package main.java.proyectofinal.excepciones;

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