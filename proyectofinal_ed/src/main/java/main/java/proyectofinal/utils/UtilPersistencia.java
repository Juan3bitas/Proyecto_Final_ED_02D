package main.java.proyectofinal.utils;
/* 
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import main.java.proyectofinal.modelo.Contenido;
import main.java.proyectofinal.modelo.GrupoEstudio;
import main.java.proyectofinal.modelo.SolicitudAyuda;
import main.java.proyectofinal.modelo.Usuario;

public class UtilPersistencia {
    private static UtilPersistencia instancia;
    private UtilProperties utilProperties;
    private UtilLog utilLog;
    private List<Usuario> listaUsuariosCache = new ArrayList<>();

    private UtilPersistencia() {
        this.utilProperties = UtilProperties.getInstance();
        this.utilLog = UtilLog.getInstance();
    }

    // metodo que se encarga de gestionar la escritura de las listas de los obj
    public void gestionarArchivos(List<Usuario> listaUsuarios, List<SolicitudAyuda> listaSolicitudes,
            List<Contenido> listaContenidos, List<GrupoEstudio> listaGruposEstudio) {
        String rutaUsuarios = utilProperties.obtenerPropiedad("rutaUsuarios.txt");
        String rutaSolicitudes = utilProperties.obtenerPropiedad("rutaSolicitudes.txt");
        String rutaContenidos = utilProperties.obtenerPropiedad("rutaContenidos.txt");
        String rutaGruposEstudio= utilProperties.obtenerPropiedad("rutaGruposEstudio.txt");
        escribirListaEnArchivo(rutaUsuarios, listaUsuarios);
        escribirListaEnArchivo(rutaSolicitudes, listaSolicitudes);
        escribirListaEnArchivo(rutaContenidos, listaContenidos);
        escribirListaEnArchivo(rutaGruposEstudio, listaGruposEstudio);
        utilLog.escribirLog("Archivos gestionados correctamente", Level.INFO);
    }


    private void escribirListaEnArchivo(String ruta, List<?> lista) {
        utilLog.escribirLog("Escribir lista en archivo: " + ruta, Level.INFO);

        if (ruta == null) {
            utilLog.escribirLog("La ruta de archivo es nula.", Level.SEVERE);
            return;
        }

        if (lista == null) {
            utilLog.escribirLog("La lista es nula y no se puede escribir en el archivo.", Level.SEVERE);
            return;
        }

        File archivo = new File(ruta);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(archivo))) {
            for (Object objeto : lista) {
                if (objeto == null) {
                    utilLog.escribirLog("El objeto en la lista es nulo, saltando este elemento.", Level.WARNING);
                    continue;
                }

                if (objeto instanceof Usuario) {
                    Usuario usuario = (Usuario) objeto;
                    guardarUsuarioEnArchivo(usuario);
                } else if (objeto instanceof SolicitudAyuda) {
                    SolicitudAyuda solicitud = (SolicitudAyuda) objeto;
                    guardarSolicitudAyudaEnArchivo(solicitud);
                } else if (objeto instanceof Contenido) {
                    Contenido contenido = (Contenido) objeto;
                    guardarContenidoEnArchivo(contenido);
                } else if (objeto instanceof GrupoEstudio) {
                    GrupoEstudio grupoEstudio = (GrupoEstudio) objeto;
                    guardarGrupoEstudioEnArchivo(grupoEstudio);
                } else {
                    utilLog.escribirLog("Tipo de objeto desconocido: " + objeto.getClass().getName(), Level.WARNING);
                }
            }
            utilLog.escribirLog("Lista escrita en archivo correctamente: " + ruta, Level.INFO);
        } catch (IOException e) {
            utilLog.escribirLog("Error al escribir en el archivo: " + ruta + ", " + e.getMessage(), Level.SEVERE);
        }
    }

    public Object buscarUsuarioCorreo(String correo) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'buscarUsuarioCorreo'");
    }

    public void guardarUsuarioArchivo(Usuario usuario) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'guardarUsuarioArchivo'");
    }

}*/
