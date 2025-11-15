package ClasesMemoria;

import java.util.Comparator;

public class ComparatorGanador implements Comparator<Jugador> {

    @Override
    public int compare(Jugador j1, Jugador j2) {
        int pares1 = j1.getNumeroDePares();
        int pares2 = j2.getNumeroDePares();

        int comparacionPares = Integer.compare(pares2, pares1);

        if (comparacionPares != 0) {
            return comparacionPares;
        }

        int suma1 = j1.getSumaTotalDeValores();
        int suma2 = j2.getSumaTotalDeValores();

        return Integer.compare(suma2, suma1);
    }
}