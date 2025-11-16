package ClasesMemoria;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class JuegoMemoria {

    protected final List<Jugador> jugadores;
    protected final Tablero tablero;

    // Casillas volteadas en el turno actual (0, 1 o 2 elementos)
    protected final List<Casilla> casillasVolteadas;

    protected int indiceJugadorActual;
    protected boolean terminado;

    // Crea el juego con una lista de jugadores y un tablero ya construido.
    public JuegoMemoria(List<Jugador> jugadores, Tablero tablero) {
        this.jugadores = new ArrayList<>(jugadores);
        this.tablero = tablero;
        this.casillasVolteadas = new ArrayList<>(2);
        this.indiceJugadorActual = 0;
        this.terminado = false;
    }

    // Regla abstracta que define si dos cartas constituyen un par según la modalidad concreta.
    // Subclases deben implementar esta función (por ejemplo: mismo valor; mismo valor y mismo color).

    protected abstract boolean esPar(Casilla c1, Casilla c2);

    /**
     * Inicializa el estado del juego para comenzar a jugar:
     * - Oculta las casillas visibles (si no están emparejadas).
     * - Limpia las casillas volteadas del turno y resetea el turno al primer jugador.
     * - Marca terminado = false.
     *
     * Nota: no "desempareja" casillas que ya estuvieran emparejadas.
     */
    public void iniciar() {
        // ocultar todas las casillas no emparejadas
        for (int i = 0; i < tablero.getFilas(); i++) {
            for (int j = 0; j < tablero.getColumnas(); j++) {
                Casilla c = tablero.getCasillaEn(i, j);
                if (c != null && !c.estaEmparejada()) {
                    c.ocultar();
                }
            }
        }
        casillasVolteadas.clear();
        indiceJugadorActual = 0;
        terminado = false;
    }

    /**
     * Intenta voltear la casilla en la posición (fila, columna).
     *
     * Reglas:
     * - Si la posición no es válida o la casilla ya está emparejada o ya está volteada en este turno,
     *   retorna false y no hace nada.
     * - Si el volteo es válido, la casilla se marca volteada y se agrega a casillasVolteadas.
     * - Cuando hay dos casillas volteadas, se evalúa esPar(Carta, Carta):
     *     - Si son par: se marcan ambas como emparejadas y el jugador actual registra el par.
     *     - Si no son par: se ocultan ambas casillas y pasa el turno al siguiente jugador.
     *   En ambos casos se limpia casillasVolteadas.
     * - Si tras evaluar todas las casillas quedan emparejadas, el juego se marca como terminado.
     */
    public boolean voltearCarta(int fila, int columna) {
        if (terminado) return false;
        Casilla casilla = tablero.getCasillaEn(fila, columna);
        if (casilla == null) return false;
        if (casilla.estaEmparejada()) return false;
        if (casillasVolteadas.contains(casilla)) return false; // misma casilla ya volteada en este turno

        // Voltear la casilla
        casilla.voltear();
        casillasVolteadas.add(casilla);

        // Si ya tenemos dos casillas, evaluar par
        if (casillasVolteadas.size() == 2) {
            Casilla c1 = casillasVolteadas.get(0);
            Casilla c2 = casillasVolteadas.get(1);

            if (esPar(c1, c2)) {
                // formar par: marcar emparejadas y registrar en jugador
                c1.marcarComoEmparejada();
                c2.marcarComoEmparejada();
                getJugadorActual().registrarPar(c1.getCarta(), c2.getCarta());
            } else {
                // no son par: ocultarlas y pasar turno
                c1.ocultar();
                c2.ocultar();
                pasarTurno();
            }

            casillasVolteadas.clear();

            // verificar si todas están emparejadas -> terminar juego
            if (tablero.estanTodasEmparejadas()) {
                terminado = true;
            }
        }

        return true;
    }

    // Devuelve el jugador cuyo turno es actualmente.
    public Jugador getJugadorActual() {
        return jugadores.get(indiceJugadorActual);
    }

    // Pasa al siguiente jugador en orden circular.
    protected void pasarTurno() {
        indiceJugadorActual = (indiceJugadorActual + 1) % jugadores.size();
    }

    // Indica si el juego ha terminado (todas las cartas emparejadas).
    public boolean isTerminado() {
        return terminado;
    }

    // Devuelve la lista de jugadores .
    public List<Jugador> getJugadores() {
        return Collections.unmodifiableList(new ArrayList<>(jugadores));
    }

    // Devuelve el tablero asociado a este juego.
    public Tablero getTablero() {
        return tablero;
    }

    /**
     * Devuelve la(s) persona(s) ganadora(s) al finalizar el juego.
     *
     * - Ordena a los jugadores usando la lógica de ComparatorGanador (mejor primero).
     * - Devuelve todos los jugadores que empatan en la primera posición (mismo número de pares
     *   y misma suma total de valores, según las métricas usadas en ComparatorGanador).
     *
     * Regresa la lista con los ganadores o el ganador. Si no hay jugadores, retorna lista vacía.
     */

    public List<Jugador> obtenerGanadores() {
        if (jugadores.isEmpty()) {
            return Collections.emptyList();
        }

        // Hacer copia para ordenar sin modificar la lista original
        List<Jugador> copia = new ArrayList<>(jugadores);
        Collections.sort(copia, new ComparatorGanador());

        // El primer jugador es el mejor según ComparatorGanador
        Jugador mejor = copia.get(0);
        int mejorPares = mejor.getNumeroDePares();
        int mejorSuma = mejor.getSumaTotalDeValores();

        List<Jugador> winners = new ArrayList<>();
        for (Jugador j : copia) {
            if (j.getNumeroDePares() == mejorPares && j.getSumaTotalDeValores() == mejorSuma) {
                winners.add(j);
            } else {
                break; // como la lista está ordenada, al encontrar uno distinto podemos terminar
            }
        }

        return Collections.unmodifiableList(winners);
    }
}