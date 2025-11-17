package ClasesMemoria;

import java.util.Objects;

public class Carta implements Comparable<Carta> {
    /**
     * Clase que representa una carta con valor (1..13) y palo.
     * getValor() devuelve As como 14 para comparaciones.
     */
    private final int valor;
    private final Palo palo;

    /**
     * Crea una carta con el valor raw (1..13) y el palo.
     */
    public Carta(int valor, Palo palo) {
        this.valor = valor;
        this.palo = palo;
    }

    /**
     * Devuelve el valor usado en comparaciones (As = 14).
     */
    public int getValor() {
        return (valor == 1) ? 14 : valor;
    }

    /**
     * Devuelve el valor interno (1..13).
     */
    public int getRawValor() {
        return valor;
    }

    /**
     * Devuelve el palo de la carta.
     */
    public Palo getPalo() {
        return palo;
    }

    /**
     * Indica si la carta es de color rojo (Diamante o Corazón).
     */
    public boolean esRoja() {
        return this.palo.esRojo();
    }

    /**
     * Representación legible de la carta (A, J, Q, K + figura del palo).
     */
    @Override
    public String toString() {
        String laCarta;
        switch (valor) {
            case 1:
                laCarta = "A";
                break;
            case 11:
                laCarta = "J";
                break;
            case 12:
                laCarta = "Q";
                break;
            case 13:
                laCarta = "K";
                break;
            default:
                laCarta = "" + valor;
                break;
        }
        laCarta = laCarta + palo.getFigura();
        return laCarta;
    }

    /**
     * Orden natural: por valor (As alto) y en empate por peso del palo.
     */
    @Override
    public int compareTo(Carta o) {
        if (o == null) return 1;
        int comparar = Integer.compare(this.getValor(), o.getValor());
        if (comparar != 0) return comparar;
        return Integer.compare(this.palo.getPeso(), o.palo.getPeso());
    }

    /**
     * Igualdad por valor raw y palo.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Carta)) return false;
        Carta carta = (Carta) o;
        return this.valor == carta.valor && this.palo == carta.palo;
    }

    /**
     * Hash basado en valor raw y palo.
     */
    @Override
    public int hashCode() {
        return Objects.hash(valor, palo);
    }
}