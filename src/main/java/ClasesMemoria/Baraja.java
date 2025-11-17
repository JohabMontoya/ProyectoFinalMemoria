package ClasesMemoria;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    /**
     * Devuelve una lista de cartas para el tablero de tamaño 'tamañoTablero'.
     * - Selecciona tamañoTablero/2 cartas distintas de la baraja.
     * - Crea duplicados (nuevas instancias) para formar pares.
     * - Mezcla y devuelve la lista final.
     */
    public List<Carta> getCartasParaTablero(int tamañoTablero) {
        int nPares = tamañoTablero / 2;

        // Usa una copia y barajarla para seleccionar cartas aleatorias
        List<Carta> copiaBarajada = new ArrayList<>(barajaCompleta);
        Collections.shuffle(copiaBarajada);

        // Toma los primeros N_pares de cartas distintas (por valor+palo)
        Set<Carta> seleccion = new HashSet<>();
        List<Carta> cartasDistintas = new ArrayList<>();
        for (Carta carta : copiaBarajada) {
            if (seleccion.size() >= nPares) break;
            if (!seleccion.contains(carta)) {
                seleccion.add(carta);
                cartasDistintas.add(carta);
            }
        }

        // Crea los pares duplicando creando nuevas instancias para cada carta
        List<Carta> cartasTablero = new ArrayList<>(tamañoTablero);
        for (Carta c : cartasDistintas) {
            // crear dos instancias separadas con mismo valor+palo
            cartasTablero.add(new Carta(c.getRawValor(), c.getPalo()));
            cartasTablero.add(new Carta(c.getRawValor(), c.getPalo()));
        }

        // Si por algún motivo faltan cartas (tamaño impar o menos pares), rellenar con nuevas cartas aleatorias
        while (cartasTablero.size() < tamañoTablero) {
            Carta extra = barajaCompleta.get((int) (Math.random() * barajaCompleta.size()));
            cartasTablero.add(new Carta(extra.getRawValor(), extra.getPalo()));
        }

        // Baraja las cartas finales del tablero antes de entregarlas
        Collections.shuffle(cartasTablero);
        return cartasTablero;
    }

    public List<Carta> getBarajaCompleta() {
        return new ArrayList<>(barajaCompleta);
    }
}