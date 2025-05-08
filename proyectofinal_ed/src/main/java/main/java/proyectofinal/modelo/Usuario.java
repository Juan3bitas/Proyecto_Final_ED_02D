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
    private boolean suspendido;
    private int diasSuspension;
    
    public Usuario(){}
    public Usuario(String id, String nombre, String correo, String contrasenia, boolean suspendido, int diasSuspension) {
        this.id = (id == null || id.isEmpty()) ? UtilId.generarIdAleatorio() : id;
        this.nombre = Objects.requireNonNull(nombre, "El nombre no puede ser nulo");
        this.correo = Objects.requireNonNull(correo, "El correo no puede ser nulo");
        this.contrasenia = Objects.requireNonNull(contrasenia, "La contraseña no puede ser nula");
        this.suspendido = (suspendido) ? suspendido : false; // Esto es redundante, ya que suspendido siempre será true o false.
        this.diasSuspension = (diasSuspension > 0) ? diasSuspension : 0;

    }

    //getters and setters
    public String getId() {
        return id;
    }

    public boolean isSuspendido() {
        return suspendido;
    }
    public void setSuspendido(boolean suspendido) {
        this.suspendido = suspendido;
    }
    public int getDiasSuspension() {
        return diasSuspension;
    }
    public void setDiasSuspension(int diasSuspension) {
        this.diasSuspension = diasSuspension;
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

    

    public void setId(String id) {
        this.id = id;
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
