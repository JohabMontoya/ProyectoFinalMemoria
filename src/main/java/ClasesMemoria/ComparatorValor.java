package ClasesMemoria;

import java.util.Comparator;

public class ComparatorValor implements Comparator<Carta> {
    @Override
    public int compare(Carta carta1, Carta carta2) {
        if (carta1 == null && carta2 == null) return 0;
        if (carta1 == null) return -1;
        if (carta2 == null) return 1;

        return Integer.compare(carta1.getValor(), carta2.getValor());
    }
}