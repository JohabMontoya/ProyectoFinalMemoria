package ClasesMemoria;

public class Carta {
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


}
