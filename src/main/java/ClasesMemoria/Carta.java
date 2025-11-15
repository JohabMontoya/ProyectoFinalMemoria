package ClasesMemoria;

public class Carta implements Comparable<Carta> {
    int valor;
    Palo palo;
    boolean estaVolteada;
    boolean estaEmparejada;

    public Carta(int valor, Palo palo) {
        this.valor = valor;
        this.palo = palo;
        estaVolteada = false;
        estaEmparejada = false;
    }
    // getters/setters
    public int getValor() {
        return valor;
    }
    public Palo getPalo() {
        return palo;
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
        // Orden natural: por valor primero, si empate por peso del palo
        int cmp = Integer.compare(this.valor, o.valor);
        if (cmp != 0) return cmp;
        return Integer.compare(this.palo.getPeso(), o.palo.getPeso());
    }
}