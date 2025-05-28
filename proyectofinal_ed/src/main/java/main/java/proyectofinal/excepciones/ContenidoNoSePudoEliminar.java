package main.java.proyectofinal.excepciones;

/**
 * Excepción lanzada cuando no se puede eliminar un contenido.
 * Puede ser causada por problemas de persistencia o lógica de negocio.
 */
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