package main.java.proyectofinal.modelo;

import java.util.Comparator;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Clase que representa un árbol binario de búsqueda para almacenar contenidos.
 * Permite insertar, eliminar, buscar y modificar contenidos basados en diferentes criterios de orden.
 */

public class ArbolContenidos {
    NodoContenido raiz;
    private final Comparator<Contenido> comparador;


    /**
     * Constructor que inicializa el árbol con un criterio de orden específico.
     *
     * @param criterio El criterio de orden para los contenidos (TEMA, AUTOR, FECHA).
     * @throws IllegalArgumentException Si el criterio es nulo o no soportado.
     */

    public ArbolContenidos(CriterioOrden criterio) {
        this.comparador = crearComparador(criterio);
    }

    /**
     * Inicializa el árbol con una lista de contenidos.
     * Si la lista es nula o vacía, no se realiza ninguna acción.
     *
     * @param contenidos Lista de contenidos a insertar en el árbol.
     */

    public void inicializarConLista(List<Contenido> contenidos) {
        if (contenidos != null && !contenidos.isEmpty()) {
            contenidos.forEach(this::insertar);
        }
    }

    /**
     * Crea un comparador basado en el criterio de orden especificado.
     *
     * @param criterio El criterio de orden para los contenidos (TEMA, AUTOR, FECHA).
     * @return Un comparador que compara contenidos según el criterio especificado.
     * @throws IllegalArgumentException Si el criterio es nulo o no soportado.
     */

    private Comparator<Contenido> crearComparador(CriterioOrden criterio) {
        Objects.requireNonNull(criterio, "El criterio de orden no puede ser nulo");
        
        switch (criterio) {
            case TEMA:
                return Comparator.comparing(Contenido::getTema, String.CASE_INSENSITIVE_ORDER);
            case AUTOR:
                return Comparator.comparing(Contenido::getAutor, String.CASE_INSENSITIVE_ORDER);
            case FECHA:
                return Comparator.comparing(Contenido::getFecha);
            default:
                throw new IllegalArgumentException("Criterio no soportado: " + criterio);
        }
    }

    /**
     * Elimina un contenido del árbol.
     * Si el contenido es nulo, lanza una excepción.
     *
     * @param contenido El contenido a eliminar.
     * @return true si la eliminación fue exitosa, false si el contenido no estaba presente.
     */

    public boolean eliminar(Contenido contenido) {
        if (contenido == null) {
            throw new IllegalArgumentException("El contenido a eliminar no puede ser nulo");
        }

        System.out.println("[DEBUG] Intentando eliminar contenido ID: " + contenido.getId());

        int tamañoAntes = contarNodos();

        raiz = eliminarRec(raiz, contenido);

        int tamañoDespues = contarNodos();
        boolean eliminadoExitoso = tamañoDespues < tamañoAntes;

        System.out.println("[DEBUG] Eliminación " + (eliminadoExitoso ? "exitosa" : "fallida"));
        return eliminadoExitoso;
    }

    /**
     * Cuenta el número de nodos en el árbol.
     *
     * @return El número total de nodos en el árbol.
     */

    private int contarNodos() {
        return tamañoRec(raiz);
    }

    /**
     * Elimina un contenido del árbol de forma recursiva.
     *
     * @param nodo El nodo actual del árbol.
     * @param contenido El contenido a eliminar.
     * @return El nodo actualizado después de la eliminación.
     */

    private NodoContenido eliminarRec(NodoContenido nodo, Contenido contenido) {
        if (nodo == null) {
            return null;
        }

        int comparacion = contenido.getId().compareTo(nodo.contenido.getId());

        if (comparacion < 0) {
            nodo.izquierdo = eliminarRec(nodo.izquierdo, contenido);
        } else if (comparacion > 0) {
            nodo.derecho = eliminarRec(nodo.derecho, contenido);
        } else {

            if (nodo.izquierdo == null) {
                return nodo.derecho;
            } else if (nodo.derecho == null) {
                return nodo.izquierdo;
            }


            NodoContenido sucesor = encontrarMinimo(nodo.derecho);

            nodo.contenido = sucesor.contenido;

            nodo.derecho = eliminarRec(nodo.derecho, sucesor.contenido);
        }

        return nodo;
    }

    /**
     * Encuentra el nodo con el valor mínimo en un subárbol.
     *
     * @param nodo El nodo raíz del subárbol.
     * @return El nodo con el valor mínimo.
     */

    private NodoContenido encontrarMinimo(NodoContenido nodo) {
        while (nodo.izquierdo != null) {
            nodo = nodo.izquierdo;
        }
        return nodo;
    }

    /**
     * Busca un contenido por su ID.
     * Si el ID es nulo, lanza una excepción.
     *
     * @param contenidoId El ID del contenido a buscar.
     * @return El contenido encontrado o null si no se encuentra.
     */

    public Contenido buscarPorId(String contenidoId) {
        System.out.println("[DEBUG] Iniciando búsqueda para ID: " + contenidoId);
        Contenido resultado = buscarPorIdRec(raiz, contenidoId);
        System.out.println("[DEBUG] Resultado búsqueda ID " + contenidoId + ": " +
                (resultado != null ? "Encontrado" : "No encontrado"));
        return resultado;
    }

    /**
     * Metodo recursivo para buscar un contenido por su ID.
     *
     * @param nodo El nodo actual del árbol.
     * @param contenidoId El ID del contenido a buscar.
     * @return El contenido encontrado o null si no se encuentra.
     */

    private Contenido buscarPorIdRec(NodoContenido nodo, String contenidoId) {
        if (nodo == null) {
            return null;
        }

        System.out.println("[DEBUG] Visitando nodo con ID: " + nodo.contenido.getId());

        if (nodo.contenido.getId().equals(contenidoId)) {
            return nodo.contenido;
        }

        Contenido izquierdo = buscarPorIdRec(nodo.izquierdo, contenidoId);
        if (izquierdo != null) {
            return izquierdo;
        }

        return buscarPorIdRec(nodo.derecho, contenidoId);
    }

    /**
     * Modifica un contenido existente en el árbol.
     * Si el contenido actualizado es nulo o no se encuentra, lanza una excepción.
     *
     * @param contenidoActualizado El contenido con los datos actualizados.
     */

    public void modificar(Contenido contenidoActualizado) {
        Objects.requireNonNull(contenidoActualizado, "El contenido actualizado no puede ser nulo");
        Contenido contenidoExistente = buscarPorId(contenidoActualizado.getId());
        if (contenidoExistente != null) {
            eliminar(contenidoExistente);
            insertar(contenidoActualizado);
        } else {
            throw new IllegalArgumentException("Contenido con ID " + contenidoActualizado.getId() + " no encontrado");
        }
    }

    /**
     * Clase interna que representa un nodo del árbol.
     * Contiene el contenido y referencias a los nodos izquierdo y derecho.
     */

    private static class NodoContenido {
        Contenido contenido;
        NodoContenido izquierdo;
        NodoContenido derecho;

        NodoContenido(Contenido contenido) {
            this.contenido = Objects.requireNonNull(contenido, "El contenido no puede ser nulo");
        }
    }


    /**
     * Inserta un nuevo contenido en el árbol.
     * Si el contenido es nulo, lanza una excepción.
     *
     * @param contenido El contenido a insertar.
     */

    public void insertar(Contenido contenido) {
        raiz = insertarRec(raiz, contenido);
    }

    /**
     * Inserta un contenido de forma recursiva en el árbol.
     *
     * @param nodo El nodo actual del árbol.
     * @param contenido El contenido a insertar.
     * @return El nodo actualizado después de la inserción.
     */
    private NodoContenido insertarRec(NodoContenido nodo, Contenido contenido) {
        if (nodo == null) {
            return new NodoContenido(contenido);
        }

        if (comparador.compare(contenido, nodo.contenido) < 0) {
            nodo.izquierdo = insertarRec(nodo.izquierdo, contenido);
        } else if (comparador.compare(contenido, nodo.contenido) > 0) {
            nodo.derecho = insertarRec(nodo.derecho, contenido);
        }

        return nodo;
    }


    /**
     * Busca contenidos por tema.
     * Si el tema es nulo o vacío, devuelve una lista vacía.
     *
     * @param tema El tema a buscar.
     * @return Una lista de contenidos que coinciden con el tema.
     */

    public List<Contenido> buscarPorTema(String tema) {
        List<Contenido> resultados = new ArrayList<>();
        buscarPorTemaRec(raiz, tema.toLowerCase(), resultados);
        resultados.sort(comparador); // Ordena según el criterio del árbol
        return resultados;
    }

    /**
     * Método recursivo para buscar contenidos por tema.
     *
     * @param nodo El nodo actual del árbol.
     * @param tema El tema a buscar.
     * @param resultados La lista donde se almacenan los resultados encontrados.
     */

    private void buscarPorTemaRec(NodoContenido nodo, String tema, List<Contenido> resultados) {
        if (nodo == null) return;

        if (nodo.contenido.getTema().toLowerCase().contains(tema)) {
            resultados.add(nodo.contenido);
        }

        buscarPorTemaRec(nodo.izquierdo, tema, resultados);
        buscarPorTemaRec(nodo.derecho, tema, resultados);
    }

    /**
     * Busca contenidos por autor.
     * Si el autor es nulo o vacío, devuelve una lista vacía.
     *
     * @param autor El autor a buscar.
     * @return Una lista de contenidos que coinciden con el autor.
     */

    public List<Contenido> buscarPorAutor(String autor) {
        List<Contenido> resultados = new ArrayList<>();
        buscarPorAutorRec(raiz, autor.toLowerCase(), resultados);
        resultados.sort(comparador);
        return resultados;
    }

    /**
     * Metodo recursivo para buscar contenidos por autor.
     *
     * @param nodo El nodo actual del árbol.
     * @param autor El autor a buscar.
     * @param resultados La lista donde se almacenan los resultados encontrados.
     */

    private void buscarPorAutorRec(NodoContenido nodo, String autor, List<Contenido> resultados) {
        if (nodo == null) return;

        if (nodo.contenido.getAutor().toLowerCase().contains(autor)) {
            resultados.add(nodo.contenido);
        }

        buscarPorAutorRec(nodo.izquierdo, autor, resultados);
        buscarPorAutorRec(nodo.derecho, autor, resultados);
    }


    /**
     * Obtiene todos los contenidos en orden (inorden).
     * Si la lista es nula, se inicializa una nueva lista.
     *
     * @param contenidos Lista donde se almacenarán los contenidos en orden.
     * @return Una lista de contenidos ordenados.
     */

    public List<Contenido> obtenerTodosEnOrden(List<Contenido> contenidos) {
        if (contenidos == null) {
            contenidos = new ArrayList<>();
        }
        inorden(raiz, contenidos);
        return contenidos;
    }

    /**
     * Metodo recursivo para recorrer el árbol en orden (inorden).
     *
     * @param nodo El nodo actual del árbol.
     * @param contenidos La lista donde se almacenan los contenidos en orden.
     */

    private void inorden(NodoContenido nodo, List<Contenido> contenidos) {
        if (nodo != null) {
            inorden(nodo.izquierdo, contenidos);
            contenidos.add(nodo.contenido);
            inorden(nodo.derecho, contenidos);
        }
    }


    /**
     * Verifica si el árbol está vacío.
     *
     * @return true si el árbol no contiene nodos, false en caso contrario.
     */

    public boolean estaVacio() {
        return raiz == null;
    }

    /**
     * Obtiene el tamaño del árbol (número de nodos).
     *
     * @return El número total de nodos en el árbol.
     */

    public int tamaño() {
        return tamañoRec(raiz);
    }

    /**
     * Método recursivo para calcular el tamaño del árbol.
     *
     * @param nodo El nodo actual del árbol.
     * @return El número de nodos en el subárbol.
     */

    private int tamañoRec(NodoContenido nodo) {
        if (nodo == null) return 0;
        return 1 + tamañoRec(nodo.izquierdo) + tamañoRec(nodo.derecho);
    }
}