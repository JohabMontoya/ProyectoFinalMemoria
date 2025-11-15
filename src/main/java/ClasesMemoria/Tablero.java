package ClasesMemoria;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;


public class Tablero {

    private final List<Carta> cartas;
    private final int filas;
    private final int columnas;

    public Tablero(int filas, int columnas, List<Carta> cartas) {
        this.filas = filas;
        this.columnas = columnas;
        this.cartas = new ArrayList<>(cartas); // copia defensiva
    }

    // Devuelve la carta en el índice lineal
    public Carta getCartaEn(int index) {
        if (index < 0 || index >= cartas.size()) return null;
        return cartas.get(index);
    }

    // Devuelve la carta en la posición fila/columna
    public Carta getCartaEn(int fila, int columna) {
        if (fila < 0 || fila >= filas || columna < 0 || columna >= columnas) return null;
        int idx = fila * columnas + columna;
        return getCartaEn(idx);
    }

    // Baraja las cartas del tablero.
    public void barajar() {
        Collections.shuffle(cartas);
    }

    // Número total de posiciones en el tablero.
    public int size() {
        return cartas.size();
    }

    public int getFilas() {
        return filas;
    }

    public int getColumnas() {
        return columnas;
    }

    // Devuelve una vista inmutable de las cartas.
    public List<Carta> getCartas() {
        return Collections.unmodifiableList(cartas);
    }

    @Override
    public String toString() {
        return "Tablero{filas=" + filas + ", columnas=" + columnas + ", cartas=" + cartas.size() + "}";
    }
}