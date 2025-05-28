package main.java.proyectofinal.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.*;

public class UtilLog {
    private static UtilLog instancia;
    private final Logger logger;
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private UtilLog() {
        this.logger = Logger.getLogger(UtilLog.class.getName());
        configurarLogger();
    }

    private void configurarLogger() {
        try {
            String ruta = "persistencia/log/redSocial.log";

            Files.createDirectories(Paths.get(ruta).getParent());

            if (!Files.exists(Paths.get(ruta))) {
                Files.createFile(Paths.get(ruta));
            }

            FileHandler fileHandler = new FileHandler(ruta, true);
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);
            logger.setUseParentHandlers(false);
            logger.setLevel(Level.ALL);

            System.out.println("✅ Logger configurado correctamente en la ruta: " + ruta);
        } catch (IOException e) {
            System.err.println("❌ ERROR CRÍTICO: No se pudo inicializar el logger.");
            e.printStackTrace();
            throw new RuntimeException("Fallo en la configuración del logger", e);
        }
    }

    public static synchronized UtilLog getInstance() {
        if (instancia == null) {
            instancia = new UtilLog();
        }
        return instancia;
    }

    public void escribirLog(String mensaje, Level nivel) {
        System.out.println("📝 Registrando en el log: " + mensaje);
        logger.log(nivel, mensaje);

        for (Handler handler : logger.getHandlers()) {
            if (handler instanceof FileHandler) {
                handler.flush();
            }
        }
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

    public void logSevere(String mensaje) { escribirLog(mensaje, Level.SEVERE); }
    public void logWarning(String mensaje) { escribirLog(mensaje, Level.WARNING); }
    public void logInfo(String mensaje) { escribirLog(mensaje, Level.INFO); }
    public void logConfig(String mensaje) { escribirLog(mensaje, Level.CONFIG); }
    public void logFine(String mensaje) { escribirLog(mensaje, Level.FINE); }
    public void logFiner(String mensaje) { escribirLog(mensaje, Level.FINER); }
    public void logFinest(String mensaje) { escribirLog(mensaje, Level.FINEST); }
}