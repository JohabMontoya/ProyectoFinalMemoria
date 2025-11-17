package ClasesMemoria;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Baraja: fuente única de 52 cartas. Selección de cartas para tablero
 * sin repetir cartas (modelo de baraja convencional).
 *
 * getCartasParaTablero admite una modalidad:
 * - "Por Valor" -> pares con mismo valor (cualquier palo), cartas distintas.
 * - "Por Valor y Color" -> pares con mismo valor y mismo color (dos palos del mismo color).
 */
public class Baraja {
    private final List<Carta> barajaCompleta;
    private final Random rnd = new Random();

    public Baraja() {
        this.barajaCompleta = new ArrayList<>();
        inicializar();
    }

    // Construye el mazo
    private void inicializar() {
        barajaCompleta.clear();
        for (Palo palo : Palo.values()) {
            for (int v = 1; v <= 13; v++) {
                barajaCompleta.add(new Carta(v, palo));
            }
        }
    }

    /**
     * Devuelve una lista de cartas para el tablero de tamaño 'tamañoTablero'
     * seleccionadas desde una baraja convencional sin repetir cartas.
     *
     * modalidad: "Por Valor" o "Por Valor y Color"
     *
     * @throws IllegalArgumentException si tamañoTablero es impar, mayor a 52 o
     *         si no es posible formar suficientes pares según la modalidad.
     */
    public List<Carta> getCartasParaTablero(int tamañoTablero, String modalidad) {
        if (tamañoTablero % 2 != 0) {
            throw new IllegalArgumentException("El tamaño del tablero debe ser par");
        }
        if (tamañoTablero > barajaCompleta.size()) {
            throw new IllegalArgumentException("Tamaño del tablero mayor que el número de cartas en la baraja");
        }

        int nPares = tamañoTablero / 2;

        // Pool de cartas disponibles (usar referencias originales de la baraja sin duplicar)
        List<Carta> pool = new ArrayList<>(barajaCompleta);
        Collections.shuffle(pool, rnd);

        List<Carta> cartasTablero = new ArrayList<>(tamañoTablero);

        if ("Por Valor y Color".equals(modalidad)) {
            // Para cada valor (orden aleatorio), intentar tomar pares por color (rojo, negro)
            List<Integer> valores = new ArrayList<>();
            for (int v = 1; v <= 13; v++) valores.add(v);
            Collections.shuffle(valores, rnd);

            for (int valor : valores) {
                if (cartasTablero.size() / 2 >= nPares) break;

                // Par rojo: DIAMANTE + CORAZON
                if (cartasTablero.size() / 2 < nPares) {
                    Carta d = findAndRemove(pool, valor, Palo.DIAMANTE);
                    Carta c = findAndRemove(pool, valor, Palo.CORAZON);
                    if (d != null && c != null) {
                        cartasTablero.add(d);
                        cartasTablero.add(c);
                    } else {
                        // si no se encontró ambos, devolver las que hallamos quitado (si existe alguno)
                        if (d != null) pool.add(d);
                        if (c != null) pool.add(c);
                    }
                }

                if (cartasTablero.size() / 2 >= nPares) break;

                // Par negro: TREBOL + PICA
                if (cartasTablero.size() / 2 < nPares) {
                    Carta t = findAndRemove(pool, valor, Palo.TREBOL);
                    Carta p = findAndRemove(pool, valor, Palo.PICA);
                    if (t != null && p != null) {
                        cartasTablero.add(t);
                        cartasTablero.add(p);
                    } else {
                        if (t != null) pool.add(t);
                        if (p != null) pool.add(p);
                    }
                }
            }
        } else {
            // Modalidad "Por Valor" u otras por defecto: pares por mismo valor (cualquier palo)
            List<Integer> valores = new ArrayList<>();
            for (int v = 1; v <= 13; v++) valores.add(v);
            Collections.shuffle(valores, rnd);

            for (int valor : valores) {
                if (cartasTablero.size() / 2 >= nPares) break;

                // obtener todas las cartas disponibles de este valor en pool
                List<Carta> disponibles = findAllAndRemove(pool, valor);
                // empaquetar en pares disjuntos
                while (disponibles.size() >= 2 && cartasTablero.size() / 2 < nPares) {
                    Carta a = disponibles.remove(0);
                    Carta b = disponibles.remove(0);
                    cartasTablero.add(a);
                    cartasTablero.add(b);
                }
                // cualquier sobrante de 'disponibles' devolverlo al pool (ya fue removido)
                if (!disponibles.isEmpty()) {
                    // devolver al pool los sobrantes (manteniendo sin duplicar)
                    pool.addAll(disponibles);
                }
            }
        }

        if (cartasTablero.size() / 2 < nPares) {
            throw new IllegalArgumentException("No fue posible formar " + nPares + " pares con la modalidad '" + modalidad + "' usando una baraja convencional sin repetir cartas.");
        }

        // Si por alguna razón quedaron más cartas (no debería), recortar
        if (cartasTablero.size() > tamañoTablero) {
            cartasTablero = cartasTablero.subList(0, tamañoTablero);
        }

        // Barajar el tablero final
        Collections.shuffle(cartasTablero, rnd);
        return cartasTablero;
    }

    // busca y elimina la primer carta en pool con ese valor y palo; devuelve la carta o null
    private Carta findAndRemove(List<Carta> pool, int valor, Palo palo) {
        for (int i = 0; i < pool.size(); i++) {
            Carta c = pool.get(i);
            if (c.getRawValor() == valor && c.getPalo() == palo) {
                pool.remove(i);
                return c;
            }
        }
        return null;
    }

    // obtiene todas las cartas de pool con ese valor; las remueve de pool y las devuelve
    private List<Carta> findAllAndRemove(List<Carta> pool, int valor) {
        List<Carta> res = new ArrayList<>();
        for (int i = pool.size() - 1; i >= 0; i--) {
            Carta c = pool.get(i);
            if (c.getRawValor() == valor) {
                res.add(0, c); // preservando algún orden
                pool.remove(i);
            }
        }
        return res;
    }

    public List<Carta> getBarajaCompleta() {
        return new ArrayList<>(barajaCompleta);
    }
}