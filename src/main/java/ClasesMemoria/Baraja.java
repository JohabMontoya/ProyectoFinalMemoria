package ClasesMemoria;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Baraja {
    private List<Carta> cartas;

    public Baraja() {
        this.cartas = new ArrayList<>();
        inicializar();
        barajar();
    }

    // Construye el mazo
    private void inicializar() {
        cartas.clear();
        for (Palo palo : Palo.values()) {
            for (int v = 1; v <= 13; v++) {
                cartas.add(new Carta(v, palo));
            }
        }
    }

    // Baraja el mazo aleatoriamente
    public void barajar() {
        Collections.shuffle(cartas);
    }

    // Roba
    public Carta robar() {
        if (cartas.isEmpty()) return null;
        return cartas.remove(cartas.size() - 1);
    }

    // Mira la carta en la posición index
    public Carta verEn(int index) {
        if (index < 0 || index >= cartas.size()) return null;
        return cartas.get(index);
    }

    // Devuelve la cantidad de cartas restantes
    public int size() {
        return cartas.size();
    }

    // Indica si la baraja está vacía
    public boolean isEmpty() {
        return cartas.isEmpty();
    }

    // Reconstruye y vuelve a barajar la baraja estándar
    public void reset() {
        inicializar();
        barajar();
    }

    // Devuelve una lista de cartas
    public List<Carta> getCartas() {
        return new ArrayList<>(cartas);
    }

    @Override
    public String toString() {
        return "Baraja{cartas=" + cartas.size() + "}";
    }
}