package main.java.proyectofinal.modelo;

/**
 * Clase abstracta que representa un usuario base en el sistema.
 * Las clases concretas (Estudiante, Moderador) deben extenderla.
 */

import java.util.Objects;

import main.java.proyectofinal.utils.UtilId;

public abstract class Usuario {
    private String id;
    private String nombre;
    private String correo;
    private String contrasenia;
    
    public Usuario(){}
    public Usuario(String id, String nombre, String correo, String contrasenia) {
        this.id = (id == null || id.isEmpty()) ? UtilId.generarIdAleatorio() : id;
    this.nombre = Objects.requireNonNull(nombre, "El nombre no puede ser nulo");
    this.correo = Objects.requireNonNull(correo, "El correo no puede ser nulo");
    this.contrasenia = Objects.requireNonNull(contrasenia, "La contraseña no puede ser nula");
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Usuario usuario = (Usuario) o;
        return id.equals(usuario.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format(
            "Usuario [ID: %s, Nombre: %s, Correo: %s]",
            id, nombre, correo
        );
    }
}
