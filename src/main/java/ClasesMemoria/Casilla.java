package ClasesMemoria;

public class Casilla {

    private final Carta carta;
    private boolean estaVolteada;
    private boolean estaEmparejada;

    public Casilla(Carta carta) {
        this.carta = carta;
        this.estaVolteada = false; // Inicialmente boca abajo
        this.estaEmparejada = false; // Inicialmente sin emparejar
    }

    // --- Métodos de Estado (Control) ---

    public void voltear() {
        if (!estaEmparejada) {
            this.estaVolteada = true;
        }
    }

    public void ocultar() {
        if (!estaEmparejada) {
            this.estaVolteada = false;
        }
    }

    public void marcarComoEmparejada() {
        this.estaEmparejada = true;
        this.estaVolteada = true; // Un par emparejado se deja visible
    }

    // Getters

    public Carta getCarta() {
        return carta;
    }

    public boolean estaVolteada() {
        return estaVolteada;
    }

    public boolean estaEmparejada() {
        return estaEmparejada;
    }
}