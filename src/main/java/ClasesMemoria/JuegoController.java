package ClasesMemoria;

import java.util.List;


public class JuegoController {

    private JuegoMemoria juego;
    private Tablero tablero;

    public JuegoController() {
        this.juego = null;
        this.tablero = null;
    }


     // Crea la Baraja y genera las cartas para el tablero usando una baraja convencional sin repetir cartas.

    public void iniciarPartida(String modalidad, List<Jugador> jugadores, int filas, int columnas) {
        Baraja baraja = new Baraja();
        int total = filas * columnas;
        List<Carta> deck = baraja.getCartasParaTablero(total, modalidad);

        // Asegurar tamaño correcto (la baraja ya baraja internamente)
        if (deck.size() > total) {
            deck = deck.subList(0, total);
        }

        this.tablero = new Tablero(filas, columnas, deck);

        if ("Por Valor y Color".equals(modalidad)) {
            this.juego = new ModalidadPorValorYColor(jugadores, tablero);
        } else {
            this.juego = new ModalidadPorValor(jugadores, tablero);
        }

        this.juego.iniciar();
    }

    public JuegoMemoria getJuego() {
        return juego;
    }

    public Tablero getTablero() {
        return tablero;
    }

    /**
     * Delegado a JuegoMemoria.voltearCarta
     */
    public boolean voltearCarta(int fila, int columna) {
        if (juego == null) return false;
        return juego.voltearCarta(fila, columna);
    }

    public boolean isTerminado() {
        return juego != null && juego.isTerminado();
    }

    public List<Jugador> getJugadores() {
        if (juego == null) return java.util.Collections.emptyList();
        return juego.getJugadores();
    }

    public Jugador getJugadorActual() {
        if (juego == null) return null;
        return juego.getJugadorActual();
    }

    public java.util.List<Jugador> obtenerGanadores() {
        if (juego == null) return java.util.Collections.emptyList();
        return juego.obtenerGanadores();
    }
}