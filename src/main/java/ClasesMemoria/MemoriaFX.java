package ClasesMemoria;

import ClasesMemoria.*;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.*;

public class MemoriaFX extends Application {

    private JuegoMemoria juego;
    private Tablero tablero;
    private GridPane grid;
    private Button[][] botones;
    private Label lblEstado;

    // Panel lateral con estado de jugadores
    private VBox panelJugadores;

    // Datos configurables/fijos por requerimiento
    private static final int FILAS = 4;
    private static final int COLUMNAS = 13;

    // Colores para indicar propietario de pares (hasta 4 jugadores)
    private static final String[] PLAYER_COLORS = {
            "#ffd54f", // amarillo
            "#90caf9", // azul claro
            "#a5d6a7", // verde claro
            "#ef9a9a"  // rojo claro
    };

    private Stage primaryStage;

    private final Map<String, Image> imageCache = new HashMap<>();
    private Image backImage = null;

    private final List<Path> candidateDirs = new ArrayList<>();

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("MemoriaFX - Inicio");

        initCandidateDirs();

        // Intentar precargar imagen de reverso si existe
        precargarBackImage();

        // Pantalla de inicio
        Label titulo = new Label("MemoriaFX");
        titulo.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        Button btnPorValor = new Button("Por Valor");
        Button btnPorValorColor = new Button("Por Valor y Color");
        Button btnSalir = new Button("Salir");

        btnPorValor.setPrefWidth(200);
        btnPorValorColor.setPrefWidth(200);
        btnSalir.setPrefWidth(200);

        btnPorValor.setOnAction(e -> iniciarFlujoSeleccionModalidad("Por Valor"));
        btnPorValorColor.setOnAction(e -> iniciarFlujoSeleccionModalidad("Por Valor y Color"));
        btnSalir.setOnAction(e -> primaryStage.close());

        VBox inicio = new VBox(12, titulo, btnPorValor, btnPorValorColor, btnSalir);
        inicio.setAlignment(Pos.CENTER);
        inicio.setPadding(new Insets(30));

        Scene sceneInicio = new Scene(inicio, 600, 400);
        primaryStage.setScene(sceneInicio);
        primaryStage.show();
    }

    private void initCandidateDirs() {
        String userDir = System.getProperty("user.dir");
        candidateDirs.add(Paths.get("src", "cardImages"));
        for (Path p : candidateDirs) {
            System.out.println("  - " + p.toAbsolutePath().toString());
        }
    }

    // Precarga imagen de reverso si existe en alguna ruta o en classpath
    private void precargarBackImage() {
        backImage = loadImageFromAnyLocation("Carta Atras.jpg");
        if (backImage != null) {
            System.out.println("[MemoriaFX] Reverso (back.jpg) cargado correctamente.");
        } else {
            System.out.println("[MemoriaFX] No se encontró back.jpg en las rutas candidatas ni en el classpath.");
        }
    }

    private void iniciarFlujoSeleccionModalidad(String modalidadSeleccionada) {
        // Dialogo para seleccionar cantidad de jugadores (2-4)
        Stage dialog = new Stage();
        dialog.initOwner(primaryStage);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Seleccionar jugadores");

        Label lbl = new Label("Cantidad de jugadores (2-4):");
        Spinner<Integer> spCantidad = new Spinner<>(2, 4, 2);
        spCantidad.setEditable(false);

        Button btnSiguiente = new Button("Siguiente");
        Button btnCancelar = new Button("Cancelar");

        HBox botones = new HBox(10, btnSiguiente, btnCancelar);
        botones.setAlignment(Pos.CENTER);

        VBox box = new VBox(10, lbl, spCantidad, botones);
        box.setPadding(new Insets(15));
        box.setAlignment(Pos.CENTER);

        Scene s = new Scene(box);
        dialog.setScene(s);
        dialog.show();

        btnCancelar.setOnAction(e -> dialog.close());
        btnSiguiente.setOnAction(e -> {
            int cantidad = spCantidad.getValue();
            dialog.close();
            solicitarNombresJugadores(modalidadSeleccionada, cantidad);
        });
    }

    // Solicita los nombres de los jugadores en una ventana
    private void solicitarNombresJugadores(String modalidad, int cantidad) {
        Stage dialog = new Stage();
        dialog.initOwner(primaryStage);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Nombres de jugadores");

        VBox campos = new VBox(8);
        List<TextField> camposNombres = new ArrayList<>();
        for (int i = 0; i < cantidad; i++) {
            TextField tf = new TextField();
            tf.setPromptText("Nombre jugador " + (i + 1));
            campos.getChildren().add(tf);
            camposNombres.add(tf);
        }

        Button btnIniciar = new Button("Iniciar Juego");
        Button btnCancelar = new Button("Cancelar");
        HBox botones = new HBox(8, btnIniciar, btnCancelar);
        botones.setAlignment(Pos.CENTER);

        VBox root = new VBox(10, new Label("Ingrese los nombres:"), campos, botones);
        root.setPadding(new Insets(12));
        Scene scene = new Scene(root);
        dialog.setScene(scene);
        dialog.show();

        btnCancelar.setOnAction(e -> dialog.close());

        btnIniciar.setOnAction(e -> {
            List<Jugador> jugadores = new ArrayList<>();
            for (TextField tf : camposNombres) {
                String nombre = tf.getText().trim();
                if (nombre.isEmpty()) nombre = "Jugador";
                jugadores.add(new Jugador(nombre, new ArrayList<>()));
            }
            dialog.close();
            crearYIniciarJuego(modalidad, jugadores);
        });
    }

    // Crea deck de 52 cartas, construye tablero 4x13 y la modalidad seleccionada
    private void crearYIniciarJuego(String modalidad, List<Jugador> jugadores) {
        // Crear baraja completa manualmente (52 cartas sin repeticiones)
        List<Carta> deck = new ArrayList<>(52);
        for (Palo palo : Palo.values()) {
            for (int v = 1; v <= 13; v++) {
                deck.add(new Carta(v, palo));
            }
        }
        Collections.shuffle(deck);

        // Construir tablero con 4 x 13 = 52 cartas (sin repetidos)
        tablero = new Tablero(FILAS, COLUMNAS, deck);

        // Crear instancia de juego según modalidad elegida
        if ("Por Valor y Color".equals(modalidad)) {
            juego = new ModalidadPorValorYColor(jugadores, tablero);
        } else {
            juego = new ModalidadPorValor(jugadores, tablero);
        }

        // Iniciar estado del juego
        juego.iniciar();

        // Construir UI principal del juego
        construirInterfazJuego(jugadores);
    }

    // Construye la UI con tablero a la izquierda y panel de jugadores a la derecha
    private void construirInterfazJuego(List<Jugador> jugadores) {
        primaryStage.setTitle("MemoriaFX - Juego");

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        // Grid del tablero en centro-izquierda
        grid = new GridPane();
        grid.setHgap(4);
        grid.setVgap(4);
        grid.setPadding(new Insets(8));
        grid.setAlignment(Pos.CENTER);
        botones = new Button[FILAS][COLUMNAS];

        for (int i = 0; i < FILAS; i++) {
            for (int j = 0; j < COLUMNAS; j++) {
                Button b = new Button();
                b.setPrefSize(70, 90);
                final int fi = i;
                final int cj = j;
                b.setOnAction(ev -> manejarClick(fi, cj));
                botones[i][j] = b;
                grid.add(b, j, i); // col, row
            }
        }

        refrescarTodo(); // inicializa la apariencia

        ScrollPane scrollGrid = new ScrollPane(grid);
        scrollGrid.setFitToWidth(true);
        scrollGrid.setFitToHeight(true);

        root.setCenter(scrollGrid);

        // Panel lateral con jugadores y estado
        panelJugadores = new VBox(10);
        panelJugadores.setPadding(new Insets(8));
        panelJugadores.setPrefWidth(300);
        panelJugadores.setStyle("-fx-background-color: #f3f3f3; -fx-border-color: #ccc;");
        Label tituloPanel = new Label("Jugadores");
        tituloPanel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        panelJugadores.getChildren().add(tituloPanel);

        // Crear entradas para cada jugador
        for (int idx = 0; idx < juego.getJugadores().size(); idx++) {
            Jugador j = juego.getJugadores().get(idx);
            HBox fila = crearFilaJugador(j, idx);
            panelJugadores.getChildren().add(fila);
        }

        // Estado general y botón volver a inicio
        lblEstado = new Label("Turno: " + juego.getJugadorActual().getNombre());
        Button btnVolver = new Button("Volver al inicio");
        btnVolver.setOnAction(e -> start(primaryStage));

        VBox pie = new VBox(8, lblEstado, btnVolver);
        pie.setAlignment(Pos.CENTER);
        pie.setPadding(new Insets(8));

        panelJugadores.getChildren().add(new Separator());
        panelJugadores.getChildren().add(pie);

        root.setRight(panelJugadores);

        Scene scene = new Scene(root, 1280, 900);
        primaryStage.setScene(scene);
        primaryStage.show();

        refrescarTodo();
        actualizarEstado();
    }

    // Crea la fila del panel lateral para un jugador: nombre, contador de pares y botón "Ver pares"
    private HBox crearFilaJugador(Jugador jugador, int indiceJugador) {
        Label lblNombre = new Label(jugador.getNombre());
        lblNombre.setPrefWidth(120);
        Label lblPares = new Label("Pares: " + jugador.getNumeroDePares());
        Button btnVerPares = new Button("Ver pares");

        // Cuando se presiona ver pares se muestra un diálogo con los pares (dos a dos)
        btnVerPares.setOnAction(e -> mostrarParesJugador(jugador));

        Region colorBox = crearColorBox(indiceJugador);

        HBox fila = new HBox(8, colorBox, lblNombre, lblPares, btnVerPares);
        fila.setAlignment(Pos.CENTER_LEFT);
        fila.setUserData(lblPares); // guardamos el label para actualizarlo luego
        return fila;
    }

    // Pequeño rectángulo indicando color del jugador
    private Region createColorRegion(int idx) {
        Region r = new Region();
        r.setPrefSize(14, 14);
        String color = PLAYER_COLORS[Math.min(idx, PLAYER_COLORS.length - 1)];
        r.setStyle("-fx-background-color: " + color + "; -fx-border-color: #888;");
        return r;
    }

    private Region crearColorBox(int idx) {
        return createColorRegion(idx);
    }

    // Mostrar pares del jugador en un diálogo: agrupar cartas en pares
    private void mostrarParesJugador(Jugador jugador) {
        List<Carta> ganadas = jugador.getCartasGanadas();
        StringBuilder sb = new StringBuilder();
        if (ganadas.isEmpty()) {
            sb.append("No ha realizado pares aún.");
        } else {
            for (int i = 0; i < ganadas.size(); i += 2) {
                Carta a = ganadas.get(i);
                Carta b = (i + 1 < ganadas.size()) ? ganadas.get(i + 1) : null;
                sb.append("- Par ").append((i / 2) + 1).append(": ");
                sb.append(a != null ? a.toString() : "null");
                sb.append("  |  ");
                sb.append(b != null ? b.toString() : "null");
                sb.append("\n");
            }
        }

        Alert al = new Alert(Alert.AlertType.INFORMATION);
        al.setTitle("Pares de " + jugador.getNombre());
        al.setHeaderText(null);
        al.setContentText(sb.toString());
        al.initOwner(primaryStage);
        al.showAndWait();
    }

    // Maneja el click en la casilla (fila, columna)
    private void manejarClick(int fila, int columna) {
        if (juego == null || tablero == null) return;
        Casilla cas = tablero.getCasillaEn(fila, columna);
        if (cas == null) return;
        if (cas.estaEmparejada()) return;

        int visibles = contarVolteadasNoEmparejadas();
        if (visibles == 0) {
            // Primer click: invocar al modelo (voltea y registra)
            juego.voltearCarta(fila, columna);
            refrescarTodo();
            actualizarEstado();
            revisarFin();
        } else if (visibles == 1) {
            // Segundo click: mostrar visualmente la carta inmediatamente,
            // esperar un instante y luego invocar la lógica del juego para evaluar par.
            tablero.voltearCarta(fila, columna); // para que el usuario vea la carta
            refrescarTodo();

            PauseTransition pause = new PauseTransition(Duration.millis(700));
            pause.setOnFinished(ev -> {
                juego.voltearCarta(fila, columna);
                refrescarTodo();
                actualizarEstado();
                revisarFin();
            });
            pause.play();
        } else {
        }
    }

    // Cuenta casillas volteadas y no emparejadas
    private int contarVolteadasNoEmparejadas() {
        int c = 0;
        for (int i = 0; i < FILAS; i++) {
            for (int j = 0; j < COLUMNAS; j++) {
                Casilla cas = tablero.getCasillaEn(i, j);
                if (cas != null && cas.estaVolteada() && !cas.estaEmparejada()) c++;
            }
        }
        return c;
    }

    // Refresca todos los botones del tablero
    private void refrescarTodo() {
        if (grid == null || botones == null) return;
        for (int i = 0; i < FILAS; i++) {
            for (int j = 0; j < COLUMNAS; j++) {
                refrescarBoton(i, j);
            }
        }
        // actualizar contador de pares en panel lateral
        if (panelJugadores != null && juego != null) {
            List<Jugador> lista = juego.getJugadores();
            for (int i = 0; i < lista.size(); i++) {
                int childIndex = i + 1;
                if (childIndex < panelJugadores.getChildren().size()) {
                    HBox fila = (HBox) panelJugadores.getChildren().get(childIndex);
                    Label lblPares = (Label) fila.getUserData();
                    if (lblPares != null) {
                        lblPares.setText("Pares: " + lista.get(i).getNumeroDePares());
                    } else {
                        for (javafx.scene.Node n : fila.getChildren()) {
                            if (n instanceof Label) {
                                Label possible = (Label) n;
                                if (possible.getText().startsWith("Pares:")) {
                                    possible.setText("Pares: " + lista.get(i).getNumeroDePares());
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Refresca apariencia de un único botón según estado de la casilla
    private void refrescarBoton(int fila, int columna) {
        Button b = botones[fila][columna];
        Casilla cas = tablero.getCasillaEn(fila, columna);
        if (cas == null) {
            b.setText("");
            b.setDisable(true);
            b.setStyle("");
            b.setGraphic(null);
            return;
        }
        Carta carta = cas.getCarta();

        if (cas.estaEmparejada()) {
            int owner = buscarJugadorQueTieneCarta(carta);
            String jugadorIndicador = owner >= 0 ? juego.getJugadores().get(owner).getNombre() : "";
            String color = owner >= 0 ? PLAYER_COLORS[Math.min(owner, PLAYER_COLORS.length - 1)] : "#d0ffd0";

            Image img = getImageForCarta(carta);
            if (img != null) {
                ImageView iv = new ImageView(img);
                iv.setPreserveRatio(true);
                iv.setFitWidth(60);
                iv.setFitHeight(80);
                b.setGraphic(iv);
                b.setText(jugadorIndicador.isEmpty() ? "" : " " + jugadorIndicador.charAt(0));
            } else {
                b.setGraphic(null);
                b.setText(carta.toString() + (jugadorIndicador.isEmpty() ? "" : " (" + jugadorIndicador.charAt(0) + ")"));
            }

            b.setDisable(true);
            b.setStyle("-fx-background-color: " + color + "; -fx-font-weight: bold;");
        } else if (cas.estaVolteada()) {
            // Mostrar la imagen de la carta
            Image img = getImageForCarta(carta);
            if (img != null) {
                ImageView iv = new ImageView(img);
                iv.setPreserveRatio(true);
                iv.setFitWidth(60);
                iv.setFitHeight(80);
                b.setGraphic(iv);
                b.setText("");
            } else {
                b.setGraphic(null);
                b.setText(carta.toString());
            }
            b.setDisable(false);
            b.setStyle("-fx-background-color: white; -fx-border-color: #555;");
        } else {
            // boca abajo: mostrar reverso si existe, sino fondo plano
            if (backImage != null) {
                ImageView iv = new ImageView(backImage);
                iv.setPreserveRatio(true);
                iv.setFitWidth(60);
                iv.setFitHeight(80);
                b.setGraphic(iv);
                b.setText("");
            } else {
                b.setGraphic(null);
                b.setText("");
            }
            b.setDisable(false);
            b.setStyle("-fx-background-color: darkslateblue;");
        }
    }

    // Busca el índice del jugador que tiene la carta en su lista de cartas ganadas.
    // Retorna -1 si no la tiene ninguno.
    private int buscarJugadorQueTieneCarta(Carta carta) {
        List<Jugador> lista = juego.getJugadores();
        for (int i = 0; i < lista.size(); i++) {
            Jugador j = lista.get(i);
            if (j.getCartasGanadas().contains(carta)) {
                return i;
            }
        }
        return -1;
    }

    // Obtiene la imagen asociada a una carta según el patrón de nombres de archivos.
    // Devuelve null si no se encuentra la imagen.
    private Image getImageForCarta(Carta carta) {
        if (carta == null) return null;
        // mapear valor para fichero: si getValor()==14 entonces usar 1 (As)
        int valorParaFichero = (carta.getValor() == 14) ? 1 : carta.getValor();
        String paloNombre = paloParaFichero(carta.getPalo());

        // Nombres pueden contener espacios según tu ejemplo: "Carta 1 Trebol.jpg"
        String fileNameJpg = String.format("Carta %d %s.jpg", valorParaFichero, paloNombre);
        String fileNamePng = String.format("Carta %d %s.png", valorParaFichero, paloNombre);

        Image img = loadImageFromAnyLocation(fileNameJpg);
        if (img != null) return img;
        img = loadImageFromAnyLocation(fileNamePng);
        if (img != null) return img;

        // variantes con guion bajo
        String fileNameJpgAlt = String.format("Carta_%d_%s.jpg", valorParaFichero, paloNombre);
        String fileNamePngAlt = String.format("Carta_%d_%s.png", valorParaFichero, paloNombre);
        img = loadImageFromAnyLocation(fileNameJpgAlt);
        if (img != null) return img;
        img = loadImageFromAnyLocation(fileNamePngAlt);
        if (img != null) return img;

        return null;
    }

    private Image loadImageFromAnyLocation(String fileName) {
        if (imageCache.containsKey(fileName)) {
            return imageCache.get(fileName);
        }

        for (Path dir : candidateDirs) {
            try {
                Path p = dir.resolve(fileName);
                if (Files.exists(p)) {
                    try (FileInputStream fis = new FileInputStream(p.toFile())) {
                        Image img = new Image(fis);
                        imageCache.put(fileName, img);
                        System.out.println("[MemoriaFX] Imagen cargada desde fichero: " + p.toAbsolutePath());
                        return img;
                    } catch (Exception ex) {
                        System.out.println("[MemoriaFX] Error al leer imagen desde " + p.toAbsolutePath() + " : " + ex.getMessage());
                    }
                } else {
                    // intentar versión con mayúsculas/minúsculas? no en este paso
                }
            } catch (Exception ex) {
                System.out.println("[MemoriaFX] Excepción comprobando " + dir + " : " + ex.getMessage());
            }
        }

        String resourcePath = "/cardImages/" + fileName;
        try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
            if (is != null) {
                Image img = new Image(is);
                imageCache.put(fileName, img);
                System.out.println("[MemoriaFX] Imagen cargada desde classpath: " + resourcePath);
                return img;
            } else {
                // intentar sin prefijo slash (no suele ser necesario, pero por si acaso)
                try (InputStream is2 = getClass().getResourceAsStream("cardImages/" + fileName)) {
                    if (is2 != null) {
                        Image img = new Image(is2);
                        imageCache.put(fileName, img);
                        System.out.println("[MemoriaFX] Imagen cargada desde classpath (sin slash): cardImages/" + fileName);
                        return img;
                    }
                } catch (Exception ignored) {}
            }
        } catch (Exception ex) {
            System.out.println("[MemoriaFX] Error cargando recurso classpath " + resourcePath + " : " + ex.getMessage());
        }

        try (InputStream is3 = Thread.currentThread().getContextClassLoader().getResourceAsStream("cardImages/" + fileName)) {
            if (is3 != null) {
                Image img = new Image(is3);
                imageCache.put(fileName, img);
                System.out.println("[MemoriaFX] Imagen cargada desde ClassLoader: cardImages/" + fileName);
                return img;
            }
        } catch (Exception ex) {
            System.out.println("[MemoriaFX] Error cargando con ClassLoader: " + ex.getMessage());
        }
S
        imageCache.put(fileName, null);
        System.out.println("[MemoriaFX] No se encontró imagen: " + fileName);
        return null;
    }

    // Mapea Palo enum a la palabra usada en los nombres de archivos
    private String paloParaFichero(Palo palo) {
        if (palo == null) return "";
        switch (palo) {
            case TREBOL:
                return "Trebol";
            case DIAMANTE:
                return "Diamante";
            case CORAZON:
                return "Corazon";
            case PICA:
                return "Pica";
            default:
                return palo.name();
        }
    }

    // Actualiza el label de estado con el jugador actual y sus pares
    private void actualizarEstado() {
        if (juego == null) return;
        Jugador actual = juego.getJugadorActual();
        lblEstado.setText("Turno: " + actual.getNombre() + " | Pares: " + actual.getNumeroDePares());
        // Actualizar contador de pares y refrescar botones emparejados con propietarios
        refrescarTodo();
    }

    // Revisa si el juego terminó y muestra diálogo con ganadores y tabla de resultados
    private void revisarFin() {
        if (juego.isTerminado()) {
            List<Jugador> ganadores = juego.obtenerGanadores();
            StringBuilder sb = new StringBuilder();
            if (ganadores.size() == 1) {
                sb.append("Ganador: ").append(ganadores.get(0).getNombre()).append("\n\n");
            } else {
                sb.append("Empate entre: ");
                for (int i = 0; i < ganadores.size(); i++) {
                    if (i > 0) sb.append(", ");
                    sb.append(ganadores.get(i).getNombre());
                }
                sb.append("\n\n");
            }
            sb.append("Resultados:\n");
            for (Jugador j : juego.getJugadores()) {
                sb.append(j.getNombre()).append(" - Pares: ").append(j.getNumeroDePares())
                        .append(" - Suma: ").append(j.getSumaTotalDeValores()).append("\n");
            }

            Alert al = new Alert(Alert.AlertType.INFORMATION);
            al.setTitle("Juego terminado");
            al.setHeaderText("Fin del juego");
            al.setContentText(sb.toString());
            al.initOwner(primaryStage);
            al.showAndWait();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}