package ClasesMemoria;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Baraja {
    private final List<Carta> barajaCompleta;

    public Baraja() {
        this.barajaCompleta = new ArrayList<>();
        inicializar();
    }

    // Construye el mazo
    private void inicializar() {
        barajaCompleta.clear();
        for (Palo palo : Palo.values()) {
            for (int v = 1; v <= 13; v++) {
                barajaCompleta.add(new Carta(v, palo));
            }
        }
    }

    public List<Carta> getCartasParaTablero(int tamañoTablero, String modalidad) {
        int nPares = tamañoTablero / 2;

        //Usa una copia y barajarla para seleccionar cartas aleatorias
        List<Carta> copiaBarajada = new ArrayList<>(barajaCompleta);
        Collections.shuffle(copiaBarajada);

        //Toma los primeros N_pares de cartas distintas
        List<Carta> cartasDistintas = new ArrayList<>();
        // Usa la lista barajada como fuente para seleccionar N_pares distintos
        for (Carta carta : copiaBarajada) {
            if (cartasDistintas.size() < nPares && !cartasDistintas.contains(carta)) {
                cartasDistintas.add(carta);
            }
        }

        //  Crea los pares duplicando el conjunto de cartas distintas
        List<Carta> cartasTablero = new ArrayList<>();
        cartasTablero.addAll(cartasDistintas);
        cartasTablero.addAll(cartasDistintas); // Duplicado para crear los pares

        // Baraja las cartas finales del tablero antes de entregarlas
        Collections.shuffle(cartasTablero);

        return cartasTablero;
    }
}