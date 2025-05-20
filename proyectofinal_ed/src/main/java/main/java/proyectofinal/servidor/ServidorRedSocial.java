package main.java.proyectofinal.servidor;

import com.google.gson.*;
import main.java.proyectofinal.modelo.*;
import main.java.proyectofinal.utils.UtilRedSocial;

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

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
                        // Obtener todos los contenidos sin filtrar por usuario
                        List<Contenido> contenidos = redSocial.obtenerTodosContenidos();

                        JsonArray contenidosJson = new JsonArray();
                        Gson gsonContenidos = new GsonBuilder()
                                .setDateFormat("yyyy-MM-dd HH:mm:ss")
                                .create();

                        for (Contenido contenido : contenidos) {
                            JsonObject contenidoJson = new JsonObject();
                            contenidoJson.addProperty("id", contenido.getId());
                            contenidoJson.addProperty("titulo", contenido.getTitulo());
                            contenidoJson.addProperty("autor", contenido.getAutor());
                            contenidoJson.addProperty("tema", contenido.getTema());
                            contenidoJson.addProperty("descripcion", contenido.getDescripcion());
                            contenidoJson.addProperty("tipo", contenido.getTipo().name());
                            contenidoJson.addProperty("fechaCreacion",
                                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(contenido.getFecha()));
                            contenidoJson.addProperty("contenido", contenido.getContenido());

                            // Valoraciones
                            JsonArray valoracionesJson = new JsonArray();
                            for (Valoracion valoracion : contenido.getValoraciones()) {
                                JsonObject v = new JsonObject();
                                v.addProperty("usuarioId", valoracion.getUsuarioId());
                                v.addProperty("valor", valoracion.getValor());
                                v.addProperty("comentario", valoracion.getComentario());
                                valoracionesJson.add(v);
                            }
                            contenidoJson.add("valoraciones", valoracionesJson);

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
                        List<SolicitudAyuda> solicitudes = redSocial.obtenerTodasSolicitudes();
                        JsonArray jsonSolicitudes = new JsonArray();

                        for (SolicitudAyuda solicitud1 : solicitudes) {
                            JsonObject jsonSolicitud = new JsonObject();
                            jsonSolicitud.addProperty("id", solicitud1.getId());
                            jsonSolicitud.addProperty("tema", solicitud1.getTema());
                            jsonSolicitud.addProperty("descripcion", solicitud1.getDescripcion());
                            jsonSolicitud.addProperty("fecha",
                                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(solicitud1.getFecha()));
                            jsonSolicitud.addProperty("urgencia", solicitud1.getUrgencia().name());
                            jsonSolicitud.addProperty("estado", solicitud1.getEstado().name());
                            jsonSolicitud.addProperty("solicitanteId", solicitud1.getSolicitanteId());

                            // Obtener nombre del solicitante
                            Usuario solicitante = redSocial.buscarUsuario(solicitud1.getSolicitanteId());
                            jsonSolicitud.addProperty("solicitanteNombre",
                                    solicitante != null ? solicitante.getNombre() : "Desconocido");

                            jsonSolicitudes.add(jsonSolicitud);
                        }

                        respuesta.addProperty("exito", true);
                        respuesta.add("solicitudes", jsonSolicitudes);

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
                case "OBTENER_DATOS_MODERADOR":
                    try {
                        if (!solicitud.has("userId")) {
                            throw new IllegalArgumentException("Falta el ID del usuario");
                        }
                        String userId = solicitud.get("userId").getAsString();

                        Usuario usuario = redSocial.buscarUsuario(userId);
                        if (usuario == null) {
                            throw new IllegalArgumentException("Usuario no encontrado");
                        }

                        // Verificar que el usuario es moderador
                        if (!(usuario instanceof Moderador)) {
                            throw new SecurityException("El usuario no tiene permisos de moderador");
                        }

                        // Construir respuesta con datos completos del moderador
                        JsonObject datosModerador = new JsonObject();
                        datosModerador.addProperty("id", usuario.getId());
                        datosModerador.addProperty("nombres", usuario.getNombre());
                        datosModerador.addProperty("correo", usuario.getCorreo());

                        // Agregar estadísticas específicas para moderador
                        datosModerador.addProperty("totalUsuarios", redSocial.obtenerTotalUsuarios());
                        datosModerador.addProperty("totalContenidos", redSocial.obtenerTotalContenidos());
                        datosModerador.addProperty("totalSolicitudes", redSocial.obtenerTotalSolicitudes());

                        respuesta.addProperty("exito", true);
                        respuesta.add("datosUsuario", datosModerador);

                    } catch (IllegalArgumentException | SecurityException e) {
                        respuesta.addProperty("exito", false);
                        respuesta.addProperty("mensaje", e.getMessage());
                    } catch (Exception e) {
                        respuesta.addProperty("exito", false);
                        respuesta.addProperty("mensaje", "Error interno al obtener datos de moderador");
                    }
                    break;

                case "OBTENER_DATOS_PERFIL":
                    try {
                        if (datos == null || !datos.has("userId") || datos.get("userId").getAsString().isEmpty()) {
                            throw new IllegalArgumentException("Falta el ID del usuario");
                        }

                        String userId = datos.get("userId").getAsString();

                        Usuario usuario = redSocial.buscarUsuario(userId);
                        if (usuario == null) {
                            throw new IllegalArgumentException("Usuario no encontrado");
                        }

                        // Construir respuesta con datos completos del perfil
                        JsonObject datosPerfil = new JsonObject();
                        datosPerfil.addProperty("id", usuario.getId());
                        datosPerfil.addProperty("nombres", usuario.getNombre());
                        datosPerfil.addProperty("correo", usuario.getCorreo());

                        if (usuario instanceof Estudiante) {
                            Estudiante estudiante1 = (Estudiante) usuario;
                            List<String> intereses = estudiante1.getIntereses();
                            String interesesStr = String.join(", ", intereses);
                            datosPerfil.addProperty("intereses", interesesStr);
                        } else {
                            datosPerfil.addProperty("intereses", "");
                        }

                        // Obtener grupos de estudio del usuario
                        JsonArray gruposArray = new JsonArray();
                        List<String> grupos = redSocial.obtenerGruposEstudio(userId);
                        for (String grupo : grupos) {
                            gruposArray.add(grupo);
                        }
                        datosPerfil.add("gruposEstudio", gruposArray);

                        // Obtener estadísticas del usuario
                        datosPerfil.addProperty("contenidosPublicados", redSocial.obtenerTotalContenidosUsuario(userId));
                        datosPerfil.addProperty("solicitudesPublicadas", redSocial.obtenerTotalSolicitudesUsuario(userId));

                        respuesta.addProperty("exito", true);
                        respuesta.add("datosUsuario", datosPerfil);

                    } catch (IllegalArgumentException e) {
                        respuesta.addProperty("exito", false);
                        respuesta.addProperty("mensaje", e.getMessage());
                    } catch (Exception e) {
                        respuesta.addProperty("exito", false);
                        respuesta.addProperty("mensaje", "Error interno al obtener datos de perfil");
                    }
                    break;



                case "ACTUALIZAR_DATOS_USUARIO":
                    try {
                        if (!datos.has("id")) {
                            throw new IllegalArgumentException("Falta el ID del usuario");
                        }

                        String userId = datos.get("id").getAsString();
                        Usuario usuario = redSocial.buscarUsuario(userId);
                        if (usuario == null) {
                            throw new IllegalArgumentException("Usuario no encontrado");
                        }

                        // Actualizar campos proporcionados
                        if (datos.has("nombres")) usuario.setNombre(datos.get("nombres").getAsString());
                        if (datos.has("correo")) usuario.setCorreo(datos.get("correo").getAsString());
                        if (datos.has("intereses") && usuario instanceof Estudiante) {
                            Estudiante estudiante2 = (Estudiante) usuario;
                            String interesesStr = datos.get("intereses").getAsString();
                            // Suponiendo que están separados por comas, por ejemplo: "música, lectura, deportes"
                            LinkedList<String> intereses = new LinkedList<>(Arrays.asList(interesesStr.split("\\s*,\\s*")));
                            estudiante2.setIntereses(intereses);
                        }

                        if (datos.has("contrasena")) usuario.setContrasenia(datos.get("contrasena").getAsString());

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
                        respuesta.addProperty("mensaje", e.getMessage());
                    } catch (Exception e) {
                        respuesta.addProperty("exito", false);
                        respuesta.addProperty("mensaje", "Error interno al actualizar usuario");
                    }
                    break;

                case "CREAR_PUBLICACION":
                    try {
                        // Validar campos obligatorios (actualizado con contenidoRuta)
                        if (!datos.has("usuarioId") || !datos.has("titulo") ||
                                !datos.has("tipoContenido") || !datos.has("contenido")) {
                            throw new IllegalArgumentException("Faltan campos obligatorios: usuarioId, título, tipoContenido o contenido");
                        }

                        String usuarioId = datos.get("usuarioId").getAsString();
                        String titulo = datos.get("titulo").getAsString();
                        String descripcion = datos.has("descripcion") ? datos.get("descripcion").getAsString() : "";
                        String tema = datos.has("tema") ? datos.get("tema").getAsString() : "General";
                        TipoContenido tipo1 = TipoContenido.valueOf(datos.get("tipoContenido").getAsString());
                        String contenidoRuta = datos.get("contenido").getAsString(); // Ruta o URL

                        // Validación adicional para enlaces
                        if (tipo1 == TipoContenido.ENLACE && !contenidoRuta.matches("^(https?|ftp)://.*")) {
                            throw new IllegalArgumentException("Formato de enlace inválido. Debe comenzar con http://, https:// o ftp://");
                        }

                        // Crear y guardar la publicación (actualizado con contenidoRuta)
                        boolean exito = redSocial.crearContenido(
                                usuarioId,
                                titulo,
                                descripcion,
                                tipo1,
                                tema,
                                contenidoRuta // <- Nuevo parámetro
                        );

                        if (exito) {
                            respuesta.addProperty("exito", true);
                            respuesta.addProperty("mensaje", "Publicación creada exitosamente");

                            // Datos extendidos de respuesta
                            JsonObject publicacionJson = new JsonObject();
                            publicacionJson.addProperty("id", usuarioId + "-" + System.currentTimeMillis());
                            publicacionJson.addProperty("titulo", titulo);
                            publicacionJson.addProperty("tipo", tipo1.name());
                            publicacionJson.addProperty("tema", tema);
                            publicacionJson.addProperty("fecha", LocalDateTime.now().toString());
                            publicacionJson.addProperty("contenido", contenidoRuta);
                            respuesta.add("publicacion", publicacionJson);
                        } else {
                            respuesta.addProperty("exito", false);
                            respuesta.addProperty("mensaje", "No se pudo crear la publicación. Verifique los datos.");
                        }

                    } catch (IllegalArgumentException e) {
                        respuesta.addProperty("exito", false);
                        respuesta.addProperty("mensaje", "Error de validación: " + e.getMessage());
                    } catch (Exception e) {
                        respuesta.addProperty("exito", false);
                        respuesta.addProperty("mensaje", "Error interno: " + e.getMessage());
                        e.printStackTrace();
                    }
                    break;

                case "CREAR_SOLICITUD":
                    try {
                        // Validar campos obligatorios según el cliente
                        if (!datos.has("usuarioId") || !datos.has("titulo") ||
                                !datos.has("tema") || !datos.has("descripcion") ||
                                !datos.has("urgencia")) {
                            throw new IllegalArgumentException("Faltan campos obligatorios: usuarioId, título, tema, descripción o urgencia");
                        }

                        String usuarioId = datos.get("usuarioId").getAsString();
                        String titulo = datos.get("titulo").getAsString();
                        String tema = datos.get("tema").getAsString();
                        String descripcion = datos.get("descripcion").getAsString();

                        // Convertir la urgencia del cliente al enum Urgencia
                        String urgenciaStr = datos.get("urgencia").getAsString();
                        Urgencia urgencia;
                        try {
                            urgencia = Urgencia.valueOf(urgenciaStr.toUpperCase());
                        } catch (IllegalArgumentException e) {
                            throw new IllegalArgumentException("Nivel de urgencia inválido. Valores permitidos: ALTA, MEDIA, BAJA");
                        }

                        // Buscar al usuario que crea la solicitud
                        Usuario usuario = redSocial.buscarUsuario(usuarioId);
                        if (usuario == null) {
                            throw new IllegalArgumentException("Usuario no encontrado");
                        }

                        // Crear la solicitud según el modelo
                        SolicitudAyuda solicitud1 = new SolicitudAyuda(
                                null, // El ID se generará automáticamente
                                titulo, // Usamos el título como tema (según el cliente)
                                descripcion,
                                new Date(), // Fecha actual
                                urgencia,
                                usuarioId
                        );

                        // Guardar la solicitud (asumiendo que redSocial tiene este método)
                        boolean exito = redSocial.crearSolicitud(solicitud1);

                        if (exito) {
                            respuesta.addProperty("exito", true);
                            respuesta.addProperty("mensaje", "Solicitud creada exitosamente");

                            // Devolver los datos de la solicitud creada
                            JsonObject solicitudJson = new JsonObject();
                            solicitudJson.addProperty("id", solicitud1.getId());
                            solicitudJson.addProperty("tema", solicitud1.getTema());
                            solicitudJson.addProperty("descripcion", solicitud1.getDescripcion());
                            solicitudJson.addProperty("fecha", solicitud1.getFecha().getTime());
                            solicitudJson.addProperty("urgencia", solicitud1.getUrgencia().name());
                            solicitudJson.addProperty("estado", solicitud1.getEstado().name());
                            solicitudJson.addProperty("solicitanteId", solicitud1.getSolicitanteId());

                            respuesta.add("solicitud", solicitudJson);
                        } else {
                            respuesta.addProperty("exito", false);
                            respuesta.addProperty("mensaje", "No se pudo crear la solicitud");
                        }

                    } catch (IllegalArgumentException e) {
                        respuesta.addProperty("exito", false);
                        respuesta.addProperty("mensaje", "Error de validación: " + e.getMessage());
                    } catch (Exception e) {
                        respuesta.addProperty("exito", false);
                        respuesta.addProperty("mensaje", "Error interno al crear la solicitud: " + e.getMessage());
                        e.printStackTrace();
                    }
                    break;

                case "OBTENER_CONTENIDOS_COMPLETOS":
                    try {
                        // Validación robusta de parámetros
                        if (datos == null || !datos.has("userId") || datos.get("userId").getAsString().isEmpty()) {
                            throw new IllegalArgumentException("Se requiere un ID de usuario válido");
                        }

                        String userId = datos.get("userId").getAsString();

                        // Verificar existencia del usuario primero
                        Usuario usuario = redSocial.buscarUsuario(userId);
                        if (usuario == null) {
                            throw new IllegalArgumentException("Usuario no encontrado");
                        }

                        // Obtener contenidos con manejo de null
                        List<Contenido> contenidos = redSocial.obtenerTodosContenidos();
                        if (contenidos == null) {
                            contenidos = new ArrayList<>(); // Evitar NPE
                        }

                        // Construcción optimizada del JSON
                        JsonArray contenidosJson = new JsonArray();
                        Gson gson1 = new GsonBuilder()
                                .setDateFormat("yyyy-MM-dd HH:mm:ss")
                                .create();

                        for (Contenido contenido : contenidos) {
                            try {
                                JsonObject contenidoJson = new JsonObject();

                                // Datos básicos con validación
                                contenidoJson.addProperty("id", contenido.getId() != null ? contenido.getId() : "");
                                contenidoJson.addProperty("titulo", contenido.getTitulo() != null ? contenido.getTitulo() : "");
                                contenidoJson.addProperty("autor", contenido.getAutor() != null ? contenido.getAutor() : "");
                                contenidoJson.addProperty("tema", contenido.getTema() != null ? contenido.getTema() : "General");
                                contenidoJson.addProperty("descripcion", contenido.getDescripcion() != null ? contenido.getDescripcion() : "");
                                contenidoJson.addProperty("tipo", contenido.getTipo() != null ? contenido.getTipo().toString() : "TEXTO");

                                // Formatear fecha correctamente
                                if (contenido.getFecha() != null) {
                                    contenidoJson.addProperty("fecha", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(contenido.getFecha()));
                                } else {
                                    contenidoJson.addProperty("fecha", "");
                                }

                                contenidoJson.addProperty("contenido", contenido.getContenido() != null ? contenido.getContenido() : "");

                                // Valoraciones con manejo de null
                                JsonArray valoracionesJson = new JsonArray();
                                if (contenido.getValoraciones() != null) {
                                    for (Valoracion valoracion : contenido.getValoraciones()) {
                                        if (valoracion != null) {
                                            JsonObject valoracionJson = new JsonObject();
                                            valoracionJson.addProperty("usuarioId", valoracion.getUsuarioId() != null ? valoracion.getUsuarioId() : "");
                                            valoracionJson.addProperty("valor", valoracion.getValor());
                                            valoracionJson.addProperty("comentario", valoracion.getComentario() != null ? valoracion.getComentario() : "");
                                            valoracionesJson.add(valoracionJson);
                                        }
                                    }
                                }
                                contenidoJson.add("valoraciones", valoracionesJson);

                                // Cálculo seguro de promedio
                                try {
                                    contenidoJson.addProperty("promedioValoraciones", contenido.obtenerPromedioValoracion());
                                } catch (Exception e) {
                                    contenidoJson.addProperty("promedioValoraciones", 0.0);
                                }


                                contenidosJson.add(contenidoJson);
                            } catch (Exception e) {
                                System.err.println("Error procesando contenido individual: " + e.getMessage());
                                continue; // Continuar con el siguiente contenido si hay error en uno
                            }
                        }

                        // Construir respuesta exitosa
                        respuesta.addProperty("exito", true);
                        respuesta.addProperty("totalContenidos", contenidos.size());
                        respuesta.add("contenidos", contenidosJson);

                        // Agregar metadatos útiles
                        JsonObject metadata = new JsonObject();
                        metadata.addProperty("usuarioId", userId);
                        metadata.addProperty("usuarioNombre", usuario.getNombre());
                        metadata.addProperty("fechaConsulta", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
                        respuesta.add("metadata", metadata);

                    } catch (IllegalArgumentException e) {
                        respuesta.addProperty("exito", false);
                        respuesta.addProperty("errorCode", "INVALID_INPUT");
                        respuesta.addProperty("mensaje", "Error de validación: " + e.getMessage());
                    } catch (SecurityException e) {
                        respuesta.addProperty("exito", false);
                        respuesta.addProperty("errorCode", "ACCESS_DENIED");
                        respuesta.addProperty("mensaje", "No autorizado: " + e.getMessage());
                    } catch (Exception e) {
                        respuesta.addProperty("exito", false);
                        respuesta.addProperty("errorCode", "SERVER_ERROR");
                        respuesta.addProperty("mensaje", "Error interno del servidor");
                        System.err.println("Error crítico en OBTENER_CONTENIDOS_COMPLETOS: ");
                        e.printStackTrace();
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