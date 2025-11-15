package ClasesMemoria;

public class Jugador {

    private String nombre;
    private int puntos;

    public Jugador(String nombre) {
        this.nombre = nombre;
        this.puntos = 0;  // inicia sin puntos
    }

    // Suma un punto cuando encuentra un par
    public void sumarPunto() {
        puntos++;
    }

    public int getPuntos() {
        return puntos;
    }

    public String getNombre() {
        return nombre;
    }

    @Override
    public String toString() {
        return nombre + " (Puntos: " + puntos + ")";
    }
}
