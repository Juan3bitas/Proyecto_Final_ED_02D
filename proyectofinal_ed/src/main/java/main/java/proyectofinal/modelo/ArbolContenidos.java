package main.java.proyectofinal.modelo;

import java.util.Comparator;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ArbolContenidos {
    NodoContenido raiz;
    private final Comparator<Contenido> comparador;


    public ArbolContenidos(CriterioOrden criterio) {
        this.comparador = crearComparador(criterio);
    }

    public void inicializarConLista(List<Contenido> contenidos) {
        if (contenidos != null && !contenidos.isEmpty()) {
            contenidos.forEach(this::insertar);
        }
    }

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

    public boolean eliminar(Contenido contenido) {
        if (contenido == null) {
            throw new IllegalArgumentException("El contenido a eliminar no puede ser nulo");
        }

        System.out.println("[DEBUG] Intentando eliminar contenido ID: " + contenido.getId());

        // Guardamos el tamaño antes para verificar si cambió
        int tamañoAntes = contarNodos();

        raiz = eliminarRec(raiz, contenido);

        int tamañoDespues = contarNodos();
        boolean eliminadoExitoso = tamañoDespues < tamañoAntes;

        System.out.println("[DEBUG] Eliminación " + (eliminadoExitoso ? "exitosa" : "fallida"));
        return eliminadoExitoso;
    }

    private int contarNodos() {
        return tamañoRec(raiz);
    }

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
            // Nodo a eliminar encontrado

            // Caso 1: Nodo sin hijos o con un solo hijo
            if (nodo.izquierdo == null) {
                return nodo.derecho;
            } else if (nodo.derecho == null) {
                return nodo.izquierdo;
            }

            // Caso 2: Nodo con dos hijos
            // Encontrar el sucesor in-order (mínimo en el subárbol derecho)
            NodoContenido sucesor = encontrarMinimo(nodo.derecho);

            // Copiar los datos del sucesor
            nodo.contenido = sucesor.contenido;

            // Eliminar el sucesor
            nodo.derecho = eliminarRec(nodo.derecho, sucesor.contenido);
        }

        return nodo;
    }

    private NodoContenido encontrarMinimo(NodoContenido nodo) {
        while (nodo.izquierdo != null) {
            nodo = nodo.izquierdo;
        }
        return nodo;
    }

    public Contenido buscarPorId(String contenidoId) {
        System.out.println("[DEBUG] Iniciando búsqueda para ID: " + contenidoId);
        Contenido resultado = buscarPorIdRec(raiz, contenidoId);
        System.out.println("[DEBUG] Resultado búsqueda ID " + contenidoId + ": " +
                (resultado != null ? "Encontrado" : "No encontrado"));
        return resultado;
    }

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


    private static class NodoContenido {
        Contenido contenido;
        NodoContenido izquierdo;
        NodoContenido derecho;

        NodoContenido(Contenido contenido) {
            this.contenido = Objects.requireNonNull(contenido, "El contenido no puede ser nulo");
        }
    }


    public void insertar(Contenido contenido) {
        raiz = insertarRec(raiz, contenido);
    }

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


    public List<Contenido> buscarPorTema(String tema) {
        List<Contenido> resultados = new ArrayList<>();
        buscarPorTemaRec(raiz, tema.toLowerCase(), resultados);
        resultados.sort(comparador); // Ordena según el criterio del árbol
        return resultados;
    }

    private void buscarPorTemaRec(NodoContenido nodo, String tema, List<Contenido> resultados) {
        if (nodo == null) return;

        if (nodo.contenido.getTema().toLowerCase().contains(tema)) {
            resultados.add(nodo.contenido);
        }

        buscarPorTemaRec(nodo.izquierdo, tema, resultados);
        buscarPorTemaRec(nodo.derecho, tema, resultados);
    }

    public List<Contenido> buscarPorAutor(String autor) {
        List<Contenido> resultados = new ArrayList<>();
        buscarPorAutorRec(raiz, autor.toLowerCase(), resultados);
        resultados.sort(comparador);
        return resultados;
    }

    private void buscarPorAutorRec(NodoContenido nodo, String autor, List<Contenido> resultados) {
        if (nodo == null) return;

        if (nodo.contenido.getAutor().toLowerCase().contains(autor)) {
            resultados.add(nodo.contenido);
        }

        buscarPorAutorRec(nodo.izquierdo, autor, resultados);
        buscarPorAutorRec(nodo.derecho, autor, resultados);
    }


    public List<Contenido> obtenerTodosEnOrden(List<Contenido> contenidos) {
        if (contenidos == null) {
            contenidos = new ArrayList<>();
        }
        inorden(raiz, contenidos);
        return contenidos;
    }

    private void inorden(NodoContenido nodo, List<Contenido> contenidos) {
        if (nodo != null) {
            inorden(nodo.izquierdo, contenidos);
            contenidos.add(nodo.contenido);
            inorden(nodo.derecho, contenidos);
        }
    }


    public boolean estaVacio() {
        return raiz == null;
    }

    public int tamaño() {
        return tamañoRec(raiz);
    }

    private int tamañoRec(NodoContenido nodo) {
        if (nodo == null) return 0;
        return 1 + tamañoRec(nodo.izquierdo) + tamañoRec(nodo.derecho);
    }
}