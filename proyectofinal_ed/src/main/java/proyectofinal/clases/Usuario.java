package main.java.proyectofinal.clases;

public class Usuario {
    private String nombre;
    private String correo;
    private String contrasenia;
    
    public Usuario(String nombre, String correo, String contrasenia) {
        this.nombre = nombre;
        this.correo = correo;
        this.contrasenia = contrasenia;
    }

    //getters and setters
    public String getNombre() {
        return this.nombre;
    }

    public void setNombre(final String nombre) {
        this.nombre = nombre;
    }

    public String getCorreo() {
        return this.correo;
    }

    public void setCorreo(final String correo) {
        this.correo = correo;
    }

    public String getContrasenia() {
        return this.contrasenia;
    }

    public void setContrasenia(final String contrasenia) {
        this.contrasenia = contrasenia;
    }
}
