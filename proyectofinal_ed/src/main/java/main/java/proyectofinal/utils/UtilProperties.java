package main.java.proyectofinal.utils;


import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class UtilProperties implements Serializable {
    private static UtilProperties instancia;
    private Properties propiedades;
    private UtilLog utilLog;

    // metodo que inicializa los objetos (propiedades leidas)
    public UtilProperties(String rutaArchivo) {
        propiedades = new Properties();
        try (FileInputStream fis = new FileInputStream(rutaArchivo)) {
            propiedades.load(fis);
        } catch (IOException e) {
            logError("Error al cargar propiedades: " + e.getMessage());
        }
    }

    // metodo para registrar mensajes de error
    private void logError(String message) {
        if (utilLog == null) {
            utilLog = UtilLog.getInstance();
        }
        utilLog.logSevere(message);
    }

    // metodo para recuperar el valor de una llave en especifico
    public String obtenerPropiedad(String llave) {
        return propiedades.getProperty(llave);
    }

    // se crea la unica instancia de la clase
    public static UtilProperties getInstance() {
        if (instancia == null) {
            String ruta = "resources/config.properties";
            instancia = new UtilProperties(ruta);
        }
        return instancia;
    }

    // metodo para obtener la lista de todas las llaves
    public List<String> getAllKeys() {
        return new ArrayList<>(propiedades.stringPropertyNames());
    }
    
}