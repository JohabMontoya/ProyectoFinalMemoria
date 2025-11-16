package ClasesMemoria;

/**
 * Modalidad concreta: empareja cartas que tienen el mismo valor (ignora color/palo).
 *
 * Implementación mínima que extiende JuegoMemoria y define la regla esPar comparando
 * el valor de las cartas contenidas en las casillas.
 */
public class ModalidadPorValor extends JuegoMemoria {

    public ModalidadPorValor(java.util.List<Jugador> jugadores, Tablero tablero) {
        super(jugadores, tablero);
    }

    @Override
    protected boolean esPar(Casilla c1, Casilla c2) {
        if (c1 == null || c2 == null) return false;
        Carta a = c1.getCarta();
        Carta b = c2.getCarta();
        if (a == null || b == null) return false;
        return a.getValor() == b.getValor();
    }
}