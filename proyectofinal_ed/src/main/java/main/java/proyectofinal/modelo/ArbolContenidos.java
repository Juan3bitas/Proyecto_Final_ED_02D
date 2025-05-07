package main.java.proyectofinal.modelo;

import java.util.Comparator;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ArbolContenidos {
    private NodoContenido raiz;
    private final Comparator<Contenido> comparador;


    public ArbolContenidos(CriterioOrden criterio) {
        this.comparador = crearComparador(criterio);
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


    public List<Contenido> obtenerTodosEnOrden() {
        List<Contenido> contenidos = new ArrayList<>();
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