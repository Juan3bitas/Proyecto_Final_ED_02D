package main.java.proyectofinal.utils;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class UtilProperties implements Serializable {
    private static final long serialVersionUID = 1L;
    private static UtilProperties instancia;
    private final Properties propiedades;
    private static final String RUTA_DEFAULT = "resources/config.properties";

    private UtilProperties(String rutaArchivo) {
        this.propiedades = new Properties();
        cargarPropiedades(rutaArchivo);
    }

    private void cargarPropiedades(String rutaArchivo) {
        try {
            validarArchivo(rutaArchivo);
            try (InputStream input = new FileInputStream(rutaArchivo)) {
                propiedades.load(input);
                System.out.println("Propiedades cargadas correctamente desde: " + rutaArchivo);
            }
        } catch (IOException e) {
            System.err.println("Error crítico al cargar el archivo de propiedades: " + e.getMessage());
            throw new RuntimeException("No se pudo cargar el archivo de propiedades", e);
        }
    }

    private void validarArchivo(String ruta) throws IOException {
        Path path = Paths.get(ruta);
        if (!Files.exists(path)) {
            throw new IOException("Archivo de configuración no encontrado: " + ruta);
        }
        if (!ruta.endsWith(".properties")) {
            throw new IOException("El archivo debe tener extensión .properties");
        }
        if (!Files.isReadable(path)) {
            throw new IOException("No se tiene permisos de lectura para el archivo: " + ruta);
        }
    }

    public String obtenerPropiedad(String llave) {
        if (llave == null || llave.trim().isEmpty()) {
            System.err.println("Advertencia: Se solicitó una propiedad con llave vacía o inválida.");
            return null;
        }
        String valor = propiedades.getProperty(llave);
        if (valor == null) {
            System.err.println("Advertencia: No se encontró la propiedad '" + llave + "' en el archivo.");
        }
        return valor;
    }

    public static synchronized UtilProperties getInstance() {
        if (instancia == null) {
            instancia = new UtilProperties(RUTA_DEFAULT);
        }
        return instancia;
    }

    protected static void resetInstance() {
        instancia = null;
    }
}