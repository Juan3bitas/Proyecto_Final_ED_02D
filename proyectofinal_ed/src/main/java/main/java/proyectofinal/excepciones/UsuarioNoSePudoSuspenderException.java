package main.java.proyectofinal.excepciones;

public class UsuarioNoSePudoSuspenderException extends Exception {
    public UsuarioNoSePudoSuspenderException(){
        super("No se pudo suspender el usuario");
    }
}
