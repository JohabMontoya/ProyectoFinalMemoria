package ClasesMemoria;

import java.util.Collections;
import java.util.List;



public class Tablero {

    private final Casilla[][] casillas;
    private final int filas;
    private final int columnas;

    public Tablero(int filas, int columnas, List<Carta> cartasIniciales) {
        this.filas = filas;
        this.columnas = columnas;
        this.casillas = new Casilla[filas][columnas];

        //Llenado de la matriz con objetos Casilla
        int k = 0;
        for (int i = 0; i < filas; i++) {
            for (int j = 0; j < columnas; j++) {
                // Cada posición recibe un nuevo objeto Casilla que contiene una Carta
                this.casillas[i][j] = new Casilla(cartasIniciales.get(k++));
            }
        }
    }
    private boolean esPosicionValida(int fila, int columna) {
        return fila >= 0 && fila < filas && columna >= 0 && columna < columnas;
    }

    public void voltearCarta(int fila, int columna) {
        if (esPosicionValida(fila, columna)) {
            casillas[fila][columna].voltear();
        }
    }

    public void ocultarCarta(int fila, int columna) {
        if (esPosicionValida(fila, columna)) {
            casillas[fila][columna].ocultar();
        }
    }

    public void marcarComoEmparejada(int fila, int columna) {
        if (esPosicionValida(fila, columna)) {
            casillas[fila][columna].marcarComoEmparejada();
        }
    }

    public Carta getCartaEn(int fila, int columna) {
        Casilla casilla = getCasillaEn(fila, columna);
        return (casilla != null) ? casilla.getCarta() : null;
    }

    // Devuelve la carta en la posición fila/columna
    public Casilla getCasillaEn(int fila, int columna) {
        if (esPosicionValida(fila, columna)) {
            return casillas[fila][columna];
        }
        return null;
    }
    public boolean estanTodasEmparejadas() {
        for (int i = 0; i < filas; i++) {
            for (int j = 0; j < columnas; j++) {
                if (!casillas[i][j].estaEmparejada()) {
                    return false;
                }
            }
        }
        return true;
    }

    public int getFilas() {
        return filas;
    }

    public int getColumnas() {
        return columnas;
    }


    @Override
    public String toString() {
        int totalCartas = filas * columnas;
        return "Tablero{filas=" + filas + ", columnas=" + columnas + ", cartas=" + totalCartas + "}";
    }
}