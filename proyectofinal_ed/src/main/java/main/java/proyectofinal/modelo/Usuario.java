package main.java.proyectofinal.modelo;

import main.java.proyectofinal.utils.UtilId;

public abstract class Usuario {
    private String id;
    private String nombre;
    private String correo;
    private String contrasenia;
    
    public Usuario(){}
    public Usuario(String id, String nombre, String correo, String contrasenia) {
        this.id = (id == null || id.isEmpty()) ? UtilId.generarIdAleatorio() : id;
        this.nombre = nombre;
        this.correo = correo;
        this.contrasenia = contrasenia;
        UtilId.getInstance();
    }

    //getters and setters
    public String getId() {
        return id;
    }

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
