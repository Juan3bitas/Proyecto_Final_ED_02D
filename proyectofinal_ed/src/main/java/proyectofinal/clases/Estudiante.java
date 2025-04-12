package proyectofinal.clases;

public class Estudiante extends Usuario{
    private LinkedList<Contenido> contenidosPublicados;
    private LinkedList<String> intereses;

    public Estudiante(String nombre, String correo, String contrasenia) {

        super(nombre, correo, contrasenia);
        this.contenidosPublicados = new LinkedList<>();
        this.intereses = new LinkedList<>();
    }
    
    public void publicarContenido() {
        
    }

    public void agregarContenido(Contenido cont){
        contenidosPublicados.add(cont)
    }
    public void agregarIntereses(String interes) {
        intereses.add(interes);
    }

    //getters and setters
    public LinkedList<Contenido> getContenidosPublicados() {
        return contenidosPublicados;
    }

    public void setContenidosPublicados(LinkedList<Contenido> contenidosPublicados) {
        this.contenidosPublicados = contenidosPublicados;
    }

    public LinkedList<String> getIntereses() {
        return intereses;
    }

    public void setIntereses(LinkedList<String> intereses) {
        this.intereses = intereses;
    }
}
