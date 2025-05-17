package main.java.proyectofinal.utils;

import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

import main.java.proyectofinal.excepciones.*;
import main.java.proyectofinal.modelo.*;

public class UtilRedSocial {
    private static UtilRedSocial instancia;
    private final UtilPersistencia utilPersistencia;
    private final UtilLog utilLog;

    public UtilRedSocial() {
        this.utilPersistencia = UtilPersistencia.getInstance();
        this.utilLog = UtilLog.getInstance();
    }

    public static synchronized UtilRedSocial getInstance() {
        if (instancia == null) {
            instancia = new UtilRedSocial();
        }
        return instancia;
    }

    // ==================== USUARIOS ====================
    public Usuario buscarUsuario(String usuarioId) {
        try {
            return utilPersistencia.buscarUsuarioPorId(usuarioId);
        } catch (Exception e) {
            utilLog.logSevere("Error buscando usuario: " + e.getMessage());
            return null;
        }
    }

    public void suspenderUsuario(Object usuarioObj, int tiempoDias) {
        if (!(usuarioObj instanceof Usuario)) {
            throw new IllegalArgumentException("El objeto no es una instancia de Usuario");
        }
        Usuario usuario = (Usuario) usuarioObj;
        usuario.setSuspendido(true);
        usuario.setDiasSuspension(tiempoDias);
        utilPersistencia.actualizarUsuario(usuario);
        utilLog.logInfo("Usuario suspendido: " + usuario.getId());
    }

    public void registrarUsuario(Usuario usuario) {
        if (utilPersistencia.buscarUsuarioCorreo(usuario.getCorreo()) != null) {
            throw new IllegalStateException("El correo ya está registrado");
        }
        utilPersistencia.guardarUsuarioArchivo(usuario);
        utilLog.logInfo("Nuevo usuario registrado: " + usuario.getCorreo());
    }

    public Usuario iniciarSesion(String correo, String contrasenia) {
        Usuario usuario = (Usuario) utilPersistencia.buscarUsuarioCorreo(correo);
        if (usuario != null && usuario.getContrasenia().equals(contrasenia)) {
            return usuario;
        }
        return null;
    }

    public boolean eliminarUsuario(String id) {
        if (id == null || id.isEmpty()) {
            return false;
        }

        try {
            utilPersistencia.eliminarUsuario(id);
            utilLog.logInfo("Usuario eliminado: " + id);
            return true;
        } catch (Exception e) {
            utilLog.logSevere("Error al eliminar el usuario con ID " + id + ": " + e.getMessage());
            return false;
        }
    }


    public void modificarUsuario(Usuario usuario) {
        utilPersistencia.actualizarUsuario(usuario);
        utilLog.logInfo("Usuario actualizado: " + usuario.getId());
    }

    public List<Usuario> obtenerUsuarios() {
        return utilPersistencia.obtenerTodosUsuarios();
    }

    public Usuario buscarUsuarioPorId(String usuarioId) {
        return this.buscarUsuario(usuarioId);
    }

    // ==================== CONTENIDOS ====================
    public Contenido buscarContenido(String contId) {
        return utilPersistencia.buscarContenidoPorId(contId);
    }

    public boolean eliminarContenido(Object contenidoObj) {
        if (!(contenidoObj instanceof Contenido)) {
            throw new IllegalArgumentException("El objeto no es un Contenido válido");
        }
        return utilPersistencia.eliminarContenido(((Contenido) contenidoObj).getId());
    }

    public boolean guardarContenido(Contenido cont) {
        return utilPersistencia.guardarContenido(cont);
    }

    public void actualizarContenido(Contenido cont) {
        utilPersistencia.actualizarContenido(cont);
    }

    public List<Contenido> obtenerContenidosPorEstudiante(String idEstudiante) {
        return utilPersistencia.obtenerContenidosPorUsuario(idEstudiante);
    }


    public void agregarContenidoAEstudiante(String idEstudiante, String idContenido) {
        Estudiante estudiante = (Estudiante) this.buscarUsuarioPorId(idEstudiante);
        Contenido contenido = this.buscarContenido(idContenido);

        if (estudiante != null && contenido != null) {
            estudiante.agregarContenido(idContenido);
            // Ahora usando el método correcto
            this.actualizarEstudiante(estudiante);
        } else {
            utilLog.escribirLog(
                    "No se pudo agregar contenido al estudiante. Estudiante: " + (estudiante != null) +
                            ", Contenido: " + (contenido != null),
                    Level.WARNING
            );
        }
    }

    // ==================== SOLICITUDES ====================
    public void crearSolicitud(SolicitudAyuda solicitud) {
        utilPersistencia.guardarSolicitud(solicitud);
    }

    public void eliminarSolicitud(String idSolicitud) {
        utilPersistencia.eliminarSolicitud(idSolicitud);
    }

    public void modificarSolicitud(SolicitudAyuda solicitud) {
        utilPersistencia.actualizarSolicitud(solicitud);
    }

    public List<SolicitudAyuda> obtenerSolicitudes() {
        return utilPersistencia.obtenerTodasSolicitudes();
    }

    public void guardarSolicitud(SolicitudAyuda solicitud) {
        this.crearSolicitud(solicitud);
    }

    public void actualizarEstadoSolicitud(String id, Estado estado) {
        SolicitudAyuda solicitud = utilPersistencia.buscarSolicitudPorId(id);
        if (solicitud != null) {
            solicitud.setEstado(estado);
            utilPersistencia.actualizarSolicitud(solicitud);
        }
    }

    public SolicitudAyuda buscarSolicitud(String id) {
        return utilPersistencia.buscarSolicitudPorId(id);
    }

    public void actualizarSolicitud(SolicitudAyuda solicitud) {
        this.modificarSolicitud(solicitud);
    }

    public void agregarSolicitudAyuda(SolicitudAyuda ayuda) {
        this.crearSolicitud(ayuda);
    }

    // ==================== GRUPOS ====================

    private GrupoEstudio crearNuevoGrupo(List<Estudiante> miembros, int numeroGrupo) {
        return new GrupoEstudio(
                "GRP-" + UUID.randomUUID().toString().substring(0, 8),
                "Grupo " + numeroGrupo,
                "Grupo autoformado",
                new LinkedList<>(miembros.stream()
                        .map(Estudiante::getId)
                        .collect(Collectors.toList())),
                new LinkedList<>(),
                new Date()
        );
    }

    public List<GrupoEstudio> formarGruposAutomaticos(List<Usuario> usuarios) {
        // 1. Filtrar estudiantes y mezclar
        List<Estudiante> estudiantes = usuarios.stream()
                .filter(u -> u instanceof Estudiante)
                .map(u -> (Estudiante) u)
                .collect(Collectors.toList());
        Collections.shuffle(estudiantes);

        // 2. Crear grupos
        final int TAMAÑO_GRUPO = 5;
        List<GrupoEstudio> grupos = new ArrayList<>();

        for (int i = 0; i < estudiantes.size(); i += TAMAÑO_GRUPO) {
            List<Estudiante> miembros = estudiantes.subList(
                    i,
                    Math.min(i + TAMAÑO_GRUPO, estudiantes.size())
            );

            grupos.add(new GrupoEstudio(
                    "GRP-" + UUID.randomUUID().toString().substring(0, 8),
                    "Grupo " + (grupos.size() + 1),
                    "Grupo autoformado",
                    new LinkedList<>(miembros.stream()
                            .map(Estudiante::getId)
                            .collect(Collectors.toList())),
                    new LinkedList<>(),
                    new Date()
            ));
        }

        return grupos;
    }


    public boolean agregarMiembroAGrupo(String grupoId, String estudianteId) throws OperacionFallidaException {
        GrupoEstudio grupo = utilPersistencia.buscarGrupoPorId(grupoId);
        Estudiante estudiante = (Estudiante) this.buscarUsuario(estudianteId);
        
        if (grupo != null && estudiante != null) {
            grupo.agregarMiembro(estudianteId);
            estudiante.unirseAGrupo(grupoId);
            utilPersistencia.actualizarGrupo(grupo);
            utilPersistencia.actualizarEstudiante(estudiante);
            return true;
        }
        return false;
    }

    public boolean eliminarMiembroDeGrupo(String grupoId, String estudianteId) throws OperacionFallidaException {
        GrupoEstudio grupo = utilPersistencia.buscarGrupoPorId(grupoId);
        Estudiante estudiante = (Estudiante) this.buscarUsuario(estudianteId);
        
        if (grupo != null && estudiante != null) {
            grupo.eliminarMiembro(estudianteId);
            estudiante.dejarGrupo(grupoId);
            utilPersistencia.actualizarGrupo(grupo);
            utilPersistencia.actualizarEstudiante(estudiante);
            return true;
        }
        return false;
    }

    public boolean agregarContenidoAGrupo(String grupoId, String contenidoId) throws OperacionFallidaException {
        GrupoEstudio grupo = utilPersistencia.buscarGrupoPorId(grupoId);
        Contenido contenido = this.buscarContenido(contenidoId);
        
        if (grupo != null && contenido != null) {
            grupo.agregarContenido(contenidoId);
            utilPersistencia.actualizarGrupo(grupo);
            return true;
        }
        return false;
    }




    // ==================== VALORACIONES ====================
    public List<GrupoEstudio> generarRecomendaciones(String idUsuario) {
        try {
            Objects.requireNonNull(idUsuario, "ID de usuario no puede ser nulo");
            
            Estudiante estudiante = (Estudiante) this.buscarUsuario(idUsuario);
            if (estudiante == null) {
                utilLog.logWarning("Estudiante no encontrado para recomendaciones: " + idUsuario);
                return Collections.emptyList();
            }
    
            // 1. Obtener intereses del estudiante
            List<String> interesesEstudiante = estudiante.getIntereses();
            if (interesesEstudiante.isEmpty()) {
                utilLog.logInfo("Estudiante sin intereses registrados: " + idUsuario);
                return Collections.emptyList();
            }
    
            // 2. Obtener todos los grupos disponibles
            List<GrupoEstudio> todosGrupos = utilPersistencia.obtenerTodosGrupos();
            
            // 3. Filtrar grupos a los que el estudiante no pertenece
            List<GrupoEstudio> gruposNoPertenece = todosGrupos.stream()
                .filter(grupo -> !grupo.getIdMiembros().contains(idUsuario))
                .collect(Collectors.toList());
    
            // 4. Calcular puntaje de afinidad para cada grupo
            Map<GrupoEstudio, Integer> gruposConPuntaje = new HashMap<>();
            
            for (GrupoEstudio grupo : gruposNoPertenece) {
                // Obtener intereses de los miembros del grupo
                Set<String> interesesDelGrupo = new HashSet<>(); // Variable renombrada
                for (String miembroId : grupo.getIdMiembros()) {
                    Estudiante miembro = (Estudiante) this.buscarUsuario(miembroId);
                    if (miembro != null) {
                        interesesDelGrupo.addAll(miembro.getIntereses());
                    }
                }
                
                // Calcular coincidencias de intereses (corregido aquí)
                int puntaje = (int) interesesEstudiante.stream()
                    .filter(interes -> interesesDelGrupo.contains(interes)) // Corregido
                    .count();
                
                if (puntaje > 0) {
                    gruposConPuntaje.put(grupo, puntaje);
                }
            }
    
            // 5. Ordenar grupos por puntaje de afinidad
            List<GrupoEstudio> recomendaciones = gruposConPuntaje.entrySet().stream()
                .sorted(Map.Entry.<GrupoEstudio, Integer>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .limit(5)
                .collect(Collectors.toList());
    
            utilLog.logInfo("Generadas " + recomendaciones.size() + 
                          " recomendaciones para: " + idUsuario);
            
            return recomendaciones;
    
        } catch (Exception e) {
            utilLog.logSevere("Error generando recomendaciones: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public GrupoEstudio buscarGrupoPorId(String grupoId) {
        try {
            Objects.requireNonNull(grupoId, "ID de grupo no puede ser nulo");
            if (grupoId.trim().isEmpty()) {
                throw new IllegalArgumentException("ID de grupo no puede estar vacío");
            }
            
            return utilPersistencia.buscarGrupoPorId(grupoId);
        } catch (Exception e) {
            utilLog.logSevere("Error buscando grupo: " + e.getMessage());
            return null;
        }
    }
    
    public List<String> obtenerGruposDeEstudiante(String estudianteId) {
        try {
            Objects.requireNonNull(estudianteId, "ID de estudiante no puede ser nulo");
            if (estudianteId.trim().isEmpty()) {
                throw new IllegalArgumentException("ID de estudiante no puede estar vacío");
            }
            
            // 1. Obtener todos los grupos
            List<GrupoEstudio> todosGrupos = utilPersistencia.obtenerTodosGrupos();
            
            // 2. Filtrar los grupos donde el estudiante es miembro
            return todosGrupos.stream()
                    .filter(grupo -> grupo.getIdMiembros().contains(estudianteId))
                    .map(grupo -> grupo.getIdGrupo())  // Forma corregida
                    .collect(Collectors.toList());
                    
        } catch (Exception e) {
            utilLog.logSevere("Error obteniendo grupos del estudiante: " + e.getMessage());
            return Collections.emptyList();
        }
    }
    
    public void guardarGrupo(GrupoEstudio nuevoGrupo) {
        try {
            Objects.requireNonNull(nuevoGrupo, "Grupo no puede ser nulo");
            
            // Validar campos requeridos
            if (nuevoGrupo.getIdGrupo() == null || nuevoGrupo.getIdGrupo().isEmpty()) {
                throw new IllegalArgumentException("ID de grupo inválido");
            }
            if (nuevoGrupo.getNombre() == null || nuevoGrupo.getNombre().isEmpty()) {
                throw new IllegalArgumentException("Nombre de grupo inválido");
            }
            if (nuevoGrupo.getIdMiembros() == null) {
                throw new IllegalArgumentException("Lista de miembros no puede ser nula");
            }
            
            // Verificar si el grupo ya existe
            GrupoEstudio existente = utilPersistencia.buscarGrupoPorId(nuevoGrupo.getIdGrupo());
            if (existente != null) {
                throw new IllegalStateException("Ya existe un grupo con este ID");
            }
            
            // Guardar el grupo
            utilPersistencia.guardarGrupo(nuevoGrupo);
            utilLog.logInfo("Nuevo grupo creado: " + nuevoGrupo.getIdGrupo());
            
        } catch (Exception e) {
            utilLog.logSevere("Error guardando grupo: " + e.getMessage());
            throw new RuntimeException("Error al guardar grupo", e);
        }
    }

    public void guardarReporte(String reporte) {
        try {
            Objects.requireNonNull(reporte, "El reporte no puede ser nulo");
            utilPersistencia.guardarReporte(reporte);
            utilLog.logInfo("Reporte guardado: " + reporte);
        } catch (Exception e) {
            utilLog.logSevere("Error guardando reporte: " + e.getMessage());
        }
    }

    public boolean actualizarEstudiante(Estudiante estudiante) {
        if (estudiante == null) {
            return false;
        }

        try {
            utilPersistencia.actualizarEstudiante(estudiante);
            utilLog.logInfo("Estudiante actualizado: " + estudiante.getId());
            return true;
        } catch (Exception e) {
            utilLog.logSevere("Error actualizando estudiante: " + e.getMessage());
            return false;
        }
    }


    public void agregarValoracionAContenido(String idContenido, String idEstudiante, int puntuacion, String comentario) {
        // Validaciones mejoradas
        Objects.requireNonNull(idContenido, "El ID del contenido no puede ser nulo");
        Objects.requireNonNull(idEstudiante, "El ID del estudiante no puede ser nulo");

        if (puntuacion < 1 || puntuacion > 5) {
            throw new IllegalArgumentException("La puntuación debe estar entre 1 y 5");
        }

        // Buscar contenido con manejo de excepciones
        try {
            Contenido contenido = this.buscarContenido(idContenido);
            if (contenido == null) {
                utilLog.logWarning("Contenido no encontrado para agregar valoración: " + idContenido);
                return;
            }

            // Crear valoración con fecha actual
            Valoracion valoracion = new Valoracion(
                    null,                       // ID se generará automáticamente
                    contenido.getTema(),        // Tema del contenido
                    contenido.getDescripcion(), // Descripción del contenido
                    idEstudiante,              // Autor de la valoración
                    puntuacion,                 // Puntuación
                    new Date(),                 // Fecha actual
                    comentario                  // Comentario
            );

            // Agregar y actualizar
            contenido.agregarValoracion(valoracion);
            this.actualizarContenido(contenido);



        } catch (Exception e) {
            utilLog.logSevere("Error al agregar valoración: " + e.getMessage());
            throw new RuntimeException("Error al agregar valoración", e);
        }
    }

    public List<Contenido> obtenerContenidosPorAutor(String idEstudiante) {
        return utilPersistencia.buscarContenidoPorAutor(idEstudiante);
    }

    public List<Contenido> obtenerContenidosPorTema(String tema) {
        return utilPersistencia.buscarContenidoPorTema(tema);
    }

    public List<Contenido> obtenerContenidosPorTipo(TipoContenido tipo) {
        return utilPersistencia.buscarContenidoPorTipo(tipo);
    }


    public List<Contenido> obtenerContenidos() {
        return utilPersistencia.obtenerTodosContenidos();
    }

    public List<GrupoEstudio> obtenerGrupos() {
        return utilPersistencia.obtenerTodosGrupos();
    }

    public void guardarGrupos(List<GrupoEstudio> grupos) {
        for (GrupoEstudio grupo : grupos) {
            utilPersistencia.guardarGrupo(grupo);
        }
        utilLog.logInfo("Grupos guardados en la persistencia");
    }
}