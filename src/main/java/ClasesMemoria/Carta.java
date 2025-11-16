package ClasesMemoria;

public class Carta implements Comparable<Carta> {
    private final int valor;
    private final Palo palo;

    public Carta(int valor, Palo palo) {
        this.valor = valor;
        this.palo = palo;

    }
    // getters/setters
    public int getValor() {
        return (valor == 1) ? 14 : valor;
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
}