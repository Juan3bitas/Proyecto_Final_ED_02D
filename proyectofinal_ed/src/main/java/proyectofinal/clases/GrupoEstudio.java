package proyectofinal.clases;

public class GrupoEstudio {
    private String nombre;
    private String descripcion;
    private LinkedList<Estudiante> miembros;
    private LocalDateTime fechaCreacion;

    public GrupoEstudio(String nombre, String descripcion, LocalDateTime fechaCreacion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.fechaCreacion = fechaCreacion;
        this.miembros = new LinkedList<>();
    }

    //getters and setters
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public LinkedList<Estudiante> getMiembros() {
        return miembros;
    }

    public void setMiembros(LinkedList<Estudiante> miembros) {
        this.miembros = miembros;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
}