package ClasesMemoria;

import java.util.ArrayList;
import java.util.List;

public class Jugador {

    private final String nombre;
    private final List<Carta> cartasGanadas;

    public Jugador(String nombre, List<Carta> cartasGanadas) {
        this.nombre = nombre;
        this.cartasGanadas = cartasGanadas;
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

    public List<Carta> getCartasGanadas() {
        return cartasGanadas;
    }

    @Override
    public String toString() {
        return nombre + " (Pares: " + getNumeroDePares() + ")";
    }
}
