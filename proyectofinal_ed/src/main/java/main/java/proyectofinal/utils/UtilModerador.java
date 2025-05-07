package main.java.proyectofinal.utils;

import java.util.List;

import main.java.proyectofinal.excepciones.ContenidoNoSePudoEliminar;
import main.java.proyectofinal.excepciones.UsuarioNoSePudoSuspenderException;
import main.java.proyectofinal.modelo.Estudiante;
import main.java.proyectofinal.modelo.TipoReporte;

public class UtilModerador {
    private static UtilRedSocial utilRS=UtilRedSocial.getInstance();
    private static UtilModerador instancia;

    public static UtilModerador getInstance() {
        if (instancia == null) {
            instancia = new UtilModerador();
        }
        return instancia;
    }

    public void suspenderUsuarioMod(String usuarioId, int tiempoDias ) throws UsuarioNoSePudoSuspenderException {
        utilRS.suspenderUsuario(utilRS.buscarUsuario(usuarioId), tiempoDias);
        throw new UsuarioNoSePudoSuspenderException();
    }

    public void eliminarContenidoMod(String contId) throws ContenidoNoSePudoEliminar {
        utilRS.eliminarContenido(utilRS.buscarContenido(contId));
        throw new UnsupportedOperationException("Unimplemented method 'eliminarContenidoMod'");
    }

    public void generarReporte(TipoReporte tipo) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'generarReporte'");
    }

    public void mostrarGrafo() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'mostrarGrafo'");
    }

    public List<Estudiante> obtenerComunidades() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'obtenerComunidades'");
    }

}
