package main.java.proyectofinal.utils;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class UtilId implements Serializable {
    private static UtilId instancia;
    private static final Random random = new Random();
    private static final Set<String> idsGenerados = new HashSet<>();
    private static final int LONGITUD_ID = 8; 
    private static final int MAX_INTENTOS = 100; 


    private UtilId() {}

    public static synchronized UtilId getInstance() {
        if (instancia == null) {
            instancia = new UtilId();
        }
        return instancia;
    }

    /**
     * Genera un ID único alfanumérico
     * @return ID único como String
     * @throws IllegalStateException si no se puede generar un ID único después de múltiples intentos
     */
    public static String generarIdAleatorio() {
        int intentos = 0;
        String id;
        
        do {
            if (intentos++ >= MAX_INTENTOS) {
                throw new IllegalStateException("No se pudo generar un ID único después de " + MAX_INTENTOS + " intentos");
            }
            
            id = String.format("%0" + LONGITUD_ID + "d", random.nextInt((int) Math.pow(10, LONGITUD_ID)));
            
        } while (idsGenerados.contains(id));

        idsGenerados.add(id);
        return id;
    }

    /**
     * Libera un ID para que pueda ser reutilizado
     * @param id El ID a liberar
     */
    public static void liberarId(String id) {
        idsGenerados.remove(id);
    }

    /**
     * Verifica si un ID ya está en uso
     * @param id El ID a verificar
     * @return true si el ID está en uso
     */
    public static boolean idEstaEnUso(String id) {
        return idsGenerados.contains(id);
    }
}