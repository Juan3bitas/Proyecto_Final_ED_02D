package proyectofinal.clases;

public class Contenido {
    private String titulo;
    private String autor;
    private LocalDateTime fecha;
    private String tipo;
    private String tema;
    private LinkedList<String> valoraciones;

    public Contenido(String titulo, String autor, LocalDateTime fecha, String tipo, String tema) {
        this.titulo = titulo;
        this.autor = autor;
        this.fecha = fecha;
        this.tipo = tipo;
        this.tema = tema;
        this.valoraciones = new LinkedList<>();
    }

    //getters and setters
    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getTema() {
        return tema;
    }

    public void setTema(String tema) {
        this.tema = tema;
    }

    public LinkedList<String> getValoraciones() {
        return valoraciones;
    }

    public void setValoraciones(LinkedList<String> valoraciones) {
        this.valoraciones = valoraciones;
    }
}