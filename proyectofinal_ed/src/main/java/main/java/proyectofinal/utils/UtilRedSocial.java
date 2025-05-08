package main.java.proyectofinal.utils;

import java.util.*;
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

    public void eliminarUsuario(String id) {
        utilPersistencia.eliminarUsuario(id);
        utilLog.logInfo("Usuario eliminado: " + id);
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
        Estudiante estudiante = (Estudiante) this.buscarUsuario(idEstudiante);
        Contenido contenido = this.buscarContenido(idContenido);
        
        if (estudiante != null && contenido != null) {
            estudiante.agregarContenido(idContenido);
            utilPersistencia.actualizarEstudiante(estudiante);
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
    public List<GrupoEstudio> formarGruposAutomaticos(List<Usuario> usuarios) {
        List<Estudiante> estudiantes = new ArrayList<>();
        for (Usuario u : usuarios) {
            if (u instanceof Estudiante) {
                estudiantes.add((Estudiante) u);
            }
        }
        
        List<GrupoEstudio> grupos = new ArrayList<>();
        int tamanoGrupo = 5;
        
        for (int i = 0; i < estudiantes.size(); i += tamanoGrupo) {
            List<Estudiante> subgrupo = estudiantes.subList(i, Math.min(i + tamanoGrupo, estudiantes.size()));
            
            // Corrección aquí: Usar LinkedList para idMiembros como espera el constructor
            LinkedList<String> idMiembros = subgrupo.stream()
                .map(Estudiante::getId)
                .collect(Collectors.toCollection(LinkedList::new));
            
            GrupoEstudio grupo = new GrupoEstudio(
                "GRP-" + UUID.randomUUID().toString().substring(0, 8),
                "Grupo " + (grupos.size() + 1),
                "Grupo autoformado",
                idMiembros,  // Usamos LinkedList como espera el constructor
                new LinkedList<>(),  // idContenidos vacío
                new Date()
            );
            
            utilPersistencia.guardarGrupo(grupo);
            grupos.add(grupo);
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

    // ==================== GRAFO AFINIDAD ====================
    public void actualizarGrafoAfinidad(String idUsuario, String interes) throws OperacionFallidaException {
        Estudiante estudiante = (Estudiante) this.buscarUsuario(idUsuario);
        if (estudiante != null) {
            estudiante.agregarInteres(interes);
            utilPersistencia.actualizarEstudiante(estudiante);
        }
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
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'guardarReporte'");
    }

    public void actualizarEstudiante(Estudiante estudiante) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'actualizarEstudiante'");
    }

    public void agregarValoracionAContenido(String idContenido, String idEstudiante, int puntuacion,
            String comentario) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'agregarValoracionAContenido'");
    }


}