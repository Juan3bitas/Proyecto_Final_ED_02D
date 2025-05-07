package main.java.proyectofinal.excepciones;

public class ContenidoNoSePudoEliminar extends Exception{
    public ContenidoNoSePudoEliminar(){
        super("No fue posible eliminar el contenido");
    }
}
