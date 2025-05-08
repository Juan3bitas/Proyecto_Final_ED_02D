package main.java.proyectofinal.utils;

import java.util.List;
import main.java.proyectofinal.modelo.Contenido;
import main.java.proyectofinal.modelo.Estudiante;
import main.java.proyectofinal.modelo.GrupoEstudio;
import main.java.proyectofinal.modelo.SolicitudAyuda;
import main.java.proyectofinal.excepciones.OperacionFallidaException;
import java.util.Objects;

public class UtilEstudiante {
    private static UtilEstudiante instancia;
    private final UtilRedSocial utilRedSocial;

    private UtilEstudiante() {
        this.utilRedSocial = UtilRedSocial.getInstance();
    }

    public static synchronized UtilEstudiante getInstance() {
        if (instancia == null) {
            instancia = new UtilEstudiante();
        }
        return instancia;
    }

    
    public boolean crearContenido(Contenido cont, Estudiante estudiante) throws OperacionFallidaException {
        Objects.requireNonNull(cont, "El contenido no puede ser nulo");
        Objects.requireNonNull(estudiante, "El estudiante no puede ser nulo");
        
        // 1. Persistir el contenido
        boolean exito = utilRedSocial.guardarContenido(cont);
        
        // 2. Actualizar referencia en el estudiante (si la persistencia fue exitosa)
        if (exito) {
            utilRedSocial.agregarContenidoAEstudiante(estudiante.getId(), cont.getId());
        }
        
        return exito;
    }

    public boolean eliminarContenido(String idCont) throws OperacionFallidaException {
        if (idCont == null || idCont.isEmpty()) {
            throw new IllegalArgumentException("ID de contenido inválido");
        }
        return utilRedSocial.eliminarContenido(idCont);
    }

    public void modificarContenido(Contenido cont) throws OperacionFallidaException {
        Objects.requireNonNull(cont, "El contenido no puede ser nulo");
        utilRedSocial.actualizarContenido(cont);
    }

    public void pedirAyuda(String idEstudiante, SolicitudAyuda ayuda) throws OperacionFallidaException {
        Objects.requireNonNull(idEstudiante, "ID de estudiante inválido");
        Objects.requireNonNull(ayuda, "La solicitud no puede ser nula");
        
        ayuda.setSolicitanteId(idEstudiante);
        utilRedSocial.agregarSolicitudAyuda(ayuda);
    }

    public List<Contenido> obtenerContenidosDeEstudiante(String idEstudiante) throws OperacionFallidaException {
        if (idEstudiante == null || idEstudiante.isEmpty()) {
            throw new IllegalArgumentException("ID de estudiante inválido");
        }
        return utilRedSocial.obtenerContenidosPorEstudiante(idEstudiante);
    }

    public void actualizarIntereses(Estudiante estudiante, String interes) throws OperacionFallidaException {
        Objects.requireNonNull(estudiante, "El estudiante no puede ser nulo");
        if (interes == null || interes.isEmpty()) {
            throw new IllegalArgumentException("El interés no puede estar vacío");
        }
        
        // 1. Actualizar en memoria
        estudiante.getIntereses().add(interes);
        
        // 2. Persistir cambios del estudiante
        utilRedSocial.actualizarEstudiante(estudiante);
        
        // 3. Actualizar grafo de afinidad
        utilRedSocial.actualizarGrafoAfinidad(estudiante.getId(), interes);
    }

    public void agregarValoracion(String idEstudiante, String idContenido, int puntuacion, String comentario) 
        throws OperacionFallidaException {
        
        if (idEstudiante == null || idContenido == null) {
            throw new IllegalArgumentException("IDs no pueden ser nulos");
        }
        if (puntuacion < 1 || puntuacion > 5) {
            throw new IllegalArgumentException("La puntuación debe ser entre 1 y 5");
        }
        
        utilRedSocial.agregarValoracionAContenido(idContenido, idEstudiante, puntuacion, comentario);
    }

    /**
 * Maneja la lógica para unir un estudiante a un grupo
 */
public void unirEstudianteAGrupo(String estudianteId, String grupoId) throws OperacionFallidaException {
    try {
        // 1. Verificar que existan ambos
        Estudiante estudiante = (Estudiante) utilRedSocial.buscarUsuario(estudianteId);
        GrupoEstudio grupo = utilRedSocial.buscarGrupoPorId(grupoId);
        
        if (estudiante == null || grupo == null) {
            throw new OperacionFallidaException("Estudiante o grupo no encontrado");
        }
        
        // 2. Actualizar en RedSocial (que manejará la persistencia)
        utilRedSocial.agregarMiembroAGrupo(grupoId, estudianteId);
        
    } catch (Exception e) {
       
        throw new OperacionFallidaException("Error al unir estudiante al grupo");
    }
}

/**
 * Maneja la lógica para remover un estudiante de un grupo
 */
public void removerEstudianteDeGrupo(String estudianteId, String grupoId) throws OperacionFallidaException {
    try {
        // 1. Verificar que existan ambos
        Estudiante estudiante = (Estudiante) utilRedSocial.buscarUsuario(estudianteId);
        GrupoEstudio grupo = utilRedSocial.buscarGrupoPorId(grupoId);
        
        if (estudiante == null || grupo == null) {
            throw new OperacionFallidaException("Estudiante o grupo no encontrado");
        }
        
        // 2. Actualizar en RedSocial (que manejará la persistencia)
        utilRedSocial.eliminarMiembroDeGrupo(grupoId, estudianteId);
        
    } catch (Exception e) {
        throw new OperacionFallidaException("Error al remover estudiante del grupo", grupoId, e);
    }
}

    public void actualizarContenidosPublicados(Estudiante estudiante) throws OperacionFallidaException {
        try {
            utilRedSocial.actualizarEstudiante(estudiante);
        } catch (Exception e) {
            throw new OperacionFallidaException("Error al actualizar contenidos del estudiante");
        }
    }

    public void abandonarGrupo(String estudianteId, String grupoId) throws OperacionFallidaException {
        try {
            Objects.requireNonNull(estudianteId, "ID de estudiante no puede ser nulo");
            Objects.requireNonNull(grupoId, "ID de grupo no puede ser nulo");
            
            // 1. Verificar que el estudiante está en el grupo
            if (!estaEnGrupo(estudianteId, grupoId)) {
                throw new OperacionFallidaException("El estudiante no es miembro de este grupo");
            }
            
            // 2. Remover al estudiante del grupo
            removerEstudianteDeGrupo(estudianteId, grupoId);
            
        } catch (Exception e) {
            throw new OperacionFallidaException("Error al abandonar el grupo: " + e.getMessage(), grupoId, e);
        }
    }
    
    public boolean estaEnGrupo(String estudianteId, String grupoId) {
        Objects.requireNonNull(estudianteId, "ID de estudiante no puede ser nulo");
        Objects.requireNonNull(grupoId, "ID de grupo no puede ser nulo");
        
        try {
            // Obtener la lista de grupos del estudiante y verificar si contiene el grupoId
            List<String> grupos = obtenerGruposDeEstudiante(estudianteId);
            return grupos.contains(grupoId);
        } catch (Exception e) {
            return false;
        }
    }
    
    public List<String> obtenerGruposDeEstudiante(String estudianteId) throws OperacionFallidaException {
        Objects.requireNonNull(estudianteId, "ID de estudiante no puede ser nulo");
        
        try {
            // Delegar la operación a UtilRedSocial
            return utilRedSocial.obtenerGruposDeEstudiante(estudianteId);
        } catch (Exception e) {
            throw new OperacionFallidaException("Error al obtener grupos del estudiante", estudianteId, e);
        }
    }
    
    public String crearGrupo(String creadorId, String nombreGrupo, String descripcion) throws OperacionFallidaException {
        Objects.requireNonNull(creadorId, "ID de creador no puede ser nulo");
        Objects.requireNonNull(nombreGrupo, "Nombre de grupo no puede ser nulo");
        
        if (nombreGrupo.trim().isEmpty()) {
            throw new IllegalArgumentException("Nombre de grupo no puede estar vacío");
        }
        
        try {
            // 1. Crear el grupo a través de UtilRedSocial
            GrupoEstudio nuevoGrupo = new GrupoEstudio(
                null, 
                nombreGrupo,
                descripcion,
                null, null, null
            );
            
            // 2. Persistir el grupo
            utilRedSocial.guardarGrupo(nuevoGrupo);
            
            // 3. Agregar al creador como miembro
            utilRedSocial.agregarMiembroAGrupo(nuevoGrupo.getIdGrupo(), creadorId);
            
            return nuevoGrupo.getIdGrupo();
            
        } catch (Exception e) {
            throw new OperacionFallidaException("Error al crear grupo: " + e.getMessage(), creadorId, e);
        }
    }
}