package main.java.proyectofinal.utils;

public class UtilGrupoEstudiante {
    private static UtilGrupoEstudiante instancia;

    public static UtilGrupoEstudiante getInstance() {
        if (instancia == null) {
            instancia = new UtilGrupoEstudiante();
        }
        return instancia;
    }

    public boolean agregarMiembro(String idEstudiante) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'agregarMiembro'");
    }

    public boolean eliminarMiembro(String idEstudiante) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'eliminarMiembro'");
    }

    public boolean agregarContenido(String idContenido) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'agregarContenido'");
    }

    public Object buscarUsuario(String usuarioId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'buscarUsuario'");
    }

}
