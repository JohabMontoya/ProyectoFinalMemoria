package ClasesMemoria;

import java.util.Comparator;

public class ComparatorPalo implements Comparator <Carta> {
    @Override
    public int compare(Carta carta1, Carta carta2) {

        int pesoCarta1 = carta1.getPalo().getPeso();
        int pesoCarta2 = carta2.getPalo().getPeso();

        // Si los pesos tienen el mismo peso, tienen el mismo palo
        if (pesoCarta1 == pesoCarta2) {
            return 0;
        }

        // Si los palos son distintos no es par
        return 1;
    }
}

