package main.java.proyectofinal.utils;

import java.util.List;

import main.java.proyectofinal.modelo.Estado;
import main.java.proyectofinal.modelo.GrupoEstudio;
import main.java.proyectofinal.modelo.RedSocial;
import main.java.proyectofinal.modelo.SolicitudAyuda;
import main.java.proyectofinal.modelo.Usuario;

public class UtilRedSocial {
    private static UtilRedSocial instancia;
    private RedSocial redSocial;
    private UtilLog utilLog;
    private UtilPersistencia utilPersistencia;

    public UtilRedSocial(){
        this.utilLog = UtilLog.getInstance();
        this.utilPersistencia = UtilPersistencia.getInstance();
        //this.utilSerializar = UtilSerializar.getInstance();
    }

    public static UtilRedSocial getInstance() {
        if (instancia == null) {
            instancia = new UtilRedSocial();
        }
        return instancia;
    }

    public Object buscarUsuario(String usuarioId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'buscarUsuario'");
    }

    public void suspenderUsuario(Object buscarUsuario, int tiempoDias) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'suspenderUsuario'");
    }

    public Object buscarContenido(String contId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'buscarContenido'");
    }

    public void eliminarContenido(Object buscarContenido) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'eliminarContenido'");
    }

    public void registrarUsuario(Usuario usuario) {
        if (utilPersistencia.buscarUsuarioCorreo(usuario.getCorreo()) == null) {
            utilPersistencia.guardarUsuarioArchivo(usuario);
            //utilSerializar.actualizarSerializacionUsuarios();
            marketPlace.getVendedores().add(vendedor);
            utilLog.registrarAccion("Vendedor nuevo ", "Registro exitoso.", "Registro.");
            return true;
        } else {
            // Excepcion de usuario existente
            utilLog.registrarAccion("Desconocido ", "Registro fallido. ", "Registro");
            throw new UsuarioExistenteException();
        }
    }

    public Usuario iniciarSesion(String correo, String contrasenia) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'iniciarSesion'");
    }

    public void generarRecomendaciones(String id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'generarRecomendaciones'");
    }

    public List<GrupoEstudio> formarGruposAutomaticos(List<Usuario> usuarios) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'formarGruposAutomaticos'");
    }

    public void eliminarUsuario(String id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'eliminarUsuario'");
    }

    public void modificarUsuario(Usuario usuario) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'modificarUsuario'");
    }

    public List<Usuario> obtenerUsuarios() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'obtenerUsuarios'");
    }

    public void crearSolicitud(SolicitudAyuda solicitudAyuda) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'crearSolicitud'");
    }

    public void eliminarSolicitud(String idSolicitud) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'eliminarSolicitud'");
    }

    public void modificarSolicitud(SolicitudAyuda solicitud) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'modificarSolicitud'");
    }

    public void obtenerSolicitudes() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'obtenerSolicitudes'");
    }

    public void guardarSolicitud(SolicitudAyuda solicitud) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'guardarSolicitud'");
    }

    public void actualizarEstadoSolicitud(String id, Estado resuelta) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'actualizarEstadoSolicitud'");
    }

}
