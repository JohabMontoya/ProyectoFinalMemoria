package ClasesMemoria;

public enum Palo {
    TREBOL(1,"♣\uFE0E","negro"),
    DIAMANTE(2,"♦\uFE0F","rojo"),
    CORAZON(3,"❤\uFE0F","rojo"),
    PICA(4,"♠\uFE0F","negro");

    private final int peso;
    private final String figura;
    private final String color;

    Palo(int peso, String figura, String color) {
        this.peso = peso;
        this.figura = figura;
        this.color = color;
    }
    public int getPeso() {
        return peso;
    }
    public String getFigura() {
        return figura;
    }
    public boolean esRojo() {
        return this.color.equals("rojo");
    }

}
