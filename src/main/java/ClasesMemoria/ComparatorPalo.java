package ClasesMemoria;

import java.util.Comparator;

public class ComparatorPalo implements Comparator <Carta> {
    @Override
    public int compare(Carta c1, Carta c2) {
        // Son un par (mismo palo)
        if (c1.getPalo() == c2.getPalo()) {
            return 0;
        }
        // No son par
        return 1;
    }
}

