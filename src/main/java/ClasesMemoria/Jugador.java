package ClasesMemoria;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Jugador {
    /**
     * Clase que representa un jugador y las cartas que ha ganado.
     */
    private final String nombre;
    private final List<Carta> cartasGanadas;

    /**
     * Crea un jugador con nombre y lista inicial de cartas (copia defensiva).
     */
    public Jugador(String nombre, List<Carta> cartasGanadas) {
        this.nombre = nombre;
        if (cartasGanadas == null) {
            this.cartasGanadas = new ArrayList<>();
        } else {
            this.cartasGanadas = new ArrayList<>(cartasGanadas);
        }
    }

    /**
     * Registra un par de cartas ganadas por el jugador.
     */
    public void registrarPar(Carta c1, Carta c2) {
        cartasGanadas.add(c1);
        cartasGanadas.add(c2);
    }

    /**
     * Devuelve el número de pares ganados.
     */
    public int getNumeroDePares() {
        return cartasGanadas.size() / 2;
    }

    /**
     * Calcula la suma total de valores de las cartas ganadas (desempate).
     */
    public int getSumaTotalDeValores() {
        return cartasGanadas.stream()
                .mapToInt(Carta::getValor)
                .sum();
    }

    /**
     * Devuelve el nombre del jugador.
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Devuelve una vista no modificable de las cartas ganadas.
     */
    public List<Carta> getCartasGanadas() {
        return Collections.unmodifiableList(cartasGanadas);
    }

    /**
     * Representación breve del jugador.
     */
    @Override
    public String toString() {
        return nombre + " (Pares: " + getNumeroDePares() + ")";
    }
}