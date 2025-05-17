package main.java.proyectofinal.servidor;

import com.google.gson.*;
import main.java.proyectofinal.modelo.*;
import main.java.proyectofinal.utils.UtilRedSocial;

import java.io.*;
import java.net.*;
import java.util.Base64;
import java.util.List;

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
                case "OBTENER_CONTENIDOS":
                    try {
                        String userId = solicitud.get("userId").getAsString();
                        List<Contenido> contenidos = redSocial.obtenerContenidosPorEstudiante(userId);

                        JsonArray contenidosJson = new JsonArray();
                        for (Contenido contenido : contenidos) {
                            JsonObject contenidoJson = new JsonObject();
                            contenidoJson.addProperty("id", contenido.getId());
                            contenidoJson.addProperty("titulo", contenido.getTitulo());
                            contenidoJson.addProperty("autor", contenido.getAutor());
                            contenidoJson.addProperty("tema", contenido.getTema());
                            contenidoJson.addProperty("descripcion", contenido.getDescripcion());
                            contenidoJson.addProperty("tipo", contenido.getTipo().toString());
                            contenidosJson.add(contenidoJson);
                        }

                        respuesta.addProperty("exito", true);
                        respuesta.add("contenidos", contenidosJson);
                    } catch (Exception e) {
                        respuesta.addProperty("exito", false);
                        respuesta.addProperty("mensaje", "Error al obtener contenidos: " + e.getMessage());
                    }
                    break;

                case "OBTENER_SOLICITUDES":
                    try {
                        String userId = solicitud.get("userId").getAsString();
                        List<SolicitudAyuda> solicitudes = redSocial.obtenerSolicitudes();

                        JsonArray solicitudesJson = new JsonArray();
                        for (SolicitudAyuda solicitudAyuda : solicitudes) {
                            JsonObject solicitudJson = new JsonObject();
                            solicitudJson.addProperty("id", solicitudAyuda.getId());
                            solicitudJson.addProperty("tema", solicitudAyuda.getTema());
                            solicitudJson.addProperty("descripcion", solicitudAyuda.getDescripcion());
                            solicitudJson.addProperty("fecha", solicitudAyuda.getFecha().getTime()); // Convertir Date a timestamp
                            solicitudJson.addProperty("urgencia", solicitudAyuda.getUrgencia().toString());
                            solicitudJson.addProperty("solicitanteId", solicitudAyuda.getSolicitanteId());
                            solicitudJson.addProperty("estado", solicitudAyuda.getEstado().toString());

                            // Opcional: Agregar nombre del solicitante si está disponible
                            Usuario solicitante = redSocial.buscarUsuario(solicitudAyuda.getSolicitanteId());
                            if (solicitante != null) {
                                solicitudJson.addProperty("solicitanteNombre", solicitante.getNombre());
                            }

                            solicitudesJson.add(solicitudJson);
                        }

                        respuesta.addProperty("exito", true);
                        respuesta.add("solicitudes", solicitudesJson);
                    } catch (Exception e) {
                        respuesta.addProperty("exito", false);
                        respuesta.addProperty("mensaje", "Error al obtener solicitudes: " + e.getMessage());
                    }
                    break;
                case "ACTUALIZAR_USUARIO":
                    try {
                        // Validar campos obligatorios
                        if (!datos.has("id")) {
                            throw new IllegalArgumentException("Falta el ID del usuario");
                        }

                        // Obtener datos del usuario
                        String id = datos.get("id").getAsString();
                        String nombre = datos.has("nombre") ? datos.get("nombre").getAsString() : null;
                        String email = datos.has("email") ? datos.get("email").getAsString() : null;
                        String password = datos.has("password") ? datos.get("password").getAsString() : null;

                        // Validar que al menos un campo se esté actualizando
                        if (nombre == null && email == null && password == null) {
                            throw new IllegalArgumentException("Debe proporcionar al menos un campo para actualizar");
                        }

                        // Buscar usuario existente
                        Usuario usuario = redSocial.buscarUsuario(id);
                        if (usuario == null) {
                            throw new IllegalArgumentException("Usuario no encontrado");
                        }

                        // Actualizar campos (solo los proporcionados)
                        if (nombre != null) usuario.setNombre(nombre);
                        if (email != null) usuario.setCorreo(email);
                        if (password != null) usuario.setContrasenia(password);

                        // Guardar cambios
                        boolean exito = redSocial.actualizarUsuario(usuario);

                        if (exito) {
                            respuesta.addProperty("exito", true);
                            respuesta.addProperty("mensaje", "Datos actualizados correctamente");
                            respuesta.add("usuario", gson.toJsonTree(usuario));
                        } else {
                            respuesta.addProperty("exito", false);
                            respuesta.addProperty("mensaje", "No se pudo actualizar el usuario");
                        }

                    } catch (IllegalArgumentException e) {
                        respuesta.addProperty("exito", false);
                        respuesta.addProperty("mensaje", "Error: " + e.getMessage());
                    } catch (Exception e) {
                        respuesta.addProperty("exito", false);
                        respuesta.addProperty("mensaje", "Error interno al actualizar usuario");
                    }
                    break;

                case "ELIMINAR_USUARIO":
                    try {
                        // Validar campos obligatorios
                        if (!datos.has("usuarioId") && !datos.has("id")) {
                            throw new IllegalArgumentException("Falta el ID del usuario");
                        }
                        String usuarioId = datos.has("usuarioId") ? datos.get("usuarioId").getAsString() : datos.get("id").getAsString();


                        // Verificar que el usuario existe
                        Usuario usuario = redSocial.buscarUsuario(usuarioId);
                        if (usuario == null) {
                            throw new IllegalArgumentException("Usuario no encontrado");
                        }

                        // Eliminar usuario
                        boolean exito = redSocial.eliminarUsuario(usuarioId);

                        if (exito) {
                            respuesta.addProperty("exito", true);
                            respuesta.addProperty("mensaje", "Cuenta eliminada correctamente");
                        } else {
                            respuesta.addProperty("exito", false);
                            respuesta.addProperty("mensaje", "No se pudo eliminar el usuario");
                        }

                    } catch (IllegalArgumentException e) {
                        respuesta.addProperty("exito", false);
                        respuesta.addProperty("mensaje", "Error: " + e.getMessage());
                    } catch (Exception e) {
                        respuesta.addProperty("exito", false);
                        respuesta.addProperty("mensaje", "Error interno al eliminar usuario");
                    }
                    break;
                case "OBTENER_INFO_USUARIO":
                    try {
                        if (!datos.has("id")) {
                            throw new IllegalArgumentException("Falta el ID del usuario");
                        }
                        String idUsuario = datos.get("id").getAsString();

                        Usuario usuario = redSocial.buscarUsuario(idUsuario);
                        if (usuario == null) {
                            throw new IllegalArgumentException("Usuario no encontrado");
                        }

                        JsonObject usuarioJson = new JsonObject();
                        usuarioJson.addProperty("id", usuario.getId());
                        usuarioJson.addProperty("nombre", usuario.getNombre());
                        usuarioJson.addProperty("email", usuario.getCorreo());  // aquí usas "email"

                        respuesta.addProperty("exito", true);
                        respuesta.add("usuario", usuarioJson);

                    } catch (IllegalArgumentException e) {
                        respuesta.addProperty("exito", false);
                        respuesta.addProperty("mensaje", "Error: " + e.getMessage());
                    } catch (Exception e) {
                        respuesta.addProperty("exito", false);
                        respuesta.addProperty("mensaje", "Error interno al obtener info usuario");
                    }
                    break;

                case "OBTENER_USUARIO":
                    try {
                        if (!datos.has("id")) {
                            throw new IllegalArgumentException("Falta el ID del usuario");
                        }
                        String idUsuario = datos.get("id").getAsString();

                        Usuario usuario = redSocial.buscarUsuario(idUsuario);
                        if (usuario == null) {
                            throw new IllegalArgumentException("Usuario no encontrado");
                        }

                        JsonObject usuarioJson = new JsonObject();
                        usuarioJson.addProperty("id", usuario.getId());
                        usuarioJson.addProperty("nombre", usuario.getNombre());
                        usuarioJson.addProperty("email", usuario.getCorreo());  // "email" igual aquí

                        respuesta.addProperty("exito", true);
                        respuesta.add("usuario", usuarioJson);

                    } catch (IllegalArgumentException e) {
                        respuesta.addProperty("exito", false);
                        respuesta.addProperty("mensaje", "Error: " + e.getMessage());
                    } catch (Exception e) {
                        respuesta.addProperty("exito", false);
                        respuesta.addProperty("mensaje", "Error interno al obtener info usuario");
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