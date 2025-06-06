package main.java.proyectofinal.modelo;


import java.util.*;

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

    }
    public void agregarInteres(String interes) throws OperacionFallidaException {
        Objects.requireNonNull(interes, "El interés no puede ser nulo");
        if (!intereses.contains(interes)) {
            intereses.add(interes);

            utilEstudiante.actualizarIntereses(this, interes);
        }
    }
    public void solicitarAyuda(SolicitudAyuda ayuda) throws OperacionFallidaException{
        utilEstudiante.pedirAyuda(this.getId(), ayuda);
    }

    public void valorarContenido(Contenido contenido, int puntuacion, String comentario) throws OperacionFallidaException {
        if (contenido == null) throw new IllegalArgumentException("Contenido nulo");
        utilEstudiante.agregarValoracion(this.getId(), contenido.getId(), puntuacion, comentario);

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


    if (!idsContenidosPublicados.contains(idContenido)) {

        idsContenidosPublicados.add(idContenido);
        
        try {

            utilEstudiante.actualizarContenidosPublicados(this);

        } catch (OperacionFallidaException e) {

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
    

    utilEstudiante.abandonarGrupo(this.getId(), grupoId);
}

    /**
     * Agrega al estudiante a un grupo de estudio
     * @param grupoId ID del grupo al que se quiere unir
     * @throws OperacionFallidaException Si ocurre un error al unirse al grupo
     */
    public void unirseAGrupo(String grupoId) throws OperacionFallidaException {
        Objects.requireNonNull(grupoId, "El ID del grupo no puede ser nulo");
        grupoId = grupoId.trim();
        if (grupoId.isEmpty()) {
            throw new IllegalArgumentException("El ID del grupo no puede estar vacío");
        }

        if (utilEstudiante.estaEnGrupo(this.getId(), grupoId)) {

            throw new MiembroExistenteException(this.getId(), grupoId);
        }

        utilEstudiante.unirEstudianteAGrupo(this.getId(), grupoId);
    }

    public String getNumeroContenidosPublicados() {
        if (idsContenidosPublicados == null) {
            return "0";
        }
        return String.valueOf(idsContenidosPublicados.size());
    }


    public class MiembroExistenteException extends OperacionFallidaException {
        public MiembroExistenteException(String idEstudiante, String idGrupo) {
            super(String.format("El estudiante %s ya es miembro del grupo %s",
                    idEstudiante, idGrupo));
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

    /**
     * Obtiene los grupos de estudio del estudiante
     * @return Conjunto de identificadores de grupos de estudio
     */
    public Set<String> getGruposEstudio() {
        try {
            return new HashSet<>(utilEstudiante.obtenerGruposDeEstudiante(this.getId()));
        } catch (OperacionFallidaException e) {
            System.out.println("Error al obtener grupos de estudio: " + e.getMessage());
            return Collections.emptySet(); // Devuelve conjunto vacío en caso de error
        }
    }

    public Map<String, Double> getValoracionesContenidos() {
        Map<String, Double> valoraciones = new HashMap<>();
        try {
            List<Valoracion> valoracionesList = utilEstudiante.obtenerValoracionesDeEstudiante(this.getId());
            for (Valoracion valoracion : valoracionesList) {
                valoraciones.put(valoracion.getIdContenido(), valoracion.getPuntuacion());
            }
        } catch (OperacionFallidaException e) {
            System.out.println("Error al obtener valoraciones: " + e.getMessage());
        }
        return valoraciones;
    }

    @Override
    public String getTipo() {
        return "ESTUDIANTE";
    }

    public Collection<Object> getGrupos() {
        try {
            return Collections.singleton(utilEstudiante.obtenerGruposDeEstudiante(this.getId()));
        } catch (OperacionFallidaException e) {
            System.out.println("Error al obtener grupos: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public void agregarGrupo(String idGrupo) throws OperacionFallidaException {
        Objects.requireNonNull(idGrupo, "El ID del grupo no puede ser nulo");
        if (idGrupo.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID del grupo no puede estar vacío");
        }

        if (!utilEstudiante.estaEnGrupo(this.getId(), idGrupo)) {
            utilEstudiante.unirEstudianteAGrupo(this.getId(), idGrupo);
        } else {
            System.out.println("El estudiante ya es miembro de este grupo");
        }
    }
}
