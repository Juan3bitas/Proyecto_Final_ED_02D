package main.java.proyectofinal.excepciones;

public class ContenidoNoSePudoEliminar extends Exception {
    public ContenidoNoSePudoEliminar() {
        super("No se pudo eliminar el contenido");
    }

    public ContenidoNoSePudoEliminar(String mensaje) {
        super(mensaje);
    }

    public ContenidoNoSePudoEliminar(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}