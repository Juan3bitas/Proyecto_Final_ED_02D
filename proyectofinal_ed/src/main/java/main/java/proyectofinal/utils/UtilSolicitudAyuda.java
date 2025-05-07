package main.java.proyectofinal.utils;

public class UtilSolicitudAyuda {

    private static UtilSolicitudAyuda instancia;

    public static UtilSolicitudAyuda getInstance() {
        if (instancia == null) {
            instancia = new UtilSolicitudAyuda();
        }
        return instancia;
    }


    public void cambiarEstadoSolicitud(String id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'cambiarEstadoSolicitud'");
    }

}
