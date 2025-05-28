package main.java.proyectofinal.modelo;

/*
 * Representa un moderador con permisos para suspender usuarios, eliminar contenidos y generar reportes.
 * Delega las operaciones complejas a UtilModerador.
 */

import java.util.List;

import main.java.proyectofinal.excepciones.ContenidoNoSePudoEliminar;
import main.java.proyectofinal.excepciones.OperacionFallidaException;
import main.java.proyectofinal.excepciones.UsuarioNoSePudoSuspenderException;
import main.java.proyectofinal.utils.UtilModerador;

public class Moderador extends Usuario {
    private transient UtilModerador utilModerador;

    public Moderador() {
        super();
        this.utilModerador = UtilModerador.getInstance();
    }

    public Moderador(String id, String nombre, String correo, String contrasenia, boolean suspendido, int diasSuspension) {
        super(id, nombre, correo, contrasenia, suspendido, diasSuspension);
        this.utilModerador = UtilModerador.getInstance();
    }

    public void suspenderUsuario(String usuarioId, int tiempoDias) throws UsuarioNoSePudoSuspenderException {
        utilModerador.suspenderUsuarioMod(usuarioId, tiempoDias);
    }

    public void eliminarContenido(String contId) throws ContenidoNoSePudoEliminar {
        utilModerador.eliminarContenidoMod(contId);
    }

    public void generarReporte(TipoReporte tipo) throws OperacionFallidaException {
        utilModerador.generarReporte(tipo);
    }

    public void visualizarGrafoAfinidad() {
        utilModerador.mostrarGrafo();
    }

    public List<GrupoEstudio> detectarComunidades() throws OperacionFallidaException {
        return utilModerador.obtenerComunidades();
    }

    @Override
    public String getTipo() {
        return "MODERADOR";
    }


}
