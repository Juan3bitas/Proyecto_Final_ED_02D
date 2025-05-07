package main.java.proyectofinal.utils;

import java.util.List;

import main.java.proyectofinal.modelo.Contenido;
import main.java.proyectofinal.modelo.Estudiante;
import main.java.proyectofinal.modelo.SolicitudAyuda;

public class UtilEstudiante {
    private static UtilEstudiante instancia;

    public static UtilEstudiante getInstance() {
        if (instancia == null) {
            instancia = new UtilEstudiante();
        }
        return instancia;
    }

    public boolean crearContenido(Contenido cont, Estudiante estudiante) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'crearContenido'");
    }

    public boolean eliminarContenido(String idCont) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'eliminarContenido'");
    }

    public void modificarContenido(Contenido cont) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'modificarContenido'");
    }

    public void pedirAyuda(String id, SolicitudAyuda ayuda) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'pedirAyuda'");
    }

    public List<Contenido> obtenerContenidosDeEstudiante(String id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'obtenerContenidosDeEstudiante'");
    }

    public void actualizarIntereses(Estudiante estudiante, String interes) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'actualizarIntereses'");
    }

    public void agregarValoracion(String id, String id2, int puntuacion, String comentario) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'agregarValoracion'");
    }
    
}
