package main.java.proyectofinal.servidor;

import com.google.gson.*;
import main.java.proyectofinal.excepciones.OperacionFallidaException;
import main.java.proyectofinal.modelo.*;
import main.java.proyectofinal.utils.UtilRedSocial;

import java.io.*;
import java.lang.reflect.Type;
import java.net.*;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ServidorRedSocial {
    private static final int PUERTO = 12345;
    private static final int MAX_HILOS = 50;
    private static final RedSocial redSocial;

    static {
        try {
            redSocial = new RedSocial(new UtilRedSocial());
        } catch (OperacionFallidaException e) {
            throw new RuntimeException(e);
        }
    }

    private static final ExecutorService threadPool = Executors.newFixedThreadPool(MAX_HILOS);
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .create();
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    private static final Logger LOGGER = Logger.getLogger(ServidorRedSocial.class.getName());

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PUERTO)) {
            System.out.println("✅ Servidor iniciado en puerto " + PUERTO);
            inicializarGrafoAfinidad();
            while (true) {
                Socket socketCliente = serverSocket.accept();
                threadPool.execute(() -> manejarCliente(socketCliente));
            }
        } catch (IOException e) {
            System.err.println("❌ Error en servidor: " + e.getMessage());
        } finally {
            threadPool.shutdown();
        }
    }

    private static void inicializarGrafoAfinidad() {
        try {
            System.out.println("🔄 Inicializando grafo de afinidad...");

            // 1. Obtener todos los estudiantes
            List<Usuario> usuarios = redSocial.obtenerTodosUsuarios();
            List<Estudiante> estudiantes = usuarios.stream()
                    .filter(u -> u instanceof Estudiante)
                    .map(u -> (Estudiante) u)
                    .collect(Collectors.toList());

            System.out.println("📊 Total estudiantes encontrados: " + estudiantes.size());

            // 2. Agregar estudiantes al grafo
            estudiantes.forEach(redSocial.getGrafoAfinidad()::agregarEstudiante);

            // 3. Calcular afinidades entre todos los estudiantes
            redSocial.getGrafoAfinidad().calcularAfinidades();

            // 4. Mostrar estado del grafo (opcional, para depuración)
            redSocial.getGrafoAfinidad().mostrarEstado();

            System.out.println("✅ Grafo de afinidad inicializado correctamente");
        } catch (Exception e) {
            System.err.println("❌ Error al inicializar grafo de afinidad: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static JsonObject manejarBuscarContenido(JsonObject datos) {
        try {
            if (!datos.has("criterio") || !datos.has("busqueda")) {
                return crearRespuestaError("Faltan criterios de búsqueda");
            }

            String criterio = datos.get("criterio").getAsString();
            String busqueda = datos.get("busqueda").getAsString().toLowerCase();

            List<Contenido> contenidos = redSocial.obtenerTodosContenidos();
            JsonArray resultados = new JsonArray();

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            for (Contenido contenido : contenidos) {
                boolean coincide = false;

                switch (criterio) {
                    case "Tema":
                        coincide = contenido.getTema().toLowerCase().contains(busqueda);
                        break;
                    case "Autor":
                        coincide = contenido.getAutor().toLowerCase().contains(busqueda);
                        break;
                    case "Tipo":
                        coincide = contenido.getTipo().name().toLowerCase().contains(busqueda);
                        break;
                    case "Fecha":
                        try {
                            // Intentar parsear la fecha de búsqueda
                            Date fechaBusqueda = dateFormat.parse(busqueda);
                            // Comparar con la fecha del contenido
                            coincide = dateFormat.format(contenido.getFecha()).contains(busqueda);
                        } catch (Exception e) {
                            // Si no se puede parsear, buscar como string
                            coincide = dateFormat.format(contenido.getFecha()).toLowerCase().contains(busqueda);
                        }
                        break;
                    default:
                        // Búsqueda general en todos los campos
                        coincide = contenido.getTitulo().toLowerCase().contains(busqueda) ||
                                contenido.getAutor().toLowerCase().contains(busqueda) ||
                                contenido.getTema().toLowerCase().contains(busqueda) ||
                                contenido.getTipo().name().toLowerCase().contains(busqueda) ||
                                dateFormat.format(contenido.getFecha()).toLowerCase().contains(busqueda);
                }

                if (coincide) {
                    JsonObject contenidoJson = new JsonObject();
                    contenidoJson.addProperty("id", contenido.getId());
                    contenidoJson.addProperty("titulo", contenido.getTitulo());
                    contenidoJson.addProperty("autor", contenido.getAutor());
                    contenidoJson.addProperty("tema", contenido.getTema());
                    contenidoJson.addProperty("descripcion", contenido.getDescripcion());
                    contenidoJson.addProperty("tipo", contenido.getTipo().name());

                    // Manejo seguro de fechas
                    try {
                        contenidoJson.addProperty("fechaCreacion", dateFormat.format(contenido.getFecha()));
                    } catch (Exception e) {
                        contenidoJson.addProperty("fechaCreacion", "Fecha no disponible");
                    }

                    contenidoJson.addProperty("contenido", contenido.getContenido());
                    resultados.add(contenidoJson);
                }
            }

            JsonObject respuesta = crearRespuestaExito("Búsqueda completada");
            respuesta.add("resultados", resultados);
            return respuesta;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error en búsqueda de contenidos", e);
            return crearRespuestaError("Error en búsqueda: " + e.getMessage());
        }
    }
    // En ServidorRedSocial.java

    private static JsonObject manejarUnirseAGrupo(JsonObject datos) {
        try {
            if (!datos.has("grupoId") || !datos.has("usuarioId")) {
                return crearRespuestaError("Faltan datos necesarios para unirse al grupo");
            }

            String grupoId = datos.get("grupoId").getAsString();
            String usuarioId = datos.get("usuarioId").getAsString();

            // Verificar que el grupo existe
            GrupoEstudio grupo = redSocial.buscarGrupoEstudio(grupoId);
            if (grupo == null) {
                return crearRespuestaError("El grupo no existe");
            }

            // Verificar que el usuario no es ya miembro
            if (grupo.getIdMiembros().contains(usuarioId)) {
                return crearRespuestaError("Ya eres miembro de este grupo");
            }

            // Verificar límite de miembros
            if (grupo.getIdMiembros().size() >= GrupoEstudio.MAX_MIEMBROS) {
                return crearRespuestaError("El grupo ha alcanzado su capacidad máxima");
            }

            // Unir al usuario al grupo
            if (redSocial.unirUsuarioAGrupo(usuarioId, grupoId)) {
                JsonObject respuesta = crearRespuestaExito("Te has unido al grupo exitosamente");

                // Devolver datos actualizados del grupo
                JsonObject grupoJson = new JsonObject();
                grupoJson.addProperty("id", grupo.getIdGrupo());
                grupoJson.addProperty("nombre", grupo.getNombre());
                grupoJson.addProperty("totalMiembros", grupo.getIdMiembros().size());
                respuesta.add("grupo", grupoJson);

                return respuesta;
            }

            return crearRespuestaError("No se pudo unir al grupo");
        } catch (Exception e) {
            return crearRespuestaError("Error al unirse al grupo: " + e.getMessage());
        }
    }

    private static JsonObject manejarObtenerMiembrosGrupo(JsonObject datos) {
        try {
            // Null check for redSocial instance
            if (redSocial == null) {
                return crearRespuestaError("Sistema de grupos no disponible");
            }

            String grupoId = datos.get("grupoId").getAsString();
            GrupoEstudio grupo = redSocial.buscarGrupoEstudio(grupoId);

            if (grupo == null) {
                return crearRespuestaError("Grupo no encontrado: " + grupoId);
            }

            // Null check for members list
            List<String> idMiembros = grupo.getIdMiembros();
            if (idMiembros == null) {
                return crearRespuestaError("El grupo no tiene lista de miembros");
            }

            JsonArray miembrosJson = new JsonArray();
            for (String miembroId : idMiembros) {
                Usuario usuario = redSocial.buscarUsuario(miembroId);
                if (usuario != null) {
                    JsonObject miembroJson = new JsonObject();
                    miembroJson.addProperty("id", usuario.getId());
                    miembroJson.addProperty("nombre", usuario.getNombre());
                    miembrosJson.add(miembroJson);
                }
            }

            JsonObject respuesta = crearRespuestaExito("Miembros obtenidos");
            respuesta.add("miembros", miembrosJson);
            return respuesta;

        } catch (Exception e) {
            return crearRespuestaError("Error interno: " + e.getMessage());
        }
    }

    private static JsonObject manejarObtenerContenidoGrupo(JsonObject datos) {
        try {
            if (!datos.has("grupoId")) {
                return crearRespuestaError("Se requiere el ID del grupo");
            }

            String grupoId = datos.get("grupoId").getAsString();
            GrupoEstudio grupo = redSocial.buscarGrupoEstudio(grupoId);
            if (grupo == null) {
                return crearRespuestaError("Grupo no encontrado");
            }

            JsonArray contenidoJson = new JsonArray();
            grupo.getIdContenidos().forEach(contenidoId -> {
                Contenido contenido = redSocial.buscarContenido(contenidoId);
                if (contenido != null) {
                    JsonObject contenidoItem = new JsonObject();
                    contenidoItem.addProperty("id", contenido.getId());
                    contenidoItem.addProperty("titulo", contenido.getTitulo());
                    contenidoItem.addProperty("autor", contenido.getAutor());
                    contenidoItem.addProperty("tema", contenido.getTema());
                    contenidoItem.addProperty("fecha", dateFormat.format(contenido.getFecha()));
                    contenidoJson.add(contenidoItem);
                }
            });

            JsonObject respuesta = crearRespuestaExito("Contenido del grupo obtenido");
            respuesta.add("contenido", contenidoJson);
            return respuesta;
        } catch (Exception e) {
            return crearRespuestaError("Error al obtener contenido: " + e.getMessage());
        }
    }

    private static JsonObject manejarEliminarContenidoGrupo(JsonObject datos) {
        try {
            if (!datos.has("grupoId") || !datos.has("contenidoId") || !datos.has("usuarioId")) {
                return crearRespuestaError("Faltan datos necesarios");
            }

            String grupoId = datos.get("grupoId").getAsString();
            String contenidoId = datos.get("contenidoId").getAsString();
            String usuarioId = datos.get("usuarioId").getAsString();

            // Verificar permisos (solo creador del contenido o moderador puede eliminarlo)
            Contenido contenido = redSocial.buscarContenido(contenidoId);
            if (contenido == null) {
                return crearRespuestaError("Contenido no encontrado");
            }

            Usuario usuario = redSocial.buscarUsuario(usuarioId);
            if (usuario == null) {
                return crearRespuestaError("Usuario no válido");
            }

            if (!contenido.getAutor().equals(usuarioId) && !(usuario instanceof Moderador)) {
                return crearRespuestaError("No tienes permisos para eliminar este contenido");
            }

            // Eliminar contenido del grupo
            if (redSocial.eliminarContenidoDeGrupo(grupoId, contenidoId)) {
                return crearRespuestaExito("Contenido eliminado del grupo");
            }

            return crearRespuestaError("No se pudo eliminar el contenido");
        } catch (Exception e) {
            return crearRespuestaError("Error al eliminar contenido: " + e.getMessage());
        }
    }

    private static void manejarCliente(Socket socket) {
        String clienteIp = socket.getInetAddress().getHostAddress();
        System.out.println("🔗 Cliente conectado desde: " + clienteIp);

        try (BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter salida = new PrintWriter(socket.getOutputStream(), true)) {

            String mensaje;
            while ((mensaje = entrada.readLine()) != null) {
                JsonObject respuesta = procesarSolicitud(mensaje);
                salida.println(respuesta.toString());
            }

        } catch (IOException e) {
            System.err.println("⚠️ Error con cliente " + clienteIp + ": " + e.getMessage());
        } finally {
            try {
                socket.close();
                System.out.println("🔌 Cliente " + clienteIp + " desconectado.");
            } catch (IOException e) {
                System.err.println("Error al cerrar socket: " + e.getMessage());
            }
        }
    }

    private static JsonObject procesarSolicitud(String mensajeJson) {
        JsonObject respuesta = new JsonObject();
        respuesta.addProperty("timestamp", System.currentTimeMillis());

        try {
            JsonObject solicitud = JsonParser.parseString(mensajeJson).getAsJsonObject();

            if (!solicitud.has("tipo") || !solicitud.has("datos")) {
                return crearRespuestaError("JSON mal formado: falta 'tipo' o 'datos'");
            }

            String tipo = solicitud.get("tipo").getAsString();
            JsonObject datos = solicitud.getAsJsonObject("datos");

            System.out.println("📥 Solicitud [" + tipo + "] recibida: " + datos);

            switch (tipo) {
                case "REGISTRO":
                    return manejarRegistro(datos);
                case "LOGIN":
                    return manejarLogin(datos);
                case "OBTENER_CONTENIDOS":
                    return manejarObtenerContenidos();
                case "OBTENER_SOLICITUDES":
                    return manejarObtenerSolicitudes();
                case "ACTUALIZAR_USUARIO":
                    return manejarActualizarUsuario(datos);
                case "ELIMINAR_USUARIO":
                    return manejarEliminarUsuario(datos);
                case "OBTENER_USUARIO":
                    return manejarObtenerUsuario(datos);
                case "OBTENER_DATOS_MODERADOR":
                    return manejarDatosModerador(datos);
                case "OBTENER_DATOS_PERFIL":
                    return manejarDatosPerfil(datos);
                case "ACTUALIZAR_DATOS_USUARIO":
                    return manejarActualizarDatosUsuario(datos);
                case "CREAR_PUBLICACION":
                    return manejarCrearPublicacion(datos);
                case "CREAR_SOLICITUD":
                    return manejarCrearSolicitud(datos);
                case "OBTENER_CONTENIDOS_COMPLETOS":
                    return manejarContenidosCompletos(datos);
                case "AGREGAR_VALORACION":
                    return manejarAgregarValoracion(datos);
                case "OBTENER_VALORACIONES":
                    return manejarObtenerValoraciones(datos);
                case "OBTENER_VALORACION":
                    return manejarObtenerValoracion(datos);
                case "BUSCAR_CONTENIDO":
                    return manejarBuscarContenido(datos);
                case "OBTENER_CONTENIDOS_USUARIO":
                    return manejarObtenerContenidosUsuario(datos);
                case "OBTENER_SOLICITUDES_USUARIO":
                    return manejarObtenerSolicitudesUsuario(datos);
                case "OBTENER_SUGERENCIAS":
                    return manejarObtenerSugerencias(datos);
                case "SUSPENDER_USUARIO":
                    return manejarSuspenderUsuario(datos);
                case "REACTIVAR_USUARIO":
                    return manejarReactivarUsuario(datos);
                case "ELIMINAR_CONTENIDO":
                    return manejarEliminarContenido(datos);
                case "OBTENER_CONTENIDO_COMPLETO":
                    return manejarObtenerContenidoCompleto(datos);
                case "OBTENER_TODOS_USUARIOS":
                    return manejarObtenerTodosUsuarios(datos);
                case "ACTUALIZAR_CONTENIDO":
                    return manejarActualizarContenido(datos);
                case "UNIRSE_GRUPO":
                    return manejarUnirseAGrupo(datos);
                case "OBTENER_MIEMBROS_GRUPO":
                    return manejarObtenerMiembrosGrupo(datos);
                case "OBTENER_CONTENIDO_GRUPO":
                    return manejarObtenerContenidoGrupo(datos);
                case "ELIMINAR_CONTENIDO_GRUPO":
                    return manejarEliminarContenidoGrupo(datos);
                case "OBTENER_GRUPOS_ESTUDIO":
                    return manejarObtenerGruposEstudio(datos);
                case "ACTUALIZAR_GRUPO":
                    return manejarActualizarGrupo(datos);
                case "ELIMINAR_GRUPO":
                    return manejarEliminarGrupo(datos);

                default:
                    return crearRespuestaError("Operación no soportada");
            }
        } catch (JsonSyntaxException e) {
            return crearRespuestaError("JSON inválido: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("❌ Error procesando solicitud: " + e.getMessage());
            e.printStackTrace();
            return crearRespuestaError("Error interno del servidor");
        }

    }



    private static JsonObject manejarEliminarGrupo(JsonObject datos) {
        try {
            if (!datos.has("grupoId") || !datos.has("usuarioId")) {
                return crearRespuestaError("Faltan datos necesarios para eliminar el grupo");
            }
            String grupoId = datos.get("grupoId").getAsString();
            String usuarioId = datos.get("usuarioId").getAsString();
            // Verificar que el grupo existe
            GrupoEstudio grupo = redSocial.buscarGrupoEstudio(grupoId);
            if (grupo == null) {
                return crearRespuestaError("El grupo no existe");
            }

            // Eliminar el grupo
            if (redSocial.eliminarGrupo(grupoId)) {
                return crearRespuestaExito("Grupo eliminado correctamente");
            }
            return crearRespuestaError("No se pudo eliminar el grupo");
        } catch (Exception e) {
            return crearRespuestaError("Error al eliminar el grupo: " + e.getMessage());
        }
    }

    private static JsonObject manejarActualizarGrupo(JsonObject datos) {
        try {
            if (!datos.has("grupoId") || !datos.has("nombre") || !datos.has("descripcion") || !datos.has("usuarioId")) {
                return crearRespuestaError("Faltan datos necesarios para actualizar el grupo");
            }
            String grupoId = datos.get("grupoId").getAsString();
            String nombre = datos.get("nombre").getAsString();
            String descripcion = datos.get("descripcion").getAsString();
            String usuarioId = datos.get("usuarioId").getAsString();
            // Verificar que el grupo existe
            GrupoEstudio grupo = redSocial.buscarGrupoEstudio(grupoId);
            if (grupo == null) {
                return crearRespuestaError("El grupo no existe");
            }
            // Actualizar los datos del grupo
            grupo.setNombre(nombre);
            grupo.setDescripcion(descripcion);

            // Guardar los cambios en el sistema
            if (redSocial.actualizarGrupo(grupo)) {
                return crearRespuestaExito("Grupo actualizado correctamente");
            }
            return crearRespuestaError("No se pudo actualizar el grupo");
        } catch (Exception e) {
            return crearRespuestaError("Error al actualizar el grupo: " + e.getMessage());
        }
    }

    private static JsonObject manejarObtenerGruposEstudio(JsonObject datos) {
        // Constantes de configuración
        final int MAX_MIEMBROS_PARA_DETALLE = 20;
        final boolean INCLUIR_NO_MIEMBROS = true;

        try {
            // 1. Validaciones iniciales
            if (redSocial == null) {
                return crearRespuestaError("Sistema de red social no disponible");
            }

            if (!datos.has("userId")) {
                return crearRespuestaError("Se requiere el ID de usuario");
            }

            String userId = datos.get("userId").getAsString();
            Usuario usuario = redSocial.buscarUsuario(userId);
            if (usuario == null) {
                return crearRespuestaError("Usuario no encontrado");
            }

            // 2. Procesamiento de grupos
            long inicio = System.currentTimeMillis();

            // Formar nuevos grupos solo si el usuario es estudiante
            List<GrupoEstudio> nuevosGrupos = Collections.emptyList();
            if (usuario instanceof Estudiante) {
                nuevosGrupos = redSocial.formarGruposAutomaticos(redSocial.getUsuarios());
                System.out.printf("Formados %d nuevos grupos en %d ms%n",
                        nuevosGrupos.size(),
                        System.currentTimeMillis() - inicio);
            }

            // Obtener todos los grupos optimizados
            List<GrupoEstudio> todosGrupos = redSocial.obtenerTodosGrupos();

            // 3. Construcción de respuesta
            JsonArray gruposJson = new JsonArray();
            int gruposComoMiembro = 0;

            for (GrupoEstudio grupo : todosGrupos) {
                // Filtro opcional: solo grupos donde es miembro
                boolean esMiembro = grupo.getIdMiembros().contains(userId);
                if (!INCLUIR_NO_MIEMBROS && !esMiembro) continue;

                if (esMiembro) gruposComoMiembro++;

                JsonObject grupoJson = new JsonObject();
                grupoJson.addProperty("id", grupo.getIdGrupo());
                grupoJson.addProperty("nombre", grupo.getNombre());
                grupoJson.addProperty("descripcion", grupo.getDescripcion());
                grupoJson.addProperty("esMiembro", esMiembro);
                grupoJson.addProperty("fechaCreacion", grupo.getFechaCreacion().toString());
                grupoJson.addProperty("totalMiembros", grupo.getIdMiembros().size());

                // Detalle de miembros solo si es relevante
                if (esMiembro || grupo.getIdMiembros().size() <= MAX_MIEMBROS_PARA_DETALLE) {
                    JsonArray miembrosJson = new JsonArray();
                    grupo.getIdMiembros().stream()
                            .map(redSocial::buscarUsuario)
                            .filter(Objects::nonNull)
                            .forEach(miembro -> {
                                JsonObject miembroJson = new JsonObject();
                                miembroJson.addProperty("id", miembro.getId());
                                miembroJson.addProperty("nombre", miembro.getNombre());
                                miembrosJson.add(miembroJson);
                            });
                    grupoJson.add("miembros", miembrosJson);
                }

                gruposJson.add(grupoJson);
            }

            // 4. Estadísticas adicionales
            JsonObject respuesta = crearRespuestaExito("Grupos obtenidos exitosamente");
            respuesta.add("grupos", gruposJson);
            respuesta.addProperty("totalGrupos", todosGrupos.size());
            respuesta.addProperty("gruposComoMiembro", gruposComoMiembro);
            respuesta.addProperty("nuevosGruposCreados", nuevosGrupos.size());
            respuesta.addProperty("tiempoProcesamiento", System.currentTimeMillis() - inicio + " ms");

            return respuesta;

        } catch (Exception e) {
            System.err.println("Error en manejarObtenerGruposEstudio: " + e.getMessage());
            e.printStackTrace();
            return crearRespuestaError("Error interno al procesar grupos");
        }
    }

    private static JsonObject manejarActualizarContenido(JsonObject datos) {
        try {
            // Validar estructura básica
            if (!datos.has("contenido")) {
                return crearRespuestaError("Se requieren los datos del contenido");
            }

            JsonObject contenidoJson = datos.getAsJsonObject("contenido");

            // Validar que el ID esté presente
            if (!contenidoJson.has("id") || contenidoJson.get("id").isJsonNull()) {
                return crearRespuestaError("Se requiere el ID del contenido");
            }

            String id = contenidoJson.get("id").getAsString();

            // Buscar el contenido existente
            Contenido contenidoExistente = redSocial.buscarContenido(id);
            if (contenidoExistente == null) {
                return crearRespuestaError("No se encontró el contenido con ID: " + id);
            }

            // Actualizar campos
            if (contenidoJson.has("titulo")) {
                contenidoExistente.setTitulo(contenidoJson.get("titulo").getAsString().trim());
            }

            if (contenidoJson.has("autor")) {
                contenidoExistente.setAutor(contenidoJson.get("autor").getAsString().trim());
            }

            if (contenidoJson.has("tema")) {
                contenidoExistente.setTema(contenidoJson.get("tema").getAsString().trim());
            }

            if (contenidoJson.has("descripcion")) {
                contenidoExistente.setDescripcion(contenidoJson.get("descripcion").getAsString().trim());
            }

            if (contenidoJson.has("contenido")) {
                contenidoExistente.setContenido(contenidoJson.get("contenido").getAsString().trim());
            }

            if (contenidoJson.has("tipo")) {
                try {
                    TipoContenido tipo = TipoContenido.valueOf(
                            contenidoJson.get("tipo").getAsString().trim().toUpperCase());
                    contenidoExistente.setTipo(tipo);
                } catch (IllegalArgumentException e) {
                    return crearRespuestaError("Tipo de contenido inválido. Valores permitidos: " +
                            Arrays.toString(TipoContenido.values()));
                }
            }

            if (contenidoJson.has("fecha")) {
                try {
                    LocalDateTime fecha = LocalDateTime.parse(
                            contenidoJson.get("fecha").getAsString(),
                            DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                    contenidoExistente.setFecha(fecha);
                } catch (Exception e) {
                    System.err.println("Error al parsear fecha, manteniendo fecha existente");
                }
            }

            // Actualizar en el sistema
            if (redSocial.actualizarContenido(contenidoExistente)) {
                JsonObject respuesta = crearRespuestaExito("Contenido actualizado correctamente");

                // Devolver datos actualizados
                JsonObject contenidoRespuesta = new JsonObject();
                contenidoRespuesta.addProperty("id", contenidoExistente.getId());
                contenidoRespuesta.addProperty("titulo", contenidoExistente.getTitulo());
                contenidoRespuesta.addProperty("autor", contenidoExistente.getAutor());
                contenidoRespuesta.addProperty("tema", contenidoExistente.getTema());
                respuesta.add("contenido", contenidoRespuesta);

                return respuesta;
            }

            return crearRespuestaError("No se pudo guardar la actualización en el sistema");

        } catch (Exception e) {
            System.err.println("Error al actualizar contenido: " + e.getMessage());
            return crearRespuestaError("Error interno al procesar la actualización");
        }
    }

    private static JsonObject manejarObtenerTodosUsuarios(JsonObject datos) {
        try {
            List<Usuario> usuarios = redSocial.obtenerTodosUsuarios();
            JsonArray usuariosJson = new JsonArray();

            for (Usuario usuario : usuarios) {
                JsonObject usuarioJson = new JsonObject();
                usuarioJson.addProperty("id", usuario.getId());
                usuarioJson.addProperty("nombre", usuario.getNombre());
                usuarioJson.addProperty("correo", usuario.getCorreo());
                usuarioJson.addProperty("suspendido", usuario.isSuspendido());
                usuarioJson.addProperty("diasSuspension", usuario.getDiasSuspension());
                usuarioJson.addProperty("esModerador", usuario instanceof Moderador);

                usuariosJson.add(usuarioJson);
            }

            JsonObject respuesta = crearRespuestaExito("Usuarios obtenidos");
            respuesta.add("usuarios", usuariosJson);
            return respuesta;
        } catch (Exception e) {
            return crearRespuestaError("Error al obtener usuarios: " + e.getMessage());
        }
    }

    private static JsonObject manejarSuspenderUsuario(JsonObject datos) {
        try {
            if (!datos.has("correoUsuario") || !datos.has("diasSuspension") || !datos.has("moderadorId")) {
                return crearRespuestaError("Faltan datos para suspender usuario");
            }

            String correo = datos.get("correoUsuario").getAsString();
            int dias = datos.get("diasSuspension").getAsInt();
            String moderadorId = datos.get("moderadorId").getAsString();

            // Verificar que el moderador existe
            Usuario moderador = redSocial.buscarUsuario(moderadorId);
            if (moderador == null || !(moderador instanceof Moderador)) {
                return crearRespuestaError("No tienes permisos para esta acción");
            }

            if (redSocial.suspenderUsuario(correo, dias)) {
                return crearRespuestaExito("Usuario suspendido correctamente");
            }
            return crearRespuestaError("No se pudo suspender al usuario");
        } catch (Exception e) {
            return crearRespuestaError("Error al suspender usuario: " + e.getMessage());
        }
    }

    private static JsonObject manejarReactivarUsuario(JsonObject datos) {
        try {
            if (!datos.has("correoUsuario") || !datos.has("moderadorId")) {
                return crearRespuestaError("Faltan datos para reactivar usuario");
            }

            String correo = datos.get("correoUsuario").getAsString();
            String moderadorId = datos.get("moderadorId").getAsString();

            // Verificar que el moderador existe
            Usuario moderador = redSocial.buscarUsuario(moderadorId);
            if (moderador == null || !(moderador instanceof Moderador)) {
                return crearRespuestaError("No tienes permisos para esta acción");
            }

            if (redSocial.reactivarUsuario(correo)) {
                return crearRespuestaExito("Usuario reactivado correctamente");
            }
            return crearRespuestaError("No se pudo reactivar al usuario");
        } catch (Exception e) {
            return crearRespuestaError("Error al reactivar usuario: " + e.getMessage());
        }
    }

    private static JsonObject manejarEliminarContenido(JsonObject datos) {
        // Validación básica de entrada
        if (!datos.has("contenidoId") || !datos.has("moderadorId")) {
            return crearRespuestaError("Se requieren ambos IDs: contenidoId y moderadorId");
        }

        String contenidoId = datos.get("contenidoId").getAsString().trim();
        String moderadorId = datos.get("moderadorId").getAsString().trim();

        if (contenidoId.isEmpty() || moderadorId.isEmpty()) {
            return crearRespuestaError("Los IDs no pueden estar vacíos");
        }

        try {
            System.out.println("[DEBUG] Intentando eliminar contenido ID: " + contenidoId);
            System.out.println("[DEBUG] Solicitado por moderador ID: " + moderadorId);

            // Verificar permisos del moderador
            Usuario moderador = redSocial.buscarUsuario(moderadorId);
            if (moderador == null || !(moderador instanceof Moderador)) {
                System.out.println("[WARN] Intento de eliminación por usuario no autorizado: " + moderadorId);
                return crearRespuestaError("No tienes permisos para esta acción");
            }

            // Intentar eliminación
            boolean eliminado = redSocial.eliminarContenido(contenidoId);

            if (eliminado) {
                System.out.println("[INFO] Contenido eliminado exitosamente: " + contenidoId);
                return crearRespuestaExito("Contenido eliminado correctamente");
            } else {
                System.out.println("[WARN] No se pudo eliminar el contenido: " + contenidoId);
                return crearRespuestaError("El contenido no existe o no se pudo eliminar");
            }

        } catch (IllegalArgumentException e) {
            System.err.println("[ERROR] Error de validación al eliminar: " + e.getMessage());
            return crearRespuestaError(e.getMessage());
        } catch (Exception e) {
            System.err.println("[ERROR] Excepción inesperada al eliminar contenido " + contenidoId + ": " + e.getMessage());
            e.printStackTrace();
            return crearRespuestaError("Error interno al procesar la solicitud");
        }
    }

    private static JsonObject manejarObtenerContenidoCompleto(JsonObject datos) {
        // Validación mejorada
        if (!datos.has("contenidoId")) {
            return crearRespuestaError("Se requiere el campo 'contenidoId'");
        }

        String contenidoId = datos.get("contenidoId").getAsString().trim();
        if (contenidoId.isEmpty()) {
            return crearRespuestaError("El ID del contenido no puede estar vacío");
        }

        try {
            Contenido contenido = redSocial.buscarContenido(contenidoId);
            JsonObject contenidoJson = construirJsonContenido(contenidoId, contenido);

            JsonObject respuesta = crearRespuestaExito("Contenido obtenido exitosamente");
            respuesta.add("contenido", contenidoJson);
            return respuesta;

        } catch (IllegalArgumentException e) {
            System.err.println("Error de búsqueda: " + e.getMessage());
            return crearRespuestaError("No se encontró contenido con ID " + contenidoId);
        } catch (Exception e) {
            System.err.println("Error inesperado: " + e.getMessage());
            return crearRespuestaError("Error interno al procesar la solicitud");
        }
    }

    // Método auxiliar para construir el JSON del contenido
    private static JsonObject construirJsonContenido(String contenidoId, Contenido contenido) {
        JsonObject contenidoJson = new JsonObject();

        // Datos básicos
        contenidoJson.addProperty("id", contenido.getId());
        contenidoJson.addProperty("titulo", contenido.getTitulo());
        contenidoJson.addProperty("autor", contenido.getAutor());
        contenidoJson.addProperty("tema", contenido.getTema());
        contenidoJson.addProperty("descripcion", contenido.getDescripcion());
        contenidoJson.addProperty("tipo", contenido.getTipo().name());
        contenidoJson.addProperty("contenido", contenido.getContenido());

        // Manejo de fecha
        try {
            LocalDateTime fechaLocal = contenido.getFecha();
            if (fechaLocal != null) {
                Date fecha = Date.from(fechaLocal.atZone(ZoneId.systemDefault()).toInstant());
                contenidoJson.addProperty("fechaCreacion", dateFormat.format(fecha));
            } else {
                contenidoJson.addProperty("fechaCreacion", "");
                System.err.println("Advertencia: Fecha es null para contenido ID: " + contenidoId);
            }
        } catch (Exception e) {
            System.err.println("Error procesando fecha para contenido ID " + contenidoId + ": " + e.getMessage());
            contenidoJson.addProperty("fechaCreacion", "");
        }

        // Valoraciones
        JsonArray valoracionesJson = new JsonArray();
        if (contenido.getValoraciones() != null && !contenido.getValoraciones().isEmpty()) {
            for (Valoracion valoracion : contenido.getValoraciones()) {
                JsonObject v = new JsonObject();
                v.addProperty("usuarioId", valoracion.getUsuarioId());
                v.addProperty("valor", valoracion.getValor());
                v.addProperty("comentario",
                        valoracion.getComentario() != null ? valoracion.getComentario() : "");
                valoracionesJson.add(v);
            }
        }
        contenidoJson.add("valoraciones", valoracionesJson);

        return contenidoJson;
    }
    ///
    private static JsonObject manejarObtenerSugerencias(JsonObject datos) {
        try {
            if (!datos.has("userId")) {
                return crearRespuestaError("Falta el ID del usuario");
            }

            String userId = datos.get("userId").getAsString();
            LOGGER.info("Calculando sugerencias para: " + userId);

            // Verificar que el usuario existe y es estudiante
            Usuario usuario = redSocial.buscarUsuario(userId);
            if (usuario == null || !(usuario instanceof Estudiante)) {
                return crearRespuestaError("Usuario no encontrado o no es estudiante");
            }

            // Obtener sugerencias del grafo de afinidad
            List<Estudiante> sugerencias = redSocial.obtenerSugerenciasCompaneros(userId);
            LOGGER.info("Sugerencias encontradas: " + sugerencias.size());

            // Verificar si el grafo tiene datos
            if (redSocial.getGrafoAfinidad().obtenerTotalEstudiantes() == 0) {
                LOGGER.warning("El grafo de afinidad está vacío");
                return crearRespuestaError("El sistema de recomendaciones no está inicializado");
            }

            JsonArray sugerenciasJson = new JsonArray();

            for (Estudiante estudiante : sugerencias) {
                JsonObject estudianteJson = new JsonObject();
                estudianteJson.addProperty("id", estudiante.getId());
                estudianteJson.addProperty("nombre", estudiante.getNombre());

                // Intereses
                String intereses = estudiante.getIntereses() != null ?
                        String.join(", ", estudiante.getIntereses()) : "Sin intereses";
                estudianteJson.addProperty("intereses", intereses);

                // Grupo de estudio
                String grupoEstudio = redSocial.obtenerGruposEstudioPorUsuario(estudiante.getId());
                estudianteJson.addProperty("grupo", grupoEstudio != null ? grupoEstudio : "Sin grupo");

                // Peso de afinidad (para depuración)
                int peso = redSocial.getGrafoAfinidad().obtenerPesoAfinidad((Estudiante)usuario, estudiante);
                estudianteJson.addProperty("afinidad", peso);

                sugerenciasJson.add(estudianteJson);
            }

            JsonObject respuesta = crearRespuestaExito("Sugerencias obtenidas");
            respuesta.add("sugerencias", sugerenciasJson);

            // Datos de depuración
            respuesta.addProperty("totalEstudiantes", redSocial.getGrafoAfinidad().obtenerTotalEstudiantes());
            respuesta.addProperty("totalConexiones", redSocial.getGrafoAfinidad().obtenerTotalConexiones());

            return respuesta;
        } catch (Exception e) {
            LOGGER.severe("Error al obtener sugerencias: " + e.getMessage());
            return crearRespuestaError("Error al obtener sugerencias: " + e.getMessage());
        }
    }

    private static JsonObject manejarAgregarValoracion(JsonObject datos) {
        try {
            // Validar campos obligatorios
            if (!datos.has("contenidoId") || !datos.has("usuarioId") ||
                    !datos.has("puntuacion") || !datos.has("comentario")) {
                return crearRespuestaError("Faltan campos obligatorios para la valoración");
            }

            String contenidoId = datos.get("contenidoId").getAsString();
            String usuarioId = datos.get("usuarioId").getAsString();
            int puntuacion = datos.get("puntuacion").getAsInt();
            String comentario = datos.get("comentario").getAsString();
            String usuarioNombre = datos.has("usuarioNombre") ?
                    datos.get("usuarioNombre").getAsString() : "Anónimo";

            // Validar rango de puntuación
            if (puntuacion < 1 || puntuacion > 5) {
                return crearRespuestaError("La puntuación debe estar entre 1 y 5");
            }

            // Verificar si el usuario ya valoró este contenido
            if (redSocial.usuarioYaValoroContenido(usuarioId, contenidoId)) {
                return crearRespuestaError("Ya has valorado este contenido anteriormente");
            }

            // Obtener el contenido para extraer tema y descripción
            Contenido contenido = redSocial.buscarContenido(contenidoId);
            if (contenido == null) {
                return crearRespuestaError("Contenido no encontrado");
            }

            // Crear la valoración según el modelo del servidor
            Valoracion valoracion = new Valoracion(
                    null, // ID se generará automáticamente
                    contenido.getTema(),
                    contenido.getDescripcion(),
                    usuarioId,
                    puntuacion,
                    new Date(),
                    comentario
            );

            // Agregar la valoración al contenido
            if (redSocial.agregarValoracion(contenidoId, valoracion)) {
                JsonObject respuesta = crearRespuestaExito("Valoración agregada correctamente");

                // Construir objeto valoración para la respuesta
                JsonObject valoracionJson = new JsonObject();
                valoracionJson.addProperty("id", valoracion.getIdValoracion());
                valoracionJson.addProperty("autor", usuarioNombre);
                valoracionJson.addProperty("puntuacion", valoracion.getValor());
                valoracionJson.addProperty("comentario", valoracion.getComentario());
                valoracionJson.addProperty("fecha", dateFormat.format(valoracion.getFecha()));

                respuesta.add("valoracion", valoracionJson);

                // Incluir el promedio actualizado
                double promedio = contenido.obtenerPromedioValoracion();
                respuesta.addProperty("promedio", promedio);

                return respuesta;
            }

            return crearRespuestaError("No se pudo agregar la valoración");
        } catch (Exception e) {
            return crearRespuestaError("Error al agregar valoración: " + e.getMessage());
        }
    }

    private static JsonObject manejarObtenerValoraciones(JsonObject datos) {
        try {
            if (!datos.has("contenidoId")) {
                return crearRespuestaError("Se requiere ID de contenido");
            }

            String contenidoId = datos.get("contenidoId").getAsString();
            Contenido contenido = redSocial.buscarContenido(contenidoId);
            if (contenido == null) {
                return crearRespuestaError("Contenido no encontrado");
            }

            JsonArray valoracionesJson = new JsonArray();
            Set<String> idsProcesados = new HashSet<>();

            // Procesar valoraciones del campo 'valoraciones'
            for (Valoracion valoracion : contenido.getValoraciones()) {
                if (idsProcesados.add(valoracion.getIdValoracion())) {
                    JsonObject v = new JsonObject();
                    v.addProperty("id", valoracion.getIdValoracion());

                    // Buscar usuario y manejar caso cuando no se encuentra
                    Usuario autor = redSocial.buscarUsuario(valoracion.getIdAutor());
                    if (autor != null) {
                        v.addProperty("autor", autor.getNombre());
                        v.addProperty("autorId", autor.getId()); // Incluir ID para referencia
                    } else {
                        // Si no se encuentra el usuario, usar el ID original como nombre
                        v.addProperty("autor", "Usuario " + valoracion.getIdAutor());
                        v.addProperty("autorId", valoracion.getIdAutor());
                    }

                    v.addProperty("puntuacion", valoracion.getValor());
                    v.addProperty("comentario", valoracion.getComentario());
                    v.addProperty("fecha", dateFormat.format(valoracion.getFecha()));
                    valoracionesJson.add(v);
                }
            }

            // Procesar valoraciones embebidas en el campo 'contenido' si no hay en 'valoraciones'
            if (valoracionesJson.size() == 0 && contenido.getContenido() != null &&
                    contenido.getContenido().contains("~")) {

                String[] valoracionesStr = contenido.getContenido().split("\\|");
                for (String valoracionStr : valoracionesStr) {
                    if (valoracionStr.trim().isEmpty()) continue;

                    String[] partes = valoracionStr.split("~");
                    if (partes.length >= 7) {
                        JsonObject v = new JsonObject();
                        v.addProperty("id", partes[0]);

                        // Buscar usuario para valoraciones embebidas
                        Usuario autorEmbebido = redSocial.buscarUsuario(partes[3]);
                        if (autorEmbebido != null) {
                            v.addProperty("autor", autorEmbebido.getNombre());
                            v.addProperty("autorId", autorEmbebido.getId());
                        } else {
                            v.addProperty("autor", "Usuario " + partes[3]);
                            v.addProperty("autorId", partes[3]);
                        }

                        v.addProperty("puntuacion", Integer.parseInt(partes[4]));
                        v.addProperty("comentario", partes[6]);
                        v.addProperty("fecha", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                                .format(new Date(Long.parseLong(partes[5]))));

                        valoracionesJson.add(v);
                    }
                }
            }

            JsonObject respuesta = crearRespuestaExito("Valoraciones obtenidas");
            respuesta.add("valoraciones", valoracionesJson);
            respuesta.addProperty("total", valoracionesJson.size());
            respuesta.addProperty("promedio", calcularPromedio(valoracionesJson));
            return respuesta;
        } catch (Exception e) {
            return crearRespuestaError("Error al obtener valoraciones: " + e.getMessage());
        }
    }

    private static double calcularPromedio(JsonArray valoraciones) {
        if (valoraciones.size() == 0) return 0.0;

        double suma = 0;
        for (JsonElement element : valoraciones) {
            suma += element.getAsJsonObject().get("puntuacion").getAsInt();
        }
        return suma / valoraciones.size();
    }

    private static JsonObject manejarObtenerValoracion(JsonObject datos) {
        try {
            if (!datos.has("contenidoId")) {
                return crearRespuestaError("Se requiere ID de contenido");
            }

            String contenidoId = datos.get("contenidoId").getAsString();
            Contenido contenido = redSocial.buscarContenido(contenidoId);
            if (contenido == null) {
                return crearRespuestaError("Contenido no encontrado");
            }

            JsonObject respuesta = crearRespuestaExito("Estadísticas de valoración obtenidas");
            respuesta.addProperty("promedio", contenido.obtenerPromedioValoracion());
            respuesta.addProperty("total", contenido.getValoraciones().size());
            return respuesta;
        } catch (Exception e) {
            return crearRespuestaError("Error al obtener valoraciones: " + e.getMessage());
        }
    }
    ///

    // Métodos de manejo de solicitudes específicas
    private static JsonObject manejarRegistro(JsonObject datos) {
        try {
            Estudiante estudiante = gson.fromJson(datos, Estudiante.class);

            if (estudiante.getNombre() == null || estudiante.getCorreo() == null) {
                return crearRespuestaError("Nombre y correo son obligatorios");
            }

            if (redSocial.registrarUsuario(estudiante)) {
                return crearRespuestaExito("Registro exitoso");
            }
            return crearRespuestaError("El correo ya está registrado");
        } catch (Exception e) {
            return crearRespuestaError("Error en registro: " + e.getMessage());
        }
    }

    private static JsonObject manejarLogin(JsonObject datos) {
        try {
            // Validaciones existentes...
            if (!datos.has("correo") || !datos.has("contrasena")) {
                return crearRespuestaError("Falta correo o contraseña");
            }

            String correo = datos.get("correo").getAsString().trim();
            String contrasena = datos.get("contrasena").getAsString();

            if (correo.isEmpty() || contrasena.isEmpty()) {
                return crearRespuestaError("Correo y contraseña son obligatorios");
            }

            if (!correo.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
                return crearRespuestaError("Formato de correo inválido");
            }

            Usuario usuario = redSocial.iniciarSesion(correo, contrasena);
            if (usuario == null) {
                Thread.sleep(200);
                return crearRespuestaError("Credenciales inválidas");
            }

            JsonObject respuesta = crearRespuestaExito("Bienvenido " + usuario.getNombre());
            JsonObject usuarioJson = gson.toJsonTree(usuario).getAsJsonObject();

            // Añadir información de rol
            if (usuario instanceof Moderador) {
                usuarioJson.addProperty("esModerador", true);
                usuarioJson.addProperty("rol", "MODERADOR");
            } else {
                usuarioJson.addProperty("esModerador", false);
                usuarioJson.addProperty("rol", "USUARIO");
            }

            respuesta.add("usuario", usuarioJson);
            respuesta.addProperty("token", generarToken(usuario.getId()));
            return respuesta;
        } catch (Exception e) {
            return crearRespuestaError("Error en login: " + e.getMessage());
        }
    }



    private static JsonObject manejarObtenerContenidosUsuario(JsonObject datos) {
        try {
            if (!datos.has("userId")) {
                return crearRespuestaError("Falta el ID del usuario");
            }

            String userId = datos.get("userId").getAsString();
            List<Contenido> contenidos = redSocial.obtenerContenidosPorUsuario(userId);
            JsonArray contenidosJson = new JsonArray();

            for (Contenido contenido : contenidos) {
                JsonObject contenidoJson = new JsonObject();
                contenidoJson.addProperty("id", contenido.getId());
                contenidoJson.addProperty("titulo", contenido.getTitulo());
                contenidoJson.addProperty("autor", contenido.getAutor());
                contenidoJson.addProperty("tema", contenido.getTema());
                contenidoJson.addProperty("descripcion", contenido.getDescripcion());
                contenidoJson.addProperty("tipo", contenido.getTipo().name());
                contenidoJson.addProperty("fechaCreacion", dateFormat.format(contenido.getFecha()));
                contenidoJson.addProperty("contenido", contenido.getContenido());

                contenidosJson.add(contenidoJson);
            }

            JsonObject respuesta = crearRespuestaExito("Contenidos del usuario obtenidos");
            respuesta.add("contenidos", contenidosJson);
            return respuesta;
        } catch (Exception e) {
            return crearRespuestaError("Error al obtener contenidos del usuario: " + e.getMessage());
        }
    }

    private static JsonObject manejarObtenerSolicitudesUsuario(JsonObject datos) {
        try {
            if (!datos.has("userId")) {
                return crearRespuestaError("Falta el ID del usuario");
            }

            String userId = datos.get("userId").getAsString();
            List<SolicitudAyuda> solicitudes = redSocial.obtenerSolicitudesPorUsuario(userId);
            JsonArray solicitudesJson = new JsonArray();

            for (SolicitudAyuda solicitud : solicitudes) {
                JsonObject solicitudJson = new JsonObject();
                solicitudJson.addProperty("id", solicitud.getId());
                solicitudJson.addProperty("tema", solicitud.getTema());
                solicitudJson.addProperty("descripcion", solicitud.getDescripcion());
                solicitudJson.addProperty("fecha", dateFormat.format(solicitud.getFecha()));
                solicitudJson.addProperty("urgencia", solicitud.getUrgencia().name());
                solicitudJson.addProperty("estado", solicitud.getEstado().name());
                solicitudJson.addProperty("solicitanteId", solicitud.getSolicitanteId());

                solicitudesJson.add(solicitudJson);
            }

            JsonObject respuesta = crearRespuestaExito("Solicitudes del usuario obtenidas");
            respuesta.add("solicitudes", solicitudesJson);
            return respuesta;
        } catch (Exception e) {
            return crearRespuestaError("Error al obtener solicitudes del usuario: " + e.getMessage());
        }
    }
    private static JsonObject manejarObtenerContenidos() {
        try {
            List<Contenido> contenidos = redSocial.obtenerTodosContenidos();
            JsonArray contenidosJson = new JsonArray();

            for (Contenido contenido : contenidos) {
                JsonObject contenidoJson = new JsonObject();
                contenidoJson.addProperty("id", contenido.getId());
                contenidoJson.addProperty("titulo", contenido.getTitulo());
                contenidoJson.addProperty("autor", contenido.getAutor());
                contenidoJson.addProperty("tema", contenido.getTema());
                contenidoJson.addProperty("descripcion", contenido.getDescripcion());
                contenidoJson.addProperty("tipo", contenido.getTipo().name());

                // FIX: Manejo seguro de fechas
                try {
                    if (contenido.getFecha() != null) {
                        contenidoJson.addProperty("fechaCreacion",
                                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(contenido.getFecha()));
                    } else {
                        contenidoJson.addProperty("fechaCreacion",
                                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
                    }
                } catch (Exception e) {
                    contenidoJson.addProperty("fechaCreacion", "Fecha no disponible");
                }

                contenidoJson.addProperty("contenido", contenido.getContenido());

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

            JsonObject respuesta = crearRespuestaExito("Contenidos obtenidos");
            respuesta.add("contenidos", contenidosJson);
            return respuesta;
        } catch (Exception e) {
            return crearRespuestaError("Error al obtener contenidos: " + e.getMessage());
        }
    }

    private static JsonObject manejarObtenerSolicitudes() {
        try {
            List<SolicitudAyuda> solicitudes = redSocial.obtenerTodasSolicitudes();
            JsonArray jsonSolicitudes = new JsonArray();

            for (SolicitudAyuda solicitud : solicitudes) {
                JsonObject jsonSolicitud = new JsonObject();
                jsonSolicitud.addProperty("id", solicitud.getId());
                jsonSolicitud.addProperty("tema", solicitud.getTema());
                jsonSolicitud.addProperty("descripcion", solicitud.getDescripcion());

                // FIX: Manejo seguro de fechas
// FIX: Manejo seguro de fechas
                try {
                    if (solicitud.getFecha() != null) {
                        jsonSolicitud.addProperty("fecha", dateFormat.format(solicitud.getFecha()));
                    } else {
                        jsonSolicitud.addProperty("fecha", dateFormat.format(new Date()));
                    }
                } catch (Exception e) {
                    // Si hay error formateando la fecha, usar fecha actual
                    jsonSolicitud.addProperty("fecha", dateFormat.format(new Date()));
                    System.err.println("Error al formatear fecha: " + e.getMessage());
                }

                jsonSolicitud.addProperty("urgencia", solicitud.getUrgencia().name());
                jsonSolicitud.addProperty("estado", solicitud.getEstado().name());
                jsonSolicitud.addProperty("solicitanteId", solicitud.getSolicitanteId());

                Usuario solicitante = redSocial.buscarUsuario(solicitud.getSolicitanteId());
                jsonSolicitud.addProperty("solicitanteNombre",
                        solicitante != null ? solicitante.getNombre() : "Desconocido");

                jsonSolicitudes.add(jsonSolicitud);
            }

            JsonObject respuesta = crearRespuestaExito("Solicitudes obtenidas");
            respuesta.add("solicitudes", jsonSolicitudes);
            return respuesta;
        } catch (Exception e) {
            return crearRespuestaError("Error al obtener solicitudes: " + e.getMessage());
        }
    }

    private static JsonObject manejarActualizarUsuario(JsonObject datos) {
        try {
            if (!datos.has("id")) {
                return crearRespuestaError("Falta el ID del usuario");
            }

            String id = datos.get("id").getAsString();
            Usuario usuario = redSocial.buscarUsuario(id);
            if (usuario == null) {
                return crearRespuestaError("Usuario no encontrado");
            }

            if (datos.has("nombre")) usuario.setNombre(datos.get("nombre").getAsString());
            if (datos.has("email")) usuario.setCorreo(datos.get("email").getAsString());
            if (datos.has("password")) usuario.setContrasenia(datos.get("password").getAsString());

            if (redSocial.actualizarUsuario(usuario)) {
                JsonObject respuesta = crearRespuestaExito("Datos actualizados");
                respuesta.add("usuario", gson.toJsonTree(usuario));
                return respuesta;
            }
            return crearRespuestaError("No se pudo actualizar el usuario");
        } catch (Exception e) {
            return crearRespuestaError("Error al actualizar usuario: " + e.getMessage());
        }
    }

    private static JsonObject manejarEliminarUsuario(JsonObject datos) {
        try {
            String usuarioId = datos.has("usuarioId") ? datos.get("usuarioId").getAsString() : datos.get("id").getAsString();

            if (redSocial.eliminarUsuario(usuarioId)) {
                return crearRespuestaExito("Cuenta eliminada correctamente");
            }
            return crearRespuestaError("No se pudo eliminar el usuario");
        } catch (Exception e) {
            return crearRespuestaError("Error al eliminar usuario: " + e.getMessage());
        }
    }

    private static JsonObject manejarObtenerUsuario(JsonObject datos) {
        try {
            if (!datos.has("id")) {
                return crearRespuestaError("Falta el ID del usuario");
            }

            String idUsuario = datos.get("id").getAsString();
            Usuario usuario = redSocial.buscarUsuario(idUsuario);
            if (usuario == null) {
                return crearRespuestaError("Usuario no encontrado");
            }

            JsonObject usuarioJson = new JsonObject();
            usuarioJson.addProperty("id", usuario.getId());
            usuarioJson.addProperty("nombre", usuario.getNombre());
            usuarioJson.addProperty("email", usuario.getCorreo());

            JsonObject respuesta = crearRespuestaExito("Usuario obtenido");
            respuesta.add("usuario", usuarioJson);
            return respuesta;
        } catch (Exception e) {
            return crearRespuestaError("Error al obtener usuario: " + e.getMessage());
        }
    }

    private static JsonObject manejarDatosModerador(JsonObject datos) {
        try {
            if (!datos.has("userId")) {
                return crearRespuestaError("Falta el ID del usuario");
            }

            String userId = datos.get("userId").getAsString();
            Usuario usuario = redSocial.buscarUsuario(userId);
            if (usuario == null) {
                return crearRespuestaError("Usuario no encontrado");
            }

            if (!(usuario instanceof Moderador)) {
                return crearRespuestaError("El usuario no tiene permisos de moderador");
            }

            JsonObject datosModerador = new JsonObject();
            datosModerador.addProperty("id", usuario.getId());
            datosModerador.addProperty("nombres", usuario.getNombre());
            datosModerador.addProperty("correo", usuario.getCorreo());
            datosModerador.addProperty("totalUsuarios", redSocial.obtenerTotalUsuarios());
            datosModerador.addProperty("totalContenidos", redSocial.obtenerTotalContenidos());
            datosModerador.addProperty("totalSolicitudes", redSocial.obtenerTotalSolicitudes());

            JsonObject respuesta = crearRespuestaExito("Datos de moderador obtenidos");
            respuesta.add("datosUsuario", datosModerador);
            return respuesta;
        } catch (Exception e) {
            return crearRespuestaError("Error al obtener datos de moderador: " + e.getMessage());
        }
    }

    private static JsonObject manejarDatosPerfil(JsonObject datos) {
        try {
            if (!datos.has("userId") || datos.get("userId").getAsString().isEmpty()) {
                return crearRespuestaError("Falta el ID del usuario");
            }

            String userId = datos.get("userId").getAsString();
            Usuario usuario = redSocial.buscarUsuario(userId);
            if (usuario == null) {
                return crearRespuestaError("Usuario no encontrado");
            }

            JsonObject datosPerfil = new JsonObject();
            datosPerfil.addProperty("id", usuario.getId());
            datosPerfil.addProperty("nombres", usuario.getNombre());
            datosPerfil.addProperty("correo", usuario.getCorreo());

            if (usuario instanceof Estudiante) {
                Estudiante estudiante = (Estudiante) usuario;
                String interesesStr = String.join(", ", estudiante.getIntereses());
                datosPerfil.addProperty("intereses", interesesStr);
            } else {
                datosPerfil.addProperty("intereses", "");
            }

            JsonArray gruposArray = new JsonArray();
            for (String grupo : redSocial.obtenerGruposEstudio(userId)) {
                gruposArray.add(grupo);
            }
            datosPerfil.add("gruposEstudio", gruposArray);

            datosPerfil.addProperty("contenidosPublicados", redSocial.obtenerTotalContenidosUsuario(userId));
            datosPerfil.addProperty("solicitudesPublicadas", redSocial.obtenerTotalSolicitudesUsuario(userId));

            JsonObject respuesta = crearRespuestaExito("Datos de perfil obtenidos");
            respuesta.add("datosUsuario", datosPerfil);
            return respuesta;
        } catch (Exception e) {
            return crearRespuestaError("Error al obtener datos de perfil: " + e.getMessage());
        }
    }

    private static JsonObject manejarActualizarDatosUsuario(JsonObject datos) {
        try {
            if (!datos.has("id")) {
                return crearRespuestaError("Falta el ID del usuario");
            }

            String userId = datos.get("id").getAsString();
            Usuario usuario = redSocial.buscarUsuario(userId);
            if (usuario == null) {
                return crearRespuestaError("Usuario no encontrado");
            }

            if (datos.has("nombres")) usuario.setNombre(datos.get("nombres").getAsString());
            if (datos.has("correo")) usuario.setCorreo(datos.get("correo").getAsString());
            if (datos.has("intereses") && usuario instanceof Estudiante) {
                Estudiante estudiante = (Estudiante) usuario;
                LinkedList<String> intereses = new LinkedList<>(
                        Arrays.asList(datos.get("intereses").getAsString().split("\\s*,\\s*")));
                estudiante.setIntereses(intereses);
            }
            if (datos.has("contrasena")) usuario.setContrasenia(datos.get("contrasena").getAsString());

            if (redSocial.actualizarUsuario(usuario)) {
                JsonObject respuesta = crearRespuestaExito("Datos actualizados correctamente");
                respuesta.add("usuario", gson.toJsonTree(usuario));
                return respuesta;
            }
            return crearRespuestaError("No se pudo actualizar el usuario");
        } catch (Exception e) {
            return crearRespuestaError("Error al actualizar usuario: " + e.getMessage());
        }
    }

    private static JsonObject manejarCrearPublicacion(JsonObject datos) {
        try {
            if (!datos.has("usuarioId") || !datos.has("titulo") ||
                    !datos.has("tipoContenido") || !datos.has("contenido")) {
                return crearRespuestaError("Faltan campos obligatorios");
            }

            String usuarioId = datos.get("usuarioId").getAsString();
            String titulo = datos.get("titulo").getAsString();
            String descripcion = datos.has("descripcion") ? datos.get("descripcion").getAsString() : "";
            String tema = datos.has("tema") ? datos.get("tema").getAsString() : "General";
            TipoContenido tipo = TipoContenido.valueOf(datos.get("tipoContenido").getAsString());
            String contenidoRuta = datos.get("contenido").getAsString();

            if (tipo == TipoContenido.ENLACE && !contenidoRuta.matches("^(https?|ftp)://.*")) {
                return crearRespuestaError("Formato de enlace inválido");
            }

            if (redSocial.crearContenido(usuarioId, titulo, descripcion, tipo, tema, contenidoRuta)) {
                JsonObject publicacionJson = new JsonObject();
                publicacionJson.addProperty("id", usuarioId + "-" + System.currentTimeMillis());
                publicacionJson.addProperty("titulo", titulo);
                publicacionJson.addProperty("tipo", tipo.name());
                publicacionJson.addProperty("tema", tema);
                publicacionJson.addProperty("fecha", LocalDateTime.now().toString());
                publicacionJson.addProperty("contenido", contenidoRuta);

                JsonObject respuesta = crearRespuestaExito("Publicación creada exitosamente");
                respuesta.add("publicacion", publicacionJson);
                return respuesta;
            }
            return crearRespuestaError("No se pudo crear la publicación");
        } catch (Exception e) {
            return crearRespuestaError("Error al crear publicación: " + e.getMessage());
        }
    }

    private static JsonObject manejarCrearSolicitud(JsonObject datos) {
        try {
            if (!datos.has("usuarioId") || !datos.has("titulo") ||
                    !datos.has("tema") || !datos.has("descripcion") ||
                    !datos.has("urgencia")) {
                return crearRespuestaError("Faltan campos obligatorios");
            }

            String usuarioId = datos.get("usuarioId").getAsString();
            String titulo = datos.get("titulo").getAsString();
            String tema = datos.get("tema").getAsString();
            String descripcion = datos.get("descripcion").getAsString();
            Urgencia urgencia = Urgencia.valueOf(datos.get("urgencia").getAsString().toUpperCase());

            Usuario usuario = redSocial.buscarUsuario(usuarioId);
            if (usuario == null) {
                return crearRespuestaError("Usuario no encontrado");
            }

            SolicitudAyuda solicitud = new SolicitudAyuda(
                    null, titulo, descripcion, new Date(), urgencia, usuarioId
            );

            if (redSocial.crearSolicitud(solicitud)) {
                JsonObject solicitudJson = new JsonObject();
                solicitudJson.addProperty("id", solicitud.getId());
                solicitudJson.addProperty("tema", solicitud.getTema());
                solicitudJson.addProperty("descripcion", solicitud.getDescripcion());

                // FIX: Manejo seguro de fechas
                try {
                    if (solicitud.getFecha() != null) {
                        solicitudJson.addProperty("fecha", dateFormat.format(solicitud.getFecha()));
                    } else {
                        solicitudJson.addProperty("fecha", dateFormat.format(new Date()));
                    }
                } catch (Exception e) {
                    solicitudJson.addProperty("fecha", dateFormat.format(new Date()));
                }

                solicitudJson.addProperty("urgencia", solicitud.getUrgencia().name());
                solicitudJson.addProperty("estado", solicitud.getEstado().name());
                solicitudJson.addProperty("solicitanteId", solicitud.getSolicitanteId());

                JsonObject respuesta = crearRespuestaExito("Solicitud creada exitosamente");
                respuesta.add("solicitud", solicitudJson);
                return respuesta;
            }
            return crearRespuestaError("No se pudo crear la solicitud");
        } catch (Exception e) {
            return crearRespuestaError("Error al crear solicitud: " + e.getMessage());
        }
    }

    private static JsonObject manejarContenidosCompletos(JsonObject datos) {
        try {
            if (!datos.has("userId") || datos.get("userId").getAsString().isEmpty()) {
                return crearRespuestaError("Se requiere un ID de usuario válido");
            }

            String userId = datos.get("userId").getAsString();
            Usuario usuario = redSocial.buscarUsuario(userId);
            if (usuario == null) {
                return crearRespuestaError("Usuario no encontrado");
            }

            List<Contenido> contenidos = redSocial.obtenerTodosContenidos();
            if (contenidos == null) {
                contenidos = new ArrayList<>();
            }

            JsonArray contenidosJson = new JsonArray();
            for (Contenido contenido : contenidos) {
                try {
                    JsonObject contenidoJson = new JsonObject();
                    contenidoJson.addProperty("id", contenido.getId() != null ? contenido.getId() : "");
                    contenidoJson.addProperty("titulo", contenido.getTitulo() != null ? contenido.getTitulo() : "");
                    contenidoJson.addProperty("autor", contenido.getAutor() != null ? contenido.getAutor() : "");
                    contenidoJson.addProperty("tema", contenido.getTema() != null ? contenido.getTema() : "General");
                    contenidoJson.addProperty("descripcion", contenido.getDescripcion() != null ? contenido.getDescripcion() : "");
                    contenidoJson.addProperty("tipo", contenido.getTipo() != null ? contenido.getTipo().toString() : "TEXTO");

                    // FIX: Manejo seguro de fechas
                    try {
                        if (contenido.getFecha() != null) {
                            contenidoJson.addProperty("fecha", dateFormat.format(contenido.getFecha()));
                        } else {
                            contenidoJson.addProperty("fecha", "");
                        }
                    } catch (Exception e) {
                        contenidoJson.addProperty("fecha", "");
                        System.err.println("Error al formatear fecha: " + e.getMessage());
                    }

                    contenidoJson.addProperty("contenido", contenido.getContenido() != null ? contenido.getContenido() : "");

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
                    contenidoJson.addProperty("promedioValoraciones", contenido.obtenerPromedioValoracion());
                    contenidosJson.add(contenidoJson);
                } catch (Exception e) {
                    System.err.println("Error procesando contenido individual: " + e.getMessage());
                    continue;
                }
            }

            JsonObject metadata = new JsonObject();
            metadata.addProperty("usuarioId", userId);
            metadata.addProperty("usuarioNombre", usuario.getNombre());
            metadata.addProperty("fechaConsulta", dateFormat.format(new Date()));

            JsonObject respuesta = crearRespuestaExito("Contenidos completos obtenidos");
            respuesta.addProperty("totalContenidos", contenidos.size());
            respuesta.add("contenidos", contenidosJson);
            respuesta.add("metadata", metadata);
            return respuesta;
        } catch (Exception e) {
            return crearRespuestaError("Error al obtener contenidos completos: " + e.getMessage());
        }
    }

    // Métodos auxiliares
    private static JsonObject crearRespuestaExito(String mensaje) {
        JsonObject respuesta = new JsonObject();
        respuesta.addProperty("exito", true);
        respuesta.addProperty("mensaje", mensaje);
        return respuesta;
    }

    private static JsonObject crearRespuestaError(String mensaje) {
        JsonObject respuesta = new JsonObject();
        respuesta.addProperty("exito", false);
        respuesta.addProperty("mensaje", mensaje);
        return respuesta;
    }



    private static String generarToken(String userId) {
        long tiempoExpiracion = System.currentTimeMillis() + 3600000; // 1 hora
        String datosToken = userId + "|" + tiempoExpiracion;
        return Base64.getEncoder().encodeToString(datosToken.getBytes());
    }

    private static class LocalDateTimeAdapter implements JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {
        private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        @Override
        public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
            return LocalDateTime.parse(json.getAsString(), formatter);
        }

        @Override
        public JsonElement serialize(LocalDateTime src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(formatter.format(src));
        }
    }



}