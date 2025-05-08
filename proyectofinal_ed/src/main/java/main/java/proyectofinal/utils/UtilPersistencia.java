package main.java.proyectofinal.utils;

import java.io.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;
import main.java.proyectofinal.modelo.*;

public class UtilPersistencia {
    private static UtilPersistencia instancia;
    private UtilProperties utilProperties;
    private UtilLog utilLog;
    private List<Usuario> listaUsuariosCache = new ArrayList<>();
    private List<Contenido> listaContenidosCache = new ArrayList<>();
    private List<SolicitudAyuda> listaSolicitudesCache = new ArrayList<>();
    private List<GrupoEstudio> listaGruposCache = new ArrayList<>();

    private UtilPersistencia() {
        this.utilProperties = UtilProperties.getInstance();
        this.utilLog = UtilLog.getInstance();
        cargarDatosIniciales();
    }

    public static synchronized UtilPersistencia getInstance() {
        if (instancia == null) {
            instancia = new UtilPersistencia();
        }
        return instancia;
    }

    private void cargarDatosIniciales() {
        cargarUsuarios();
        cargarContenidos();
        cargarSolicitudes();
        cargarGrupos();
    }

    // ==================== MÉTODOS DE USUARIO ====================
    public Object buscarUsuarioCorreo(String correo) {
        try {
            return listaUsuariosCache.stream()
                    .filter(u -> u.getCorreo().equalsIgnoreCase(correo))
                    .findFirst()
                    .orElse(null);
        } catch (Exception e) {
            utilLog.escribirLog("Error buscando usuario: " + e.getMessage(), Level.SEVERE);
            return null;
        }
    }

    public void guardarUsuarioArchivo(Usuario usuario) {
        try {
            listaUsuariosCache.add(usuario);
            guardarUsuarioEnArchivo(usuario);
        } catch (Exception e) {
            utilLog.escribirLog("Error guardando usuario: " + e.getMessage(), Level.SEVERE);
        }
    }

    public Usuario buscarUsuarioPorId(String usuarioId) {
        try {
            return listaUsuariosCache.stream()
                    .filter(u -> u.getId().equals(usuarioId))
                    .findFirst()
                    .orElse(null);
        } catch (Exception e) {
            utilLog.escribirLog("Error buscando usuario: " + e.getMessage(), Level.SEVERE);
            return null;
        }
    }

    public void actualizarUsuario(Usuario usuario) {
        try {
            listaUsuariosCache.removeIf(u -> u.getId().equals(usuario.getId()));
            listaUsuariosCache.add(usuario);
            guardarTodosUsuarios();
        } catch (Exception e) {
            utilLog.escribirLog("Error actualizando usuario: " + e.getMessage(), Level.SEVERE);
        }
    }

    public void eliminarUsuario(String id) {
        try {
            listaUsuariosCache.removeIf(u -> u.getId().equals(id));
            guardarTodosUsuarios();
        } catch (Exception e) {
            utilLog.escribirLog("Error eliminando usuario: " + e.getMessage(), Level.SEVERE);
        }
    }

    public List<Usuario> obtenerTodosUsuarios() {
        return new ArrayList<>(listaUsuariosCache);
    }

    public Collection<Usuario> obtenerTodosEstudiantes() {
        return listaUsuariosCache.stream()
                .filter(u -> u instanceof Estudiante)
                .collect(Collectors.toList());
    }

    // ==================== MÉTODOS DE CONTENIDO ====================
    public Contenido buscarContenidoPorId(String contId) {
        try {
            return listaContenidosCache.stream()
                    .filter(c -> c.getId().equals(contId))
                    .findFirst()
                    .orElse(null);
        } catch (Exception e) {
            utilLog.escribirLog("Error buscando contenido: " + e.getMessage(), Level.SEVERE);
            return null;
        }
    }

    public boolean eliminarContenido(String id) {
        try {
            boolean removed = listaContenidosCache.removeIf(c -> c.getId().equals(id));
            guardarTodosContenidos();
            return removed;
        } catch (Exception e) {
            utilLog.escribirLog("Error eliminando contenido: " + e.getMessage(), Level.SEVERE);
            return false;
        }
    }

    public boolean guardarContenido(Contenido cont) {
        try {
            listaContenidosCache.add(cont);
            guardarContenidoEnArchivo(cont);
            return true;
        } catch (Exception e) {
            utilLog.escribirLog("Error guardando contenido: " + e.getMessage(), Level.SEVERE);
            return false;
        }
    }

    public void actualizarContenido(Contenido cont) {
        try {
            listaContenidosCache.removeIf(c -> c.getId().equals(cont.getId()));
            listaContenidosCache.add(cont);
            guardarTodosContenidos();
        } catch (Exception e) {
            utilLog.escribirLog("Error actualizando contenido: " + e.getMessage(), Level.SEVERE);
        }
    }

    public List<Contenido> obtenerContenidosPorUsuario(String idEstudiante) {
        try {
            return listaContenidosCache.stream()
                    .filter(c -> c.getAutor().equals(idEstudiante))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            utilLog.escribirLog("Error obteniendo contenidos: " + e.getMessage(), Level.SEVERE);
            return Collections.emptyList();
        }
    }

    public void actualizarEstudiante(Estudiante estudiante) {
        actualizarUsuario(estudiante);
    }

    // ==================== MÉTODOS DE SOLICITUDES ====================
    public void guardarSolicitud(SolicitudAyuda solicitud) {
        try {
            listaSolicitudesCache.add(solicitud);
            guardarSolicitudEnArchivo(solicitud);
        } catch (Exception e) {
            utilLog.escribirLog("Error guardando solicitud: " + e.getMessage(), Level.SEVERE);
        }
    }

    public void eliminarSolicitud(String idSolicitud) {
        try {
            listaSolicitudesCache.removeIf(s -> s.getId().equals(idSolicitud));
            guardarTodasSolicitudes();
        } catch (Exception e) {
            utilLog.escribirLog("Error eliminando solicitud: " + e.getMessage(), Level.SEVERE);
        }
    }

    public void actualizarSolicitud(SolicitudAyuda solicitud) {
        try {
            listaSolicitudesCache.removeIf(s -> s.getId().equals(solicitud.getId()));
            listaSolicitudesCache.add(solicitud);
            guardarTodasSolicitudes();
        } catch (Exception e) {
            utilLog.escribirLog("Error actualizando solicitud: " + e.getMessage(), Level.SEVERE);
        }
    }

    public List<SolicitudAyuda> obtenerTodasSolicitudes() {
        return new ArrayList<>(listaSolicitudesCache);
    }

    public SolicitudAyuda buscarSolicitudPorId(String id) {
        try {
            return listaSolicitudesCache.stream()
                    .filter(s -> s.getId().equals(id))
                    .findFirst()
                    .orElse(null);
        } catch (Exception e) {
            utilLog.escribirLog("Error buscando solicitud: " + e.getMessage(), Level.SEVERE);
            return null;
        }
    }

    // ==================== MÉTODOS DE GRUPOS ====================
    public void guardarGrupo(GrupoEstudio grupo) {
        try {
            listaGruposCache.add(grupo);
            guardarGrupoEnArchivo(grupo);
        } catch (Exception e) {
            utilLog.escribirLog("Error guardando grupo: " + e.getMessage(), Level.SEVERE);
        }
    }

    public GrupoEstudio buscarGrupoPorId(String grupoId) {
        try {
            return listaGruposCache.stream()
                    .filter(g -> g.getIdGrupo().equals(grupoId))
                    .findFirst()
                    .orElse(null);
        } catch (Exception e) {
            utilLog.escribirLog("Error buscando grupo: " + e.getMessage(), Level.SEVERE);
            return null;
        }
    }

    public void actualizarGrupo(GrupoEstudio grupo) {
        try {
            listaGruposCache.removeIf(g -> g.getIdGrupo().equals(grupo.getIdGrupo()));
            listaGruposCache.add(grupo);
            guardarTodosGrupos();
        } catch (Exception e) {
            utilLog.escribirLog("Error actualizando grupo: " + e.getMessage(), Level.SEVERE);
        }
    }

    public List<GrupoEstudio> obtenerTodosGrupos() {
        return new ArrayList<>(listaGruposCache);
    }

    // ==================== MÉTODOS DE REPORTES ====================
    public void guardarReporte(String reporte) {
        String ruta = utilProperties.obtenerPropiedad("rutaReportes.txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(ruta, true))) {
            writer.write(reporte);
            writer.newLine();
        } catch (IOException e) {
            utilLog.escribirLog("Error guardando reporte: " + e.getMessage(), Level.SEVERE);
        }
    }

    // ==================== MÉTODOS PRIVADOS ====================


    private void guardarUsuarioEnArchivo(Usuario usuario) throws IOException {
        String ruta = utilProperties.obtenerPropiedad("rutaUsuarios.txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(ruta, true))) {
            writer.write(usuarioToCsv(usuario));
            writer.newLine();
        }
    }

    private void guardarTodosUsuarios() throws IOException {
        String ruta = utilProperties.obtenerPropiedad("rutaUsuarios.txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(ruta))) {
            for (Usuario usuario : listaUsuariosCache) {
                writer.write(usuarioToCsv(usuario));
                writer.newLine();
            }
        }
    }

    private String usuarioToCsv(Usuario usuario) {
        return String.join(",",
            usuario.getId(),
            usuario.getNombre(),
            usuario.getCorreo(),
            usuario.getContrasenia(),
            String.valueOf(usuario.isSuspendido()),
            String.valueOf(usuario.getDiasSuspension())
        );
    }

    public void gestionarArchivos(List<Usuario> listaUsuarios, List<SolicitudAyuda> listaSolicitudes,
            List<Contenido> listaContenidos, List<GrupoEstudio> listaGruposEstudio) throws IOException {
        if (listaUsuarios != null) this.listaUsuariosCache = new ArrayList<>(listaUsuarios);
        if (listaSolicitudes != null) this.listaSolicitudesCache = new ArrayList<>(listaSolicitudes);
        if (listaContenidos != null) this.listaContenidosCache = new ArrayList<>(listaContenidos);
        if (listaGruposEstudio != null) this.listaGruposCache = new ArrayList<>(listaGruposEstudio);

        guardarTodosUsuarios();
        guardarTodasSolicitudes();
        guardarTodosContenidos();
        guardarTodosGrupos();
    }

    //////////////
    private void cargarUsuarios() {
        String ruta = utilProperties.obtenerPropiedad("rutaUsuarios.txt");
        try (BufferedReader reader = new BufferedReader(new FileReader(ruta))) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                String[] datos = linea.split(",");
                if (datos.length >= 7) { // Asegúrate de que hay suficientes datos
                    String tipoUsuario = datos[6]; // Suponiendo que el tipo está en la posición 6
                    Usuario usuario = null;
    
                    // Crear el usuario según el tipo
                    if ("Estudiante".equalsIgnoreCase(tipoUsuario)) {
                        usuario = new Estudiante(datos[0], datos[1], datos[2], datos[3], 
                                                 Boolean.parseBoolean(datos[4]), 
                                                 Integer.parseInt(datos[5]), 
                                                 new LinkedList<>(), new ArrayList<>());
                    } else if ("Moderador".equalsIgnoreCase(tipoUsuario)) {
                        usuario = new Moderador(datos[0], datos[1], datos[2], datos[3], 
                                                Boolean.parseBoolean(datos[4]), 
                                                Integer.parseInt(datos[5]));
                    }
    
                    if (usuario != null) {
                        listaUsuariosCache.add(usuario);
                    }
                }
            }
        } catch (IOException e) {
            utilLog.escribirLog("Error cargando usuarios: " + e.getMessage(), Level.SEVERE);
        }
    }
    

private void cargarContenidos() {
    String ruta = utilProperties.obtenerPropiedad("rutaContenidos.txt");
    try (BufferedReader reader = new BufferedReader(new FileReader(ruta))) {
        String linea;
        while ((linea = reader.readLine()) != null) {
            String[] datos = linea.split("\\|");
            if (datos.length >= 7) {
                Contenido contenido = new Contenido(
                    datos[0], // id
                    datos[1], // título
                    datos[2], // autor
                    LocalDateTime.parse(datos[3]), // fecha
                    TipoContenido.valueOf(datos[4]), // tipo
                    datos[5], // descripción
                    datos[6]  // tema
                );
                listaContenidosCache.add(contenido);
            }
        }
    } catch (IOException e) {
        utilLog.escribirLog("Error cargando contenidos: " + e.getMessage(), Level.SEVERE);
    }
}

private void cargarSolicitudes() {
    String ruta = utilProperties.obtenerPropiedad("rutaSolicitudes.txt");
    try (BufferedReader reader = new BufferedReader(new FileReader(ruta))) {
        String linea;
        while ((linea = reader.readLine()) != null) {
            String[] datos = linea.split(";");
            if (datos.length >= 6) {
                SolicitudAyuda solicitud = new SolicitudAyuda(
                    datos[0], // id
                    datos[1], // tema
                    datos[2], // descripción
                    new Date(Long.parseLong(datos[3])), // fecha
                    Urgencia.valueOf(datos[4]), // urgencia
                    datos[5]  // solicitanteId
                );
                solicitud.setEstado(Estado.valueOf(datos[6])); // estado
                listaSolicitudesCache.add(solicitud);
            }
        }
    } catch (IOException e) {
        utilLog.escribirLog("Error cargando solicitudes: " + e.getMessage(), Level.SEVERE);
    }
}

private void cargarGrupos() {
    String ruta = utilProperties.obtenerPropiedad("rutaGruposEstudio.txt");
    try (BufferedReader reader = new BufferedReader(new FileReader(ruta))) {
        String linea;
        while ((linea = reader.readLine()) != null) {
            String[] datos = linea.split("#");
            if (datos.length >= 5) {
                LinkedList<String> miembros = new LinkedList<>(Arrays.asList(datos[3].split(",")));
                LinkedList<String> contenidos = new LinkedList<>(Arrays.asList(datos[4].split(",")));
                
                GrupoEstudio grupo = new GrupoEstudio(
                    datos[0], // id
                    datos[1], // nombre
                    datos[2], // descripción
                    miembros,
                    contenidos,
                    new Date(Long.parseLong(datos[5])) // fechaCreación
                );
                listaGruposCache.add(grupo);
            }
        }
    } catch (IOException e) {
        utilLog.escribirLog("Error cargando grupos: " + e.getMessage(), Level.SEVERE);
    }
}

private void guardarContenidoEnArchivo(Contenido contenido) throws IOException {
    String ruta = utilProperties.obtenerPropiedad("rutaContenidos.txt");
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(ruta, true))) {
        writer.write(contenidoToCsv(contenido));
        writer.newLine();
    }
}

private String contenidoToCsv(Contenido contenido) {
    return String.join("|",
        contenido.getId(),
        contenido.getTitulo(),
        contenido.getAutor(),
        contenido.getFecha().toString(),
        contenido.getTipo().name(),
        contenido.getDescripcion(),
        contenido.getTema()
    );
}

private void guardarTodosContenidos() throws IOException {
    String ruta = utilProperties.obtenerPropiedad("rutaContenidos.txt");
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(ruta))) {
        for (Contenido contenido : listaContenidosCache) {
            writer.write(contenidoToCsv(contenido));
            writer.newLine();
        }
    }
}

private void guardarSolicitudEnArchivo(SolicitudAyuda solicitud) throws IOException {
    String ruta = utilProperties.obtenerPropiedad("rutaSolicitudes.txt");
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(ruta, true))) {
        writer.write(solicitudToCsv(solicitud));
        writer.newLine();
    }
}

private String solicitudToCsv(SolicitudAyuda solicitud) {
    return String.join(";",
        solicitud.getId(),
        solicitud.getTema(),
        solicitud.getDescripcion(),
        String.valueOf(solicitud.getFecha().getTime()),
        solicitud.getUrgencia().name(),
        solicitud.getSolicitanteId(),
        solicitud.getEstado().name()
    );
}

private void guardarTodasSolicitudes() throws IOException {
    String ruta = utilProperties.obtenerPropiedad("rutaSolicitudes.txt");
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(ruta))) {
        for (SolicitudAyuda solicitud : listaSolicitudesCache) {
            writer.write(solicitudToCsv(solicitud));
            writer.newLine();
        }
    }
}

private void guardarGrupoEnArchivo(GrupoEstudio grupo) throws IOException {
    String ruta = utilProperties.obtenerPropiedad("rutaGruposEstudio.txt");
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(ruta, true))) {
        writer.write(grupoToCsv(grupo));
        writer.newLine();
    }
}

private String grupoToCsv(GrupoEstudio grupo) {
    return String.join("#",
        grupo.getIdGrupo(),
        grupo.getNombre(),
        grupo.getDescripcion(),
        String.join(",", grupo.getIdMiembros()),
        String.join(",", grupo.getIdContenidos()),
        String.valueOf(grupo.getFechaCreacion().getTime())
    );
}

private void guardarTodosGrupos() throws IOException {
    String ruta = utilProperties.obtenerPropiedad("rutaGruposEstudio.txt");
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(ruta))) {
        for (GrupoEstudio grupo : listaGruposCache) {
            writer.write(grupoToCsv(grupo));
            writer.newLine();
        }
    }
}
}