package ClasesMemoria;

import java.util.Comparator;

public class ComparatorValor implements Comparator<Carta> {
    @Override
    public int compare(Carta carta1, Carta carta2) {

        int valor1 = carta1.getValor();
        int valor2 = carta2.getValor();

        // Compara igualdad del valor
        if (valor1 == valor2) {
            return 0;
        }

        // Si los valores son distintos no es par
        return 1;
    }
}

