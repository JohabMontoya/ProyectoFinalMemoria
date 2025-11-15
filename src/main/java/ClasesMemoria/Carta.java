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
        return 0;
    }
}
