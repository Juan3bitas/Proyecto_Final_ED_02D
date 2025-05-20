package main.java.proyectofinal.modelo;


import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import main.java.proyectofinal.excepciones.OperacionFallidaException;
import main.java.proyectofinal.utils.UtilEstudiante;

public class Estudiante extends Usuario {
    private LinkedList<String> idsContenidosPublicados;
    private List<String> intereses;
    private transient UtilEstudiante utilEstudiante;

    public void setIdsContenidosPublicados(LinkedList<String> idsContenidosPublicados) {
        this.idsContenidosPublicados = idsContenidosPublicados;
    }

    public void setUtilEstudiante(UtilEstudiante utilEstudiante) {
        this.utilEstudiante = utilEstudiante;
    }


    /*public Estudiante(){
            super();
        }*/
    public Estudiante() {}
    public Estudiante(String id, String nombre, String correo, String contrasenia, boolean suspendido, int diasSuspension, LinkedList<String> idsContenidosPublicados, List<String> intereses) {
        super(id, nombre, correo, contrasenia, suspendido, diasSuspension);
        //Si el constructor tiene en la lista contenidos significa que es cargado de persistencia los ids entonces ya tiene los contenidos, si es null que se inicialize una nueva lista, es nuevo o no tiene       
        this.idsContenidosPublicados = (idsContenidosPublicados != null) ? idsContenidosPublicados : new LinkedList<>();
        this.intereses = (intereses != null) ? intereses : new ArrayList<>();
        //this.utilEstudiante = UtilEstudiante.getInstance();
    }
    
    public void publicarCont(Contenido cont) throws OperacionFallidaException {
        //Se llama al método que lo agregará en la red social
        //luego si salió bien que agregue el id de la publicación al vendedor
        boolean exito = utilEstudiante.crearContenido(cont, this);
        if(exito){
            idsContenidosPublicados.add(cont.getId());
        }
    }

    public void eliminarCont(String idCont) throws OperacionFallidaException{
        boolean exito = false;
        //primero verificar si es propio, si lo es entonces que lo elimine, sino pues no llama a nada
        if(idsContenidosPublicados.contains(idCont)){
            exito = utilEstudiante.eliminarContenido(idCont);
        } 
        if(exito){
            idsContenidosPublicados.remove(idCont);
        }
    }

    public void modificarCont(Contenido cont) throws OperacionFallidaException{
        if(idsContenidosPublicados.contains(cont.getId())){
            utilEstudiante.modificarContenido(cont);
        }
    }

    public List<Contenido> obtenerContenidosPublicados() throws OperacionFallidaException {
        return utilEstudiante.obtenerContenidosDeEstudiante(this.getId()); 
        // Asume que UtilEstudiante tiene este método que internamente usa UtilRedSocial
    }
    public void agregarInteres(String interes) throws OperacionFallidaException {
        Objects.requireNonNull(interes, "El interés no puede ser nulo");
        if (!intereses.contains(interes)) {
            intereses.add(interes);
            // El grafo se actualiza a través de UtilEstudiante:
            utilEstudiante.actualizarIntereses(this, interes);
        }
    }
    public void solicitarAyuda(SolicitudAyuda ayuda) throws OperacionFallidaException{
        utilEstudiante.pedirAyuda(this.getId(), ayuda);
    }

    public void valorarContenido(Contenido contenido, int puntuacion, String comentario) throws OperacionFallidaException {
        if (contenido == null) throw new IllegalArgumentException("Contenido nulo");
        utilEstudiante.agregarValoracion(this.getId(), contenido.getId(), puntuacion, comentario);
        // UtilEstudiante maneja la lógica con UtilRedSocial y PersistenceManager
    }

    //getters and setters
    public LinkedList<String> getIdsContenidosPublicados(){
        return idsContenidosPublicados;
    }

    public void setidsContenidosPublicados(LinkedList<String> idsContenidosPublicados) {
        this.idsContenidosPublicados = idsContenidosPublicados;
    }

    public List<String> getIntereses(){
        return intereses;
    }

    public void setIntereses(LinkedList<String> intereses){
        this.intereses = intereses;
    }
/**
 * Agrega un ID de contenido a la lista de contenidos publicados por el estudiante
 * @param idContenido ID del contenido a agregar (no puede ser nulo o vacío)
 * @throws IllegalArgumentException si el ID es inválido
 */
public void agregarContenido(String idContenido) {
    // Validación del parámetro
    Objects.requireNonNull(idContenido, "El ID del contenido no puede ser nulo");
    if (idContenido.trim().isEmpty()) {
        throw new IllegalArgumentException("El ID del contenido no puede estar vacío");
    }

    // Verificar que no esté duplicado
    if (!idsContenidosPublicados.contains(idContenido)) {
        // Agregar a la lista local
        idsContenidosPublicados.add(idContenido);
        
        try {
            // Persistir el cambio
            utilEstudiante.actualizarContenidosPublicados(this);

        } catch (OperacionFallidaException e) {
            // Revertir el cambio si falla la persistencia
            idsContenidosPublicados.remove(idContenido);

            throw new RuntimeException("Error al actualizar contenidos del estudiante", e);
        }
    }
}
public void dejarGrupo(String grupoId) throws OperacionFallidaException {
    Objects.requireNonNull(grupoId, "El ID del grupo no puede ser nulo");
    if (grupoId.trim().isEmpty()) {
        throw new IllegalArgumentException("El ID del grupo no puede estar vacío");
    }
    
    // Delegar la operación a UtilEstudiante
    utilEstudiante.abandonarGrupo(this.getId(), grupoId);
}

public void unirseAGrupo(String grupoId) throws OperacionFallidaException {
    Objects.requireNonNull(grupoId, "El ID del grupo no puede ser nulo");
    if (grupoId.trim().isEmpty()) {
        throw new IllegalArgumentException("El ID del grupo no puede estar vacío");
    }
    
    // Verificar si el estudiante ya está en el grupo (asumiendo que UtilEstudiante tiene este método)
    if (!utilEstudiante.estaEnGrupo(this.getId(), grupoId)) {
        // Delegar la operación a UtilEstudiante
        utilEstudiante.unirEstudianteAGrupo(this.getId(), grupoId);
    } else {
        throw new OperacionFallidaException("El estudiante ya es miembro de este grupo");
    }
}

/**
 * Obtiene la lista de grupos a los que pertenece el estudiante
 * @return Lista de IDs de grupos
 * @throws OperacionFallidaException si hay error al obtener los grupos
 */
public List<String> obtenerGrupos() throws OperacionFallidaException {
    return utilEstudiante.obtenerGruposDeEstudiante(this.getId());
}

/**
 * Crea un nuevo grupo
 * @param nombreGrupo Nombre del nuevo grupo
 * @param descripcion Descripción del grupo
 * @return ID del grupo creado
 * @throws OperacionFallidaException si hay error al crear el grupo
 */
public String crearGrupo(String nombreGrupo, String descripcion) throws OperacionFallidaException {
    Objects.requireNonNull(nombreGrupo, "El nombre del grupo no puede ser nulo");
    if (nombreGrupo.trim().isEmpty()) {
        throw new IllegalArgumentException("El nombre del grupo no puede estar vacío");
    }
    
    return utilEstudiante.crearGrupo(this.getId(), nombreGrupo, descripcion);
}


    public void eliminarContenido(String id) throws OperacionFallidaException {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID del contenido no puede ser nulo o vacío");
        }
        if (!idsContenidosPublicados.contains(id)) {
            throw new IllegalArgumentException("El ID del contenido no está en la lista de contenidos publicados");
        }
        idsContenidosPublicados.remove(id);
        utilEstudiante.eliminarContenido(id);
    }
}
