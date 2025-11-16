package ClasesMemoria;

import java.util.List;

public class ModalidadPorValorYColor extends JuegoMemoria {


    public ModalidadPorValorYColor(List<Jugador> jugadores, Tablero tablero) {
        super(jugadores, tablero);
    }

    @Override
    protected boolean esPar(Casilla c1, Casilla c2) {
        // Obtenemos la identidad pura de la Carta de cada Casilla
        Carta carta1 = c1.getCarta();
        Carta carta2 = c2.getCarta();

        // Condición Mismo Valor
        boolean mismoValor = carta1.getValor() == carta2.getValor();

        // Condición Mismo Color
        boolean mismoColor = carta1.esRoja() == carta2.esRoja();
        return mismoValor && mismoColor;
    }
}