package main.java.proyectofinal.modelo;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import main.java.proyectofinal.utils.UtilEstudiante;

public class Estudiante extends Usuario{
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

    public void obtenerContenidosPublicados(){
        //??
    }
/* 
    public void valorarContenido(){

    }

*/
    public void solicitarAyuda(){
        utilEstudiante.pedirAyuda(this.getId());
    }

    public void agregarIntereses(String interes){
        intereses.add(interes);
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
