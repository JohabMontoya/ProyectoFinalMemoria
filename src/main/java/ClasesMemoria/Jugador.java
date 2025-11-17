package ClasesMemoria;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Jugador {

    private final String nombre;
    private final List<Carta> cartasGanadas;

    public Jugador(String nombre, List<Carta> cartasGanadas) {
        this.nombre = nombre;
        // copia defensiva y evita nulls
        if (cartasGanadas == null) {
            this.cartasGanadas = new ArrayList<>();
        } else {
            this.cartasGanadas = new ArrayList<>(cartasGanadas);
        }
    }

    public void registrarPar(Carta c1, Carta c2) {
        cartasGanadas.add(c1);
        cartasGanadas.add(c2);
    }

    /**
     * Devuelve el número de pares (puntuación principal).
     */
    public int getNumeroDePares() {
        return cartasGanadas.size() / 2;
    }

    //Calcula la suma total de valores de todas las cartas ganadas (métrica de desempate).
    public int getSumaTotalDeValores() {
        int suma = 0;
        for (Carta carta : cartasGanadas) {
            suma += carta.getValor();
        }
        return suma;
    }

    // Getters

    public String getNombre() {
        return nombre;
    }

    /**
     * Retorna una vista no modificable de las cartas ganadas.
     */
    public List<Carta> getCartasGanadas() {
        return Collections.unmodifiableList(cartasGanadas);
    }

    @Override
    public String toString() {
        return nombre + " (Pares: " + getNumeroDePares() + ")";
    }
}