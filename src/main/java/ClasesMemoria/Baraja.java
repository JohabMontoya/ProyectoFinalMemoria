package ClasesMemoria;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Baraja {
    /**
     * Clase que representa la baraja de 52 cartas y permite seleccionar cartas
     * para crear el conjunto del tablero sin repetir cartas.
     */
    private final List<Carta> barajaCompleta;
    private final Random rnd = new Random();

    /**
     * Crea e inicializa la baraja completa.
     */
    public Baraja() {
        this.barajaCompleta = new ArrayList<>();
        inicializar();
    }

    /**
     * Rellena la baraja con las 52 cartas (valores 1..13 por cada palo).
     */
    private void inicializar() {
        barajaCompleta.clear();
        for (Palo palo : Palo.values()) {
            for (int v = 1; v <= 13; v++) {
                barajaCompleta.add(new Carta(v, palo));
            }
        }
    }

    /**
     * Selecciona cartas para formar el tablero sin repetir cartas.
     * La modalidad puede ser "Por Valor" o "Por Valor y Color".
     * Lanza IllegalArgumentException si no es posible satisfacer la petición.
     */
    public List<Carta> getCartasParaTablero(int tamañoTablero, String modalidad) {
        if (tamañoTablero % 2 != 0) {
            throw new IllegalArgumentException("El tamaño del tablero debe ser par");
        }
        if (tamañoTablero > barajaCompleta.size()) {
            throw new IllegalArgumentException("Tamaño del tablero mayor que el número de cartas en la baraja");
        }

        int nPares = tamañoTablero / 2;
        List<Carta> pool = new ArrayList<>(barajaCompleta);
        Collections.shuffle(pool, rnd);
        List<Carta> cartasTablero = new ArrayList<>(tamañoTablero);

        if ("Por Valor y Color".equals(modalidad)) {
            Map<Integer, List<Carta>> rojoPorValor = new HashMap<>();
            Map<Integer, List<Carta>> negroPorValor = new HashMap<>();

            for (Carta c : pool) {
                int v = c.getRawValor();
                if (c.esRoja()) {
                    rojoPorValor.computeIfAbsent(v, k -> new ArrayList<>()).add(c);
                } else {
                    negroPorValor.computeIfAbsent(v, k -> new ArrayList<>()).add(c);
                }
            }

            List<List<Carta>> paresCandidatos = new ArrayList<>();

            for (int v = 1; v <= 13; v++) {
                List<Carta> r = rojoPorValor.get(v);
                if (r != null && r.size() >= 2) {
                    List<Carta> par = new ArrayList<>(2);
                    par.add(r.get(0));
                    par.add(r.get(1));
                    paresCandidatos.add(par);
                }
                List<Carta> n = negroPorValor.get(v);
                if (n != null && n.size() >= 2) {
                    List<Carta> par = new ArrayList<>(2);
                    par.add(n.get(0));
                    par.add(n.get(1));
                    paresCandidatos.add(par);
                }
            }

            Collections.shuffle(paresCandidatos, rnd);

            if (paresCandidatos.size() < nPares) {
                throw new IllegalArgumentException("No fue posible formar " + nPares + " pares con la modalidad '" + modalidad + "' usando una baraja convencional sin repetir cartas.");
            }

            for (int i = 0; i < nPares; i++) {
                List<Carta> par = paresCandidatos.get(i);
                cartasTablero.add(par.get(0));
                cartasTablero.add(par.get(1));
            }

        } else {
            Map<Integer, List<Carta>> porValor = new HashMap<>();
            for (Carta c : pool) {
                int v = c.getRawValor();
                porValor.computeIfAbsent(v, k -> new ArrayList<>()).add(c);
            }

            List<List<Carta>> paresCandidatos = new ArrayList<>();
            for (int v = 1; v <= 13; v++) {
                List<Carta> lista = porValor.get(v);
                if (lista == null) continue;
                for (int i = 0; i + 1 < lista.size(); i += 2) {
                    List<Carta> par = new ArrayList<>(2);
                    par.add(lista.get(i));
                    par.add(lista.get(i + 1));
                    paresCandidatos.add(par);
                }
            }

            Collections.shuffle(paresCandidatos, rnd);

            if (paresCandidatos.size() < nPares) {
                throw new IllegalArgumentException("No fue posible formar " + nPares + " pares con la modalidad '" + modalidad + "' usando una baraja convencional sin repetir cartas.");
            }

            for (int i = 0; i < nPares; i++) {
                List<Carta> par = paresCandidatos.get(i);
                cartasTablero.add(par.get(0));
                cartasTablero.add(par.get(1));
            }
        }

        Collections.shuffle(cartasTablero, rnd);
        return cartasTablero;
    }

    /**
     * Devuelve una copia de la baraja completa.
     */
    public List<Carta> getBarajaCompleta() {
        return new ArrayList<>(barajaCompleta);
    }
}