package main.java.proyectofinal.utils;

import java.io.IOException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.*;

public class UtilLog{
    private static UtilLog instancia;
    private final Logger logger;  
    private final UtilProperties utilProperties;
    private static final DateTimeFormatter DATE_FORMATTER = 
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"); 

    private UtilLog() {
        this.utilProperties = UtilProperties.getInstance();
        this.logger = Logger.getLogger(UtilLog.class.getName());
        configurarLogger();
    }

    private void configurarLogger() {
        try {
            String ruta = utilProperties.obtenerPropiedad("ruta.log");
            FileHandler fileHandler = new FileHandler(ruta, true);
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);
            logger.setUseParentHandlers(false);  
            logger.setLevel(Level.ALL);
            logInfo("Logger configurado correctamente. Ruta: " + ruta);
        } catch (IOException e) {
            System.err.println("ERROR CRÍTICO: No se pudo inicializar el logger. " + e.getMessage());
            throw new RuntimeException("Fallo en la configuración del logger", e);  // Fail-fast
        }
    }

   
    public static synchronized UtilLog getInstance() {
        if (instancia == null) {
            instancia = new UtilLog();
        }
        return instancia;
    }

    void escribirLog(String mensaje, Level nivel) {
        logger.log(nivel, mensaje);
    }

    public void registrarAccion(String tipoUsuario, String accion, String interfaz) {
        String mensaje = String.format(
            "[%s] Tipo: %s | Acción: %s | Interfaz: %s",
            LocalDateTime.now().format(DATE_FORMATTER),
            tipoUsuario,
            accion,
            interfaz
        );
        escribirLog(mensaje, Level.INFO);
    }


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