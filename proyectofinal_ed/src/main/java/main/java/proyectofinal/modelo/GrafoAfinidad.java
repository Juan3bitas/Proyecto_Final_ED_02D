package main.java.proyectofinal.modelo;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Grafo no dirigido que representa relaciones de afinidad entre estudiantes
 * basadas en:
 * - Grupos de estudio compartidos
 * - Valoraciones similares de contenidos
 */
public class GrafoAfinidad {
    private static final Logger LOGGER = Logger.getLogger(GrafoAfinidad.class.getName());
    private final Map<Estudiante, Map<Estudiante, Integer>> grafo;

    // Factores de ponderación configurables
    private final int PESO_BASE = 1;
    private final int PESO_GRUPO_ESTUDIO = 5;
    private final int PESO_VALORACION_SIMILAR = 3;
    private final double UMBRAL_VALORACION_SIMILAR = 0.5;
    private final int PESO_MAXIMO = 10;

    public GrafoAfinidad() {
        this.grafo = new HashMap<>();
    }

    /**
     * Agrega un estudiante al grafo si no existe
     * @param estudiante Estudiante a agregar
     */
    public void agregarEstudiante(Estudiante estudiante) {
        Objects.requireNonNull(estudiante, "El estudiante no puede ser nulo");
        if (!grafo.containsKey(estudiante)) {
            grafo.put(estudiante, new HashMap<>());
            LOGGER.log(Level.FINE, "Estudiante agregado: ID={0}, Nombre={1}",
                    new Object[]{estudiante.getId(), estudiante.getNombre()});
        }
    }

    /**
     * Elimina un estudiante del grafo y todas sus conexiones
     * @param estudiante Estudiante a eliminar
     */
    public void removerEstudiante(Estudiante estudiante) {
        validarEstudiante(estudiante);
        grafo.remove(estudiante);
        grafo.values().forEach(adyacentes -> adyacentes.remove(estudiante));
        LOGGER.log(Level.FINE, "Estudiante removido: ID={0}", estudiante.getId());
    }

    /**
     * Calcula y actualiza todas las afinidades automáticamente
     */
    public void calcularAfinidades() {
        LOGGER.info("Iniciando cálculo de afinidades...");
        List<Estudiante> estudiantes = new ArrayList<>(grafo.keySet());
        int totalConexiones = 0;

        // Limpiar conexiones existentes primero
        grafo.values().forEach(Map::clear);

        // Calcular nuevas afinidades
        for (int i = 0; i < estudiantes.size(); i++) {
            Estudiante e1 = estudiantes.get(i);

            for (int j = i + 1; j < estudiantes.size(); j++) {
                Estudiante e2 = estudiantes.get(j);

                // 1. Verificar grupos en común
                Set<String> gruposComunes = new HashSet<>(e1.getGruposEstudio());
                gruposComunes.retainAll(e2.getGruposEstudio());
                int pesoGrupos = gruposComunes.size() * PESO_GRUPO_ESTUDIO;

                // 2. Verificar valoraciones similares
                double similitud = calcularSimilitudValoraciones(e1, e2);
                int pesoValoraciones = (int)(similitud * PESO_VALORACION_SIMILAR);

                // Peso total
                int pesoTotal = PESO_BASE + pesoGrupos + pesoValoraciones;

                if (pesoTotal > PESO_BASE) {
                    establecerAfinidad(e1, e2, pesoTotal);
                    totalConexiones++;
                }
            }
        }
        LOGGER.log(Level.INFO, "Cálculo de afinidades completado. Estudiantes: {0}, Conexiones: {1}",
                new Object[]{estudiantes.size(), totalConexiones});
    }

    /**
     * Establece una relación de afinidad con peso específico
     */
    public void establecerAfinidad(Estudiante e1, Estudiante e2, int peso) {
        validarEstudiante(e1);
        validarEstudiante(e2);

        peso = Math.min(peso, PESO_MAXIMO); // Asegurar no exceder peso máximo
        grafo.get(e1).put(e2, peso);
        grafo.get(e2).put(e1, peso);

        LOGGER.log(Level.FINER, "Afinidad establecida: {0} <-> {1} (Peso: {2})",
                new Object[]{e1.getId(), e2.getId(), peso});
    }

    /**
     * Calcula el peso de afinidad entre dos estudiantes
     */
    private int calcularPesoAfinidad(Estudiante e1, Estudiante e2) {
        int peso = PESO_BASE;

        // 1. Grupos de estudio en común
        Set<String> gruposComunes = new HashSet<>(e1.getGruposEstudio());
        gruposComunes.retainAll(e2.getGruposEstudio());
        peso += gruposComunes.size() * PESO_GRUPO_ESTUDIO;

        // 2. Valoraciones similares de contenidos
        peso += calcularSimilitudValoraciones(e1, e2) * PESO_VALORACION_SIMILAR;

        return peso;
    }

    /**
     * Calcula similitud de valoraciones usando distancia coseno (0-1)
     */
    private double calcularSimilitudValoraciones(Estudiante e1, Estudiante e2) {
        Map<String, Double> valoracionesE1 = e1.getValoracionesContenidos();
        Map<String, Double> valoracionesE2 = e2.getValoracionesContenidos();

        // Encuentra contenidos comunes valorados por ambos
        Set<String> contenidosComunes = new HashSet<>(valoracionesE1.keySet());
        contenidosComunes.retainAll(valoracionesE2.keySet());

        if (contenidosComunes.isEmpty()) return 0;

        double productoPunto = 0;
        double magnitudE1 = 0;
        double magnitudE2 = 0;

        for (String contenidoId : contenidosComunes) {
            double v1 = valoracionesE1.get(contenidoId);
            double v2 = valoracionesE2.get(contenidoId);
            productoPunto += v1 * v2;
            magnitudE1 += v1 * v1;
            magnitudE2 += v2 * v2;
        }

        // Evitar división por cero
        if (magnitudE1 == 0 || magnitudE2 == 0) return 0;

        double similitud = productoPunto / (Math.sqrt(magnitudE1) * Math.sqrt(magnitudE2));
        return similitud > UMBRAL_VALORACION_SIMILAR ? similitud : 0;
    }

    /**
     * Obtiene recomendaciones ordenadas por afinidad
     * @param estudiante Estudiante para el que se buscan recomendaciones
     * @param limite Número máximo de recomendaciones a devolver
     * @return Lista ordenada de recomendaciones
     */
    public List<Estudiante> obtenerRecomendaciones(Estudiante estudiante, int limite) {
        validarEstudiante(estudiante);

        return grafo.get(estudiante).entrySet().stream()
                .filter(entry -> !entry.getKey().equals(estudiante))
                .sorted(Map.Entry.<Estudiante, Integer>comparingByValue().reversed())
                .limit(limite)
                .peek(entry -> LOGGER.log(Level.FINEST,
                        "Afinidad con {0}: {1}",
                        new Object[]{entry.getKey().getId(), entry.getValue()}))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene el peso de afinidad entre dos estudiantes
     */
    public int obtenerPesoAfinidad(Estudiante e1, Estudiante e2) {
        validarEstudiante(e1);
        validarEstudiante(e2);
        return grafo.get(e1).getOrDefault(e2, 0);
    }

    /**
     * Verifica si existe una relación de afinidad
     */
    public boolean existeAfinidad(Estudiante e1, Estudiante e2) {
        validarEstudiante(e1);
        validarEstudiante(e2);
        return grafo.get(e1).containsKey(e2);
    }

    /**
     * Obtiene estudiantes adyacentes (conectados directamente)
     */
    public List<Estudiante> obtenerAdyacentes(Estudiante estudiante) {
        validarEstudiante(estudiante);
        return new ArrayList<>(grafo.get(estudiante).keySet());
    }

    /**
     * Muestra el estado actual del grafo en los logs
     */
    public void mostrarEstado() {
        LOGGER.info("=== ESTADO DEL GRAFO ===");
        LOGGER.info("Total estudiantes: " + grafo.size());
        LOGGER.info("Total conexiones: " + obtenerTotalConexiones());

        grafo.forEach((estudiante, conexiones) -> {
            LOGGER.info(String.format("Estudiante %s (%s) tiene %d conexiones:",
                    estudiante.getId(), estudiante.getNombre(), conexiones.size()));

            conexiones.entrySet().stream()
                    .sorted(Map.Entry.<Estudiante, Integer>comparingByValue().reversed())
                    .forEach(entry -> LOGGER.info(String.format(
                            "  -> %s (%s) - Peso: %d, Grupos comunes: %d, Similitud: %.2f",
                            entry.getKey().getId(),
                            entry.getKey().getNombre(),
                            entry.getValue(),
                            contarGruposComunes(estudiante, entry.getKey()),
                            calcularSimilitudValoraciones(estudiante, entry.getKey())
                    )));
        });
    }

    /**
     * Obtiene estadísticas resumidas del grafo
     */
    public String obtenerEstadisticas() {
        long totalConexiones = obtenerTotalConexiones();
        double densidad = grafo.isEmpty() ? 0 :
                (2.0 * totalConexiones) / (grafo.size() * (grafo.size() - 1));

        return String.format(
                "Estudiantes: %d | Conexiones: %d | Densidad: %.2f | Peso promedio: %.2f",
                grafo.size(),
                totalConexiones,
                densidad,
                grafo.values().stream()
                        .flatMap(m -> m.values().stream())
                        .mapToInt(Integer::intValue)
                        .average()
                        .orElse(0)
        );
    }

    /**
     * Encuentra el camino más corto entre dos estudiantes usando BFS
     */
    public List<Estudiante> encontrarCaminoMasCorto(Estudiante origen, Estudiante destino) {
        validarEstudiante(origen);
        validarEstudiante(destino);

        Queue<Estudiante> cola = new LinkedList<>();
        Map<Estudiante, Estudiante> padres = new HashMap<>();
        Set<Estudiante> visitados = new HashSet<>();

        cola.add(origen);
        visitados.add(origen);

        while (!cola.isEmpty()) {
            Estudiante actual = cola.poll();
            if (actual.equals(destino)) {
                return reconstruirCamino(padres, destino);
            }
            for (Estudiante vecino : grafo.get(actual).keySet()) {
                if (!visitados.contains(vecino)) {
                    visitados.add(vecino);
                    padres.put(vecino, actual);
                    cola.add(vecino);
                }
            }
        }
        return Collections.emptyList();
    }

    /**
     * Detecta comunidades de estudiantes usando componentes conexas (DFS)
     */
    public List<List<Estudiante>> detectarComunidades() {
        Set<Estudiante> visitados = new HashSet<>();
        List<List<Estudiante>> comunidades = new ArrayList<>();

        for (Estudiante estudiante : grafo.keySet()) {
            if (!visitados.contains(estudiante)) {
                List<Estudiante> comunidad = new ArrayList<>();
                dfs(estudiante, visitados, comunidad);
                comunidades.add(comunidad);
                LOGGER.log(Level.INFO, "Comunidad detectada: {0} estudiantes", comunidad.size());
            }
        }
        return comunidades;
    }

    // Métodos auxiliares privados
    private void validarEstudiante(Estudiante estudiante) {
        if (!grafo.containsKey(estudiante)) {
            LOGGER.log(Level.SEVERE, "Estudiante no encontrado: ID={0}", estudiante.getId());
            throw new IllegalArgumentException("El estudiante " + estudiante.getId() + " no existe en el grafo");
        }
    }

    private long contarGruposComunes(Estudiante e1, Estudiante e2) {
        return e1.getGruposEstudio().stream()
                .filter(grupo -> e2.getGruposEstudio().contains(grupo))
                .count();
    }

    private List<Estudiante> reconstruirCamino(Map<Estudiante, Estudiante> padres, Estudiante destino) {
        LinkedList<Estudiante> camino = new LinkedList<>();
        Estudiante actual = destino;
        while (actual != null) {
            camino.addFirst(actual);
            actual = padres.get(actual);
        }
        return camino;
    }

    private void dfs(Estudiante actual, Set<Estudiante> visitados, List<Estudiante> comunidad) {
        visitados.add(actual);
        comunidad.add(actual);
        grafo.get(actual).keySet().stream()
                .filter(vecino -> !visitados.contains(vecino))
                .forEach(vecino -> dfs(vecino, visitados, comunidad));
    }

    // Métodos de consulta
    public int obtenerTotalEstudiantes() {
        return grafo.size();
    }

    public int obtenerTotalConexiones() {
        return grafo.values().stream()
                .mapToInt(Map::size)
                .sum() / 2; // Dividido por 2 porque es grafo no dirigido
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Grafo de Afinidad (" + obtenerEstadisticas() + "):\n");
        grafo.forEach((estudiante, conexiones) -> {
            sb.append(String.format("%s (%s):\n", estudiante.getId(), estudiante.getNombre()));
            conexiones.forEach((vecino, peso) ->
                    sb.append(String.format("  -> %s (%s) - Peso: %d\n",
                            vecino.getId(), vecino.getNombre(), peso))
            );
        });
        return sb.toString();
    }
}