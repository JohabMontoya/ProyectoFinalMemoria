package ClasesMemoria;


import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class Tablero implements Iterable<Casilla> {

    private final Casilla[][] casillas;
    private final int filas;
    private final int columnas;

    /**
     * Construye un tablero con las dimensiones y la lista de cartas proporcionada.
     * Se asume que cartasIniciales tiene al menos filas*columnas elementos.
     */
    public Tablero(int filas, int columnas, List<Carta> cartasIniciales) {
        this.filas = filas;
        this.columnas = columnas;
        this.casillas = new Casilla[filas][columnas];

        int k = 0;
        for (int i = 0; i < filas; i++) {
            for (int j = 0; j < columnas; j++) {
                this.casillas[i][j] = new Casilla(cartasIniciales.get(k++));
            }
        }
    }

    /**
     * Comprueba si una posición es válida dentro del tablero.
     */
    private boolean esPosicionValida(int fila, int columna) {
        return fila >= 0 && fila < filas && columna >= 0 && columna < columnas;
    }

    /**
     * Voltea la carta en la posición indicada si la posición es válida.
     */
    public void voltearCarta(int fila, int columna) {
        if (esPosicionValida(fila, columna)) {
            casillas[fila][columna].voltear();
        }
    }

    /**
     * Oculta la carta en la posición indicada si la posición es válida.
     */
    public void ocultarCarta(int fila, int columna) {
        if (esPosicionValida(fila, columna)) {
            casillas[fila][columna].ocultar();
        }
    }

    /**
     * Marca la casilla como emparejada en la posición indicada si es válida.
     */
    public void marcarComoEmparejada(int fila, int columna) {
        if (esPosicionValida(fila, columna)) {
            casillas[fila][columna].marcarComoEmparejada();
        }
    }

    /**
     * Devuelve la carta en la posición fila/columna.
     */
    public Carta getCartaEn(int fila, int columna) {
        Casilla casilla = getCasillaEn(fila, columna);
        return (casilla != null) ? casilla.getCarta() : null;
    }

    /**
     * Devuelve la casilla en la posición fila/columna o null si inválida.
     */
    public Casilla getCasillaEn(int fila, int columna) {
        if (esPosicionValida(fila, columna)) {
            return casillas[fila][columna];
        }
        return null;
    }

    /**
     * Comprueba si todas las casillas están emparejadas.
     */
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

    /**
     * Devuelve el número de filas.
     */
    public int getFilas() {
        return filas;
    }

    /**
     * Devuelve el número de columnas.
     */
    public int getColumnas() {
        return columnas;
    }

    /**
     * Iterador que recorre las casillas en orden fila-major.
     */
    @Override
    public Iterator<Casilla> iterator() {
        return new Iterator<Casilla>() {
            private int index = 0;
            private final int total = filas * columnas;

            @Override
            public boolean hasNext() {
                return index < total;
            }

            @Override
            public Casilla next() {
                if (!hasNext()) throw new NoSuchElementException();
                int r = index / columnas;
                int c = index % columnas;
                index++;
                return casillas[r][c];
            }
        };
    }

    @Override
    public String toString() {
        int totalCartas = filas * columnas;
        return "Tablero{filas=" + filas + ", columnas=" + columnas + ", cartas=" + totalCartas + "}";
    }
}