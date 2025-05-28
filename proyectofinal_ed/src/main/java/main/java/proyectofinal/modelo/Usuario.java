package main.java.proyectofinal.modelo;

/*
 * Clase abstracta que representa un usuario base en el sistema.
 * Las clases concretas (Estudiante, Moderador) deben extenderla.
 */

import java.util.Objects;

import com.google.gson.annotations.SerializedName;
import main.java.proyectofinal.utils.UtilId;

public abstract class Usuario {
    private String id;
    @SerializedName("nombres") // ✅ Indicar que el JSON "nombres" debe asignarse aquí
    private String nombre;

    @SerializedName("correo")
    private String correo;
    @SerializedName("contrasena")
    private String contrasenia;
    private boolean suspendido;
    private int diasSuspension;
    
    public Usuario(){}
    public Usuario(String id, String nombre, String correo, String contrasenia, boolean suspendido, int diasSuspension) {
        this.id = (id == null || id.isEmpty()) ? UtilId.generarIdAleatorio() : id;
        this.nombre = nombre;
        this.correo = correo;
        this.contrasenia = contrasenia;
        this.suspendido = suspendido;
        this.diasSuspension = diasSuspension;
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

    public abstract String getTipo();
}
