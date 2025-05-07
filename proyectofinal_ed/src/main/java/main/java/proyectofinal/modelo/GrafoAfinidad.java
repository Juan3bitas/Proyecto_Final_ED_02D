package main.java.proyectofinal.modelo;

import java.util.*;

/**
 * Grafo no dirigido que representa las relaciones de afinidad entre estudiantes
 * basadas en intereses comunes, grupos compartidos o valoraciones similares.
 * - Los nodos son estudiantes.
 * - Las aristas tienen pesos (mayor peso = mayor afinidad).
 */
public class GrafoAfinidad {
    private final Map<Estudiante, Map<Estudiante, Integer>> grafo;

    public GrafoAfinidad() {
        this.grafo = new HashMap<>();
    }

    public void agregarNodo(Estudiante estudiante) {
        if (!grafo.containsKey(estudiante)) {
            grafo.put(estudiante, new HashMap<>());
        }
    }

    public void removerNodo(Estudiante estudiante) {
        grafo.remove(estudiante);
        grafo.values().forEach(adyacentes -> adyacentes.remove(estudiante));
    }

    public void agregarArista(Estudiante estudiante1, Estudiante estudiante2, int peso) {
        validarEstudiante(estudiante1);
        validarEstudiante(estudiante2);
        grafo.get(estudiante1).put(estudiante2, peso);
        grafo.get(estudiante2).put(estudiante1, peso); // Grafo no dirigido
    }

    public void actualizarPesoArista(Estudiante estudiante1, Estudiante estudiante2, int nuevoPeso) {
        if (existeArista(estudiante1, estudiante2)) {
            agregarArista(estudiante1, estudiante2, nuevoPeso);
        }
    }

    public boolean existeArista(Estudiante estudiante1, Estudiante estudiante2) {
        return grafo.getOrDefault(estudiante1, Collections.emptyMap()).containsKey(estudiante2);
    }

    public List<Estudiante> obtenerAdyacentes(Estudiante estudiante) {
        return new ArrayList<>(grafo.getOrDefault(estudiante, Collections.emptyMap()).keySet());
    }

    public int obtenerPesoArista(Estudiante estudiante1, Estudiante estudiante2) {
        return grafo.getOrDefault(estudiante1, Collections.emptyMap()).getOrDefault(estudiante2, 0);
    }

    public List<Estudiante> obtenerRecomendaciones(Estudiante estudiante) {
        List<Estudiante> recomendaciones = new ArrayList<>();
        Map<Estudiante, Integer> afinidades = grafo.getOrDefault(estudiante, Collections.emptyMap());

        afinidades.entrySet().stream()
            .sorted(Map.Entry.<Estudiante, Integer>comparingByValue().reversed())
            .forEach(entry -> recomendaciones.add(entry.getKey()));

        return recomendaciones;
    }

    public List<Estudiante> encontrarCaminoMasCorto(Estudiante origen, Estudiante destino) {
        // Implementación con BFS 
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
            for (Estudiante vecino : obtenerAdyacentes(actual)) {
                if (!visitados.contains(vecino)) {
                    visitados.add(vecino);
                    padres.put(vecino, actual);
                    cola.add(vecino);
                }
            }
        }
        return Collections.emptyList(); // No hay camino
    }

    public List<List<Estudiante>> detectarComunidades() {
        // Algoritmo: Componentes conexas (DFS)
        Set<Estudiante> visitados = new HashSet<>();
        List<List<Estudiante>> comunidades = new ArrayList<>();

        for (Estudiante estudiante : grafo.keySet()) {
            if (!visitados.contains(estudiante)) {
                List<Estudiante> comunidad = new ArrayList<>();
                dfs(estudiante, visitados, comunidad);
                comunidades.add(comunidad);
            }
        }
        return comunidades;
    }

    private void validarEstudiante(Estudiante estudiante) {
        if (!grafo.containsKey(estudiante)) {
            throw new IllegalArgumentException("El estudiante no existe en el grafo");
        }
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

    private void dfs(Estudiante estudiante, Set<Estudiante> visitados, List<Estudiante> comunidad) {
        visitados.add(estudiante);
        comunidad.add(estudiante);
        for (Estudiante vecino : obtenerAdyacentes(estudiante)) {
            if (!visitados.contains(vecino)) {
                dfs(vecino, visitados, comunidad);
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        grafo.forEach((estudiante, adyacentes) -> {
            sb.append(estudiante.getNombre()).append(" -> ");
            adyacentes.forEach((vecino, peso) -> 
                sb.append(String.format("%s (Peso: %d), ", vecino.getNombre(), peso))
            );
            sb.append("\n");
        });
        return sb.toString();
    }
}