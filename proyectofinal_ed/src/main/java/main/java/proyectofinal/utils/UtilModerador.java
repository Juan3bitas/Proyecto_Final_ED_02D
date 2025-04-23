package main.java.proyectofinal.utils;

import main.java.proyectofinal.excepciones.UsuarioNoSePudoSuspenderException;
import main.java.proyectofinal.modelo.Contenido;
import main.java.proyectofinal.modelo.Usuario;

public class UtilModerador {
    UtilGrupoEstudiante utilGE;

    public void suspenderUsuarioMod(String usuarioId, int tiempoDias ) throws UsuarioNoSePudoSuspenderException {
        utilGE.suspenderUsuario(utilGE.buscarUsuario(usuarioId), tiempoDias);
        throw new UsuarioNoSePudoSuspenderException();
    }

    public void eliminarContenidoMod(Contenido cont) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'eliminarContenidoMod'");
    }

    public void generarReporte(String tipo) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'generarReporte'");
    }

}
