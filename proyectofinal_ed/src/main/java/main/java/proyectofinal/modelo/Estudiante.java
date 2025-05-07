package main.java.proyectofinal.modelo;


import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import main.java.proyectofinal.utils.UtilEstudiante;

public class Estudiante extends Usuario {
    private LinkedList<String> idsContenidosPublicados;
    private List<String> intereses;
    private UtilEstudiante utilEstudiante;

    public Estudiante(){
        super();
    }
    public Estudiante(String id, String nombre, String correo, String contrasenia, LinkedList<String> idsContenidosPublicados, List<String> intereses) {
        super(id, nombre, correo, contrasenia);
        //Si el constructor tiene en la lista contenidos significa que es cargado de persistencia los ids entonces ya tiene los contenidos, si es null que se inicialize una nueva lista, es nuevo o no tiene       
        this.idsContenidosPublicados = (idsContenidosPublicados != null) ? idsContenidosPublicados : new LinkedList<>();
        //Lo mismo para este
        this.intereses = (intereses != null) ? intereses : new ArrayList<>();
        this.utilEstudiante = UtilEstudiante.getInstance();
    }
    
    public void publicarCont(Contenido cont) {
        //Se llama al método que lo agregará en la red social
        //luego si salió bien que agregue el id de la publicación al vendedor
        boolean exito = utilEstudiante.crearContenido(cont, this);
        if(exito){
            idsContenidosPublicados.add(cont.getId());
        }
    }

    public void eliminarCont(String idCont){
        boolean exito = false;
        //primero verificar si es propio, si lo es entonces que lo elimine, sino pues no llama a nada
        if(idsContenidosPublicados.contains(idCont)){
            exito = utilEstudiante.eliminarContenido(idCont);
        } 
        if(exito){
            idsContenidosPublicados.remove(idCont);
        }
    }

    public void modificarCont(Contenido cont){
        if(idsContenidosPublicados.contains(cont.getId())){
            utilEstudiante.modificarContenido(cont);
        }
    }

    public List<Contenido> obtenerContenidosPublicados() {
        return utilEstudiante.obtenerContenidosDeEstudiante(this.getId()); 
        // Asume que UtilEstudiante tiene este método que internamente usa UtilRedSocial
    }
    public void agregarInteres(String interes) {
        Objects.requireNonNull(interes, "El interés no puede ser nulo");
        if (!intereses.contains(interes)) {
            intereses.add(interes);
            // El grafo se actualiza a través de UtilEstudiante:
            utilEstudiante.actualizarIntereses(this, interes);
        }
    }
    public void solicitarAyuda(SolicitudAyuda ayuda){
        utilEstudiante.pedirAyuda(this.getId(), ayuda);
    }

    public void valorarContenido(Contenido contenido, int puntuacion, String comentario) {
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
}
