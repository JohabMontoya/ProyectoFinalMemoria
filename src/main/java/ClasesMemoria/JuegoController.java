package ClasesMemoria;

import java.util.List;

public class JuegoController {
    /**
     * Controlador que encapsula la creación del juego y delega operaciones
     * de voltear carta y obtención de estado al modelo (JuegoMemoria/Tablero).
     */
    private JuegoMemoria juego;
    private Tablero tablero;

    /**
     * Inicializa el controlador sin partida activa.
     */
    public JuegoController() {
        this.juego = null;
        this.tablero = null;
    }

    /**
     * Inicia una partida con la modalidad, jugadores y dimensiones indicadas.
     */
    public void iniciarPartida(String modalidad, List<Jugador> jugadores, int filas, int columnas) {
        Baraja baraja = new Baraja();
        int total = filas * columnas;
        List<Carta> deck = baraja.getCartasParaTablero(total, modalidad);

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

    /**
     * Devuelve la instancia del juego.
     */
    public JuegoMemoria getJuego() {
        return juego;
    }

    /**
     * Devuelve el tablero actual.
     */
    public Tablero getTablero() {
        return tablero;
    }

    /**
     * Delegado que intenta voltear la carta en la posición indicada.
     */
    public boolean voltearCarta(int fila, int columna) {
        if (juego == null) return false;
        return juego.voltearCarta(fila, columna);
    }

    /**
     * Indica si la partida ha terminado.
     */
    public boolean isTerminado() {
        return juego != null && juego.isTerminado();
    }

    /**
     * Devuelve la lista de jugadores del juego.
     */
    public List<Jugador> getJugadores() {
        if (juego == null) return java.util.Collections.emptyList();
        return juego.getJugadores();
    }

    /**
     * Devuelve el jugador actual.
     */
    public Jugador getJugadorActual() {
        if (juego == null) return null;
        return juego.getJugadorActual();
    }

    /**
     * Devuelve los ganadores de la partida.
     */
    public java.util.List<Jugador> obtenerGanadores() {
        if (juego == null) return java.util.Collections.emptyList();
        return juego.obtenerGanadores();
    }
}