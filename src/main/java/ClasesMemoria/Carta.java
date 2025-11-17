package ClasesMemoria;

import java.util.Objects;

public class Carta implements Comparable<Carta> {
    private final int valor;
    private final Palo palo;

    public Carta(int valor, Palo palo) {
        this.valor = valor;
        this.palo = palo;

    }
    // getters/setters

     // Valor usado para comparaciones donde As se considera 14.
    public int getValor() {
        return (valor == 1) ? 14 : valor;
    }

    public int getRawValor() {
        return valor;
    }

    public Palo getPalo() {
        return palo;
    }

    public boolean esRoja() {
        return this.palo.esRojo();
    }
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

    @Override
    public int compareTo(Carta o) {
        if (o == null) return 1;
        // Orden natural, por valor primero, si empate por peso del palo
        int comparar = Integer.compare(this.getValor(), o.getValor());
        if (comparar != 0) return comparar;
        return Integer.compare(this.palo.getPeso(), o.palo.getPeso());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Carta)) return false;
        Carta carta = (Carta) o;
        // Comparar por valor original (1..13) y por palo
        return this.valor == carta.valor && this.palo == carta.palo;
    }

    @Override
    public int hashCode() {
        return Objects.hash(valor, palo);
    }
}