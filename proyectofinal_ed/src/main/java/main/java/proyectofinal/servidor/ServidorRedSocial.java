package main.java.proyectofinal.servidor;

import com.google.gson.*;
import main.java.proyectofinal.modelo.*;
import main.java.proyectofinal.utils.UtilRedSocial;

import java.io.*;
import java.net.*;
import java.util.Base64;

public class ServidorRedSocial {
    private static final int PUERTO = 12345;
    private static RedSocial redSocial = new RedSocial(new UtilRedSocial());

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PUERTO)) {
            System.out.println("✅ Servidor iniciado en puerto " + PUERTO);

            while (true) { // El servidor siempre escucha
                Socket socketCliente = serverSocket.accept();
                new Thread(() -> manejarCliente(socketCliente)).start();
            }
        } catch (IOException e) {
            System.err.println("❌ Error en servidor: " + e.getMessage());
        }
    }

    private static void manejarCliente(Socket socket) {
        try (BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter salida = new PrintWriter(socket.getOutputStream(), true)) {

            System.out.println("🔗 Cliente conectado desde: " + socket.getInetAddress());

            String mensaje;
            while ((mensaje = entrada.readLine()) != null) {
                JsonObject respuesta = procesarSolicitud(mensaje);
                salida.println(respuesta.toString());
            }

        } catch (IOException e) {
            System.err.println("⚠️ Error con cliente: " + e.getMessage());
        } finally {
            try {
                socket.close();
                System.out.println("🔌 Cliente desconectado.");
            } catch (IOException e) {
                System.err.println("Error al cerrar socket: " + e.getMessage());
            }
        }
    }

    private static JsonObject procesarSolicitud(String mensajeJson) {
        Gson gson = new Gson();
        JsonObject respuesta = new JsonObject();

        try {
            JsonObject solicitud = JsonParser.parseString(mensajeJson).getAsJsonObject();

            // ✅ Validar que el JSON tenga 'tipo' y 'datos' antes de procesarlo
            if (!solicitud.has("tipo") || !solicitud.has("datos")) {
                respuesta.addProperty("exito", false);
                respuesta.addProperty("mensaje", "Error: JSON mal formado, falta 'tipo' o 'datos'");
                return respuesta;
            }

            String tipo = solicitud.get("tipo").getAsString();
            JsonObject datos = solicitud.getAsJsonObject("datos");

            // 📥 Depuración: Verificar los datos recibidos
            System.out.println("📥 Datos recibidos en el servidor: " + datos);

            switch (tipo) {
                case "REGISTRO":
                    // ✅ Asegurar que el objeto Estudiante se deserializa correctamente
                    Estudiante estudiante = gson.fromJson(datos, Estudiante.class);
                    System.out.println("🔍 Estudiante deserializado: " + estudiante);

                    // ✅ Validar que los valores esenciales no sean nulos
                    if (estudiante.getNombre() == null || estudiante.getCorreo() == null) {
                        respuesta.addProperty("exito", false);
                        respuesta.addProperty("mensaje", "Error: Nombre y correo son obligatorios.");
                        return respuesta;
                    }
                    // ✅ Registrar el estudiante en el sistema
                    redSocial.registrarUsuario(estudiante);
                    respuesta.addProperty("exito", true);
                    respuesta.addProperty("mensaje", "Registro exitoso");
                    break;

                case "LOGIN":
                    try {
                        // Validación básica
                        if (!datos.has("correo") || !datos.has("contrasena")) {
                            throw new IllegalArgumentException("Falta correo o contraseña");
                        }

                        String correo = datos.get("correo").getAsString().trim();
                        String contrasena = datos.get("contrasena").getAsString();

                        // Validaciones adicionales
                        if (correo.isEmpty() || contrasena.isEmpty()) {
                            throw new IllegalArgumentException("Correo y contraseña son obligatorios");
                        }

                        if (!correo.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
                            throw new IllegalArgumentException("Formato de correo inválido");
                        }

                        // Autenticación
                        Usuario usuario = redSocial.iniciarSesion(correo, contrasena);

                        if (usuario == null) {
                            Thread.sleep(200); // Prevención timing attack
                            throw new SecurityException("Credenciales inválidas");
                        }

                        // Respuesta exitosa
                        respuesta.addProperty("exito", true);
                        respuesta.add("usuario", gson.toJsonTree(usuario));
                        respuesta.addProperty("token", generarToken(usuario.getId()));
                        respuesta.addProperty("mensaje", "Bienvenido " + usuario.getNombre());

                    } catch (IllegalArgumentException e) {
                        respuesta.addProperty("exito", false);
                        respuesta.addProperty("mensaje", "Error: " + e.getMessage());
                    } catch (SecurityException e) {
                        respuesta.addProperty("exito", false);
                        respuesta.addProperty("mensaje", e.getMessage());
                    } catch (Exception e) {
                        respuesta.addProperty("exito", false);
                        respuesta.addProperty("mensaje", "Error interno al procesar login");
                    }
                    break;

                default:
                    respuesta.addProperty("exito", false);
                    respuesta.addProperty("mensaje", "🚫 Operación no soportada");
            }
        } catch (Exception e) {
            respuesta.addProperty("exito", false);
            respuesta.addProperty("mensaje", "Error procesando solicitud: " + e.getMessage());
            System.err.println("❌ Error procesando solicitud: " + e.getMessage());
            e.printStackTrace(); // 📌 Imprimir el stacktrace para diagnóstico
        }

        return respuesta;
    }

    private static String generarToken(String userId) {
        // Implementación básica de generación de token
        // Para producción, usa una librería como JJWT

        long tiempoExpiracion = System.currentTimeMillis() + 3600000; // 1 hora de validez
        String datosToken = userId + "|" + tiempoExpiracion;

        // En un entorno real, deberías usar un secreto seguro y hashing
        return Base64.getEncoder().encodeToString(datosToken.getBytes());
    }
}