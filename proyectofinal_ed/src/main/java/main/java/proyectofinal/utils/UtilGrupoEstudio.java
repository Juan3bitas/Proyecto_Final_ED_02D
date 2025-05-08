package main.java.proyectofinal.utils;

import main.java.proyectofinal.excepciones.OperacionFallidaException;
import main.java.proyectofinal.modelo.Usuario;

import java.util.Objects;

public class UtilGrupoEstudio {
    private static UtilGrupoEstudio instancia;
    private final UtilRedSocial utilRedSocial;

    private UtilGrupoEstudio() {
        this.utilRedSocial = UtilRedSocial.getInstance();
    }

    public static synchronized UtilGrupoEstudio getInstance() {
        if (instancia == null) {
            instancia = new UtilGrupoEstudio();
        }
        return instancia;
    }

    /**
     * Agrega un miembro al grupo (persistencia + actualización de afinidad)
     * @param grupoId ID del grupo (no nulo/vacío)
     * @param idEstudiante ID del estudiante (no nulo/vacío)
     * @return true si la operación fue exitosa
     * @throws OperacionFallidaException si falla la persistencia
     * @throws IllegalArgumentException si parámetros son inválidos
     */
    public boolean agregarMiembro(String grupoId, String idEstudiante) throws OperacionFallidaException {
        validarParametros(grupoId, idEstudiante);
        return utilRedSocial.agregarMiembroAGrupo(grupoId, idEstudiante);
    }

    /**
     * Elimina un miembro del grupo
     * @param grupoId ID del grupo (no nulo/vacío)
     * @param idEstudiante ID del estudiante (no nulo/vacío)
     * @return true si la operación fue exitosa
     * @throws OperacionFallidaException si falla la persistencia
     * @throws IllegalArgumentException si parámetros son inválidos
     */
    public boolean eliminarMiembro(String grupoId, String idEstudiante) throws OperacionFallidaException {
        validarParametros(grupoId, idEstudiante);
        return utilRedSocial.eliminarMiembroDeGrupo(grupoId, idEstudiante);
    }

    /**
     * Agrega contenido compartido al grupo
     * @param grupoId ID del grupo (no nulo/vacío)
     * @param idContenido ID del contenido (no nulo/vacío)
     * @return true si la operación fue exitosa
     * @throws OperacionFallidaException si falla la persistencia
     * @throws IllegalArgumentException si parámetros son inválidos
     */
    public boolean agregarContenido(String grupoId, String idContenido) throws OperacionFallidaException {
        validarParametros(grupoId, idContenido);
        return utilRedSocial.agregarContenidoAGrupo(grupoId, idContenido);
    }

    /**
     * Busca un usuario en la red social
     * @param usuarioId ID del usuario (no nulo/vacío)
     * @return Usuario encontrado o null si no existe
     * @throws OperacionFallidaException si falla la búsqueda
     * @throws IllegalArgumentException si el ID es inválido
     */
    public Usuario buscarUsuario(String usuarioId) throws OperacionFallidaException {
        Objects.requireNonNull(usuarioId, "ID de usuario no puede ser nulo");
        if (usuarioId.trim().isEmpty()) {
            throw new IllegalArgumentException("ID de usuario no puede estar vacío");
        }
        return utilRedSocial.buscarUsuarioPorId(usuarioId);
    }

    /**
     * Valida parámetros comunes para operaciones con grupos
     * @param grupoId ID del grupo
     * @param idObjeto ID del objeto relacionado
     * @throws IllegalArgumentException si algún parámetro es inválido
     */
    private void validarParametros(String grupoId, String idObjeto) {
        Objects.requireNonNull(grupoId, "ID de grupo no puede ser nulo");
        Objects.requireNonNull(idObjeto, "ID relacionado no puede ser nulo");
        
        if (grupoId.trim().isEmpty() || idObjeto.trim().isEmpty()) {
            throw new IllegalArgumentException("IDs no pueden estar vacíos");
        }
    }
}