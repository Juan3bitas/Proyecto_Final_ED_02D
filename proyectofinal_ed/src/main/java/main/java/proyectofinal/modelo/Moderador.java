package main.java.proyectofinal.modelo;

import main.java.proyectofinal.excepciones.UsuarioNoSePudoSuspenderException;
import main.java.proyectofinal.utils.UtilModerador;

public class Moderador extends Usuario{
    private UtilModerador utilModerador;

    public Moderador(){super();    
    }
    public Moderador(String id, String nombre, String correo, String contrasenia){
        super(id, nombre, correo, contrasenia);
    }

    public void suspenderUsuario(String usuarioId, int tiempoDias) throws UsuarioNoSePudoSuspenderException{
        utilModerador.suspenderUsuarioMod(usuarioId, tiempoDias);
    }
    
    public void eliminarContenido(Contenido cont){
        utilModerador.eliminarContenidoMod(cont);
    }

    public void generarReporte(String tipo){
        utilModerador.generarReporte(tipo);
    }


}
