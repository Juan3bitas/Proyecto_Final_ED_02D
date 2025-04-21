package main.java.proyectofinal.utils;


import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.*;

public class UtilLog implements Serializable {
    private static UtilLog instancia;
    private static final Logger logger = Logger.getLogger(UtilLog.class.getName());
    private UtilProperties utilProperties;

    private UtilLog() {
        this.utilProperties = UtilProperties.getInstance();
        try {
            String ruta = utilProperties.obtenerPropiedad("ruta.log");
            FileHandler fileHandler = new FileHandler(ruta, true);
            logger.addHandler(fileHandler);
            logger.setLevel(Level.ALL);
            SimpleFormatter formatter = new SimpleFormatter();
            fileHandler.setFormatter(formatter);
            logInfo("Logger configurado correctamente");
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error al configurar el logger", e);
        }
    }

    // se crea la unica intancia de la clase
    public static UtilLog getInstance() {
        if (instancia == null) {
            instancia = new UtilLog();
        }
        return instancia;
    }

    // metodo que registra un mensaje con nivel de severidad
    public void escribirLog(String mensaje, Level nivel) {
        logger.log(nivel, mensaje);
    }

    public void registrarAccion(String tipoUsuario, String accion, String interfaz) {
        // Formatear la fecha y hora actual
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        String fechaHora = LocalDateTime.now().format(formatter);

        // Crear el mensaje de log
        String mensaje = String.format("Tipo de Usuario: %s, Acción: %s, Interfaz: %s, Fecha y Hora: %s",
                tipoUsuario, accion, interfaz, fechaHora);

        // Registrar en el log
        escribirLog(mensaje, Level.INFO);
    }

    // Métodos para registrar mensajes en diferentes niveles de severidad
    public void logSevere(String mensaje) {
        escribirLog(mensaje, Level.SEVERE);
    }

    public void logWarning(String mensaje) {
        escribirLog(mensaje, Level.WARNING);
    }

    public void logInfo(String mensaje) {
        escribirLog(mensaje, Level.INFO);
    }

    public void logConfig(String mensaje) {
        escribirLog(mensaje, Level.CONFIG);
    }

    public void logFine(String mensaje) {
        escribirLog(mensaje, Level.FINE);
    }

    public void logFiner(String mensaje) {
        escribirLog(mensaje, Level.FINER);
    }

    public void logFinest(String mensaje) {
        escribirLog(mensaje, Level.FINEST);
    }
}