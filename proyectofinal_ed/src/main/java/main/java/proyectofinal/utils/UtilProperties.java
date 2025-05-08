package main.java.proyectofinal.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Utilidad para manejar archivos de propiedades de configuración.
 * Implementa Singleton y carga las propiedades desde un archivo config.properties.
 */
public class UtilProperties implements Serializable {
    private static final long serialVersionUID = 1L;
    private static UtilProperties instancia;
    private final Properties propiedades;
    private transient UtilLog utilLog; // Transient para serialización

    private UtilProperties(String rutaArchivo) {
        this.propiedades = new Properties();
        cargarPropiedades(rutaArchivo);
    }

    private void cargarPropiedades(String rutaArchivo) {
        try {
            validarArchivo(rutaArchivo);
            try (FileInputStream fis = new FileInputStream(rutaArchivo)) {
                propiedades.load(fis);
                getLogger().logInfo("Propiedades cargadas desde: " + rutaArchivo);
            }
        } catch (IOException e) {
            getLogger().logSevere("Error al cargar propiedades: " + e.getMessage());
            throw new RuntimeException("No se pudo cargar el archivo de propiedades", e);
        }
    }

    private void validarArchivo(String ruta) throws IOException {
        if (!Files.exists(Paths.get(ruta))) {
            throw new IOException("Archivo no encontrado: " + ruta);
        }
        if (!ruta.endsWith(".properties")) {
            throw new IOException("El archivo debe ser .properties");
        }
    }

    private UtilLog getLogger() {
        if (utilLog == null) {
            utilLog = UtilLog.getInstance();
        }
        return utilLog;
    }

    /**
     * Obtiene el valor de una propiedad
     * @param llave Clave de la propiedad
     * @return Valor de la propiedad o null si no existe
     */
    public String obtenerPropiedad(String llave) {
        if (llave == null || llave.trim().isEmpty()) {
            getLogger().logWarning("Se solicitó propiedad con llave vacía");
            return null;
        }
        return propiedades.getProperty(llave);
    }

    /**
     * Obtiene todas las claves de propiedades disponibles
     * @return Lista de claves
     */
    public List<String> getAllKeys() {
        return new ArrayList<>(propiedades.stringPropertyNames());
    }

    /**
     * Singleton thread-safe
     */
    public static synchronized UtilProperties getInstance() {
        if (instancia == null) {
            String ruta = "resources/config.properties"; // Ruta por defecto
            instancia = new UtilProperties(ruta);
        }
        return instancia;
    }

    /**
     * Método para testing - Reinicia la instancia singleton
     */
    protected static void resetInstance() {
        instancia = null;
    }
}