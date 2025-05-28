package main.java.proyectofinal.utils;

import java.util.List;
import main.java.proyectofinal.excepciones.ContenidoNoSePudoEliminar;
import main.java.proyectofinal.excepciones.OperacionFallidaException;
import main.java.proyectofinal.excepciones.UsuarioNoSePudoSuspenderException;
import main.java.proyectofinal.modelo.*;

public class UtilModerador {
    private static UtilRedSocial utilRS = UtilRedSocial.getInstance();
    private static UtilModerador instancia;
    private static RedSocial redSocial; // Instancia de RedSocial

    static {
        try {
            redSocial = new RedSocial(utilRS);
        } catch (OperacionFallidaException e) {
            throw new RuntimeException(e);
        }
    }

    public static UtilModerador getInstance() {
        if (instancia == null) {
            instancia = new UtilModerador();
        }
        return instancia;
    }

    public void suspenderUsuarioMod(String usuarioId, int tiempoDias) throws UsuarioNoSePudoSuspenderException {
        try {
            Object usuario = utilRS.buscarUsuario(usuarioId);
            if (usuario == null) throw new UsuarioNoSePudoSuspenderException("Usuario no encontrado");
            utilRS.suspenderUsuario(usuario, tiempoDias);
        } catch (Exception e) {
            throw new UsuarioNoSePudoSuspenderException("Error al suspender: " + e.getMessage());
        }
    }

    public void eliminarContenidoMod(String contId) throws ContenidoNoSePudoEliminar {
        try {
            Object contenido = utilRS.buscarContenido(contId);
            if (contenido == null) throw new ContenidoNoSePudoEliminar("Contenido no encontrado");
            if (!utilRS.eliminarContenido(contenido)) {
                throw new ContenidoNoSePudoEliminar("No se pudo eliminar el contenido");
            }
        } catch (Exception e) {
            throw new ContenidoNoSePudoEliminar("Error al eliminar: " + e.getMessage());
        }
    }

    public void generarReporte(TipoReporte tipo) throws OperacionFallidaException {
        switch (tipo) {
            case CONTENIDOS_VALORADOS:
                generarReporteContenidosValorados();
                break;
            case USUARIOS_ACTIVOS:
                generarReporteUsuariosActivos();
                break;
            case COMUNIDADES:
                generarReporteComunidades();
                break;
            default:
                throw new IllegalArgumentException("Tipo de reporte no válido");
        }
    }

    private void generarReporteContenidosValorados() {
        List<Contenido> contenidos = redSocial.getContenidosOrdenados();
        // Lógica para generar reporte de contenidos mejor valorados
        String reporte = "Reporte de Contenidos Valorados:\n";
        for (Contenido contenido : contenidos) {
            reporte += String.format("- %s (Valoración promedio: %.1f)\n", 
                contenido.getTitulo(), contenido.obtenerPromedioValoracion());
        }
        utilRS.guardarReporte(reporte);
    }

    private void generarReporteUsuariosActivos() throws OperacionFallidaException {
        List<Usuario> usuarios = redSocial.getUsuarios();
        // Lógica para generar reporte de actividad
        String reporte = "Reporte de Usuarios Activos:\n";
        for (Usuario usuario : usuarios) {
            if (usuario instanceof Estudiante) {
                Estudiante est = (Estudiante) usuario;
                reporte += String.format("- %s (Publicaciones: %d)\n", 
                    est.getNombre(), est.obtenerContenidosPublicados().size());
            }
        }
        utilRS.guardarReporte(reporte);
    }

    private void generarReporteComunidades() {
        List<List<Estudiante>> comunidades = redSocial.getGrafoAfinidad().detectarComunidades();
        String reporte = "Comunidades detectadas:\n";
        for (int i = 0; i < comunidades.size(); i++) {
            reporte += String.format("Comunidad %d (%d miembros):\n", i+1, comunidades.get(i).size());
            for (Estudiante est : comunidades.get(i)) {
                reporte += String.format("- %s\n", est.getNombre());
            }
        }
        utilRS.guardarReporte(reporte);
    }

    public void mostrarGrafo() {
        GrafoAfinidad grafo = redSocial.getGrafoAfinidad();

        System.out.println("=== VISUALIZACIÓN DEL GRAFO ===");
        System.out.println(grafo.toString());
    }

    public List<GrupoEstudio> obtenerComunidades() throws OperacionFallidaException {
        return utilRS.formarGruposAutomaticos(redSocial.getUsuarios());
    }
}