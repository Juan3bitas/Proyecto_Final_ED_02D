package main.java.proyectofinal.utils;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class UtilId implements Serializable{
    private static UtilId instancia;
    private static final Random random = new Random();
    private static final Set<String> idsGenerados = new HashSet<>();

// se crea la unica instancia de la clase 
    public static UtilId getInstance() {
        if (instancia == null) {
            instancia = new UtilId();
        }
        return instancia;
    }
    
// metodo para crear un identificador unico 
    public static String generarIdAleatorio() {
        String id;
        do {
            id = String.format("%04d", random.nextInt(100000)); // Genera un número entre 0000 y 99999
        } while (idsGenerados.contains(id)); // Verifica si el ID ya ha sido generado

        idsGenerados.add(id); // Agrega el nuevo ID al conjunto
        return id;
    }
}