package ClasesMemoria;

import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.Stop;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.FileInputStream;
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
            "#FFD54F", // amarillo
            "#90CAF9", // azul claro
            "#A5D6A7", // verde claro
            "#EF9A9A"  // rojo claro
    };

    private Stage primaryStage;

    private final Map<String, Image> imageCache = new HashMap<>();
    private Image backImage = null;

    // Única ruta candidata en disco
    private final List<Path> candidateDirs = new ArrayList<>();

    // Bandera para evitar mostrar múltiples veces el diálogo de fin
    private boolean endDialogShown = false;

    // Tamaños más grandes para imágenes y botones
    private static final double CARD_BTN_WIDTH = 100;
    private static final double CARD_BTN_HEIGHT = 140;
    private static final double CARD_IMG_WIDTH = 92;
    private static final double CARD_IMG_HEIGHT = 128;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("MemoriaFX - Memorama Color");

        initCandidateDirs();
        precargarBackImage();

        // Inicio con fondo en gradiente y botones coloridos
        VBox inicio = new VBox(18);
        inicio.setPadding(new Insets(30));
        inicio.setAlignment(Pos.CENTER);

        Label titulo = new Label("MemoriaFX");
        titulo.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: white;");

        Button btnPorValor = new Button("Por Valor");
        Button btnPorValorColor = new Button("Por Valor y Color");
        Button btnSalir = new Button("Salir");

        styleMainButton(btnPorValor);
        styleMainButton(btnPorValorColor);
        styleMainButton(btnSalir);

        btnPorValor.setOnAction(e -> iniciarFlujoSeleccionModalidad("Por Valor"));
        btnPorValorColor.setOnAction(e -> iniciarFlujoSeleccionModalidad("Por Valor y Color"));
        btnSalir.setOnAction(e -> primaryStage.close());

        inicio.getChildren().addAll(titulo, btnPorValor, btnPorValorColor, btnSalir);

        // fondo en gradiente
        Stop[] stops = new Stop[] { new Stop(0, Color.web("#4A148C")), new Stop(1, Color.web("#0D47A1")) };
        LinearGradient lg = new LinearGradient(0,0,1,1,true, CycleMethod.NO_CYCLE, stops);
        inicio.setBackground(new Background(new BackgroundFill(lg, CornerRadii.EMPTY, Insets.EMPTY)));

        Scene sceneInicio = new Scene(inicio, 800, 560);
        primaryStage.setScene(sceneInicio);
        primaryStage.show();
    }

    private void styleMainButton(Button b) {
        b.setPrefWidth(260);
        b.setStyle("-fx-background-color: linear-gradient(to bottom, #FF8A65, #FF7043); -fx-text-fill: white; -fx-font-weight: bold;");
        b.setOnMouseEntered(e -> b.setStyle("-fx-background-color: linear-gradient(to bottom, #FFA270, #FF8A65); -fx-text-fill: white; -fx-font-weight: bold;"));
        b.setOnMouseExited(e -> b.setStyle("-fx-background-color: linear-gradient(to bottom, #FF8A65, #FF7043); -fx-text-fill: white; -fx-font-weight: bold;"));
    }

    private void initCandidateDirs() {
        candidateDirs.clear();
        candidateDirs.add(Paths.get("src", "cardImages"));
        System.out.println("[MemoriaFX] Ruta candidata para imágenes: " + candidateDirs.get(0).toAbsolutePath());
    }

    private void precargarBackImage() {
        String[] posibles = {"back.jpg", "Carta Atras.jpg", "back.png", "Carta_Atras.jpg"};
        for (String nombre : posibles) {
            Image img = loadImageFromAnyLocation(nombre);
            if (img != null) {
                backImage = img;
                System.out.println("[MemoriaFX] Reverso (" + nombre + ") cargado correctamente.");
                return;
            }
        }
        System.out.println("[MemoriaFX] No se encontró imagen de reverso en src/cardImages ni en classpath.");
    }

    /**
     * Diálogo unificado para elegir cantidad y nombres (dinámico).
     * Mantengo la versión con ScrollPane y campo dinámico, pero con tamaño mayor.
     */
    private void iniciarFlujoSeleccionModalidad(String modalidadSeleccionada) {
        Dialog<List<String>> dialog = new Dialog<>();
        dialog.initOwner(primaryStage);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Configuración de jugadores - " + modalidadSeleccionada);

        ButtonType btnIniciarType = new ButtonType("Iniciar Juego", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnIniciarType, ButtonType.CANCEL);

        VBox content = new VBox(12);
        content.setPadding(new Insets(14));

        HBox top = new HBox(8);
        top.setAlignment(Pos.CENTER_LEFT);
        Label lblCantidad = new Label("Cantidad de jugadores (2-4):");
        lblCantidad.setStyle("-fx-font-weight: bold;");
        Spinner<Integer> spCantidad = new Spinner<>(2, 4, 2);
        spCantidad.setEditable(false);
        top.getChildren().addAll(lblCantidad, spCantidad);

        GridPane gridFields = new GridPane();
        gridFields.setHgap(10);
        gridFields.setVgap(10);
        gridFields.setPadding(new Insets(6));

        List<TextField> nameFields = new ArrayList<>();
        updateNameFields(gridFields, nameFields, spCantidad.getValue());

        ScrollPane scrollPane = new ScrollPane(gridFields);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefViewportHeight(220);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        spCantidad.valueProperty().addListener((obs, oldV, newV) -> {
            updateNameFields(gridFields, nameFields, newV);
            double prefHeight = 80 + newV * 58;
            scrollPane.setPrefViewportHeight(Math.min(prefHeight, 400));
            dialog.getDialogPane().requestLayout();
            Platform.runLater(() -> { if (!nameFields.isEmpty()) nameFields.get(0).requestFocus(); });
        });

        content.getChildren().addAll(top, new Separator(), scrollPane);
        dialog.getDialogPane().setContent(content);

        dialog.setResizable(true);
        dialog.getDialogPane().setPrefSize(620, 380);

        Platform.runLater(() -> { if (!nameFields.isEmpty()) nameFields.get(0).requestFocus(); });

        dialog.setResultConverter(btn -> {
            if (btn == btnIniciarType) {
                List<String> nombres = new ArrayList<>();
                for (TextField tf : nameFields) nombres.add(tf.getText().trim());
                return nombres;
            }
            return null;
        });

        Optional<List<String>> result = dialog.showAndWait();
        result.ifPresent(nombres -> {
            List<Jugador> jugadores = new ArrayList<>();
            for (int i = 0; i < nombres.size(); i++) {
                String nombre = nombres.get(i);
                if (nombre == null || nombre.isEmpty()) nombre = "Jugador" + (i + 1);
                jugadores.add(new Jugador(nombre, new ArrayList<>()));
            }
            endDialogShown = false;
            crearYIniciarJuego(modalidadSeleccionada, jugadores);
        });
    }

    private void updateNameFields(GridPane gridFields, List<TextField> nameFields, int cantidad) {
        gridFields.getChildren().clear();
        nameFields.clear();
        for (int i = 0; i < cantidad; i++) {
            Label lbl = new Label("Jugador " + (i + 1) + ":");
            lbl.setStyle("-fx-font-weight: bold;");
            TextField tf = new TextField();
            tf.setPromptText("Nombre jugador " + (i + 1));
            tf.setPrefWidth(360);
            nameFields.add(tf);
            gridFields.add(lbl, 0, i);
            gridFields.add(tf, 1, i);
            GridPane.setFillWidth(tf, true);
        }
    }

    private void crearYIniciarJuego(String modalidad, List<Jugador> jugadores) {
        List<Carta> deck = new ArrayList<>(52);
        for (Palo palo : Palo.values()) {
            for (int v = 1; v <= 13; v++) deck.add(new Carta(v, palo));
        }
        Collections.shuffle(deck);
        tablero = new Tablero(FILAS, COLUMNAS, deck);

        if ("Por Valor y Color".equals(modalidad)) juego = new ModalidadPorValorYColor(jugadores, tablero);
        else juego = new ModalidadPorValor(jugadores, tablero);

        juego.iniciar();
        endDialogShown = false;
        construirInterfazJuego(jugadores);
    }

    private void construirInterfazJuego(List<Jugador> jugadores) {
        primaryStage.setTitle("MemoriaFX - Juego");

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(12));

        // Fondo principal con color suave
        root.setBackground(new Background(new BackgroundFill(Color.web("#F3E5F5"), CornerRadii.EMPTY, Insets.EMPTY)));

        // Grid del tablero
        grid = new GridPane();
        grid.setHgap(6);
        grid.setVgap(6);
        grid.setPadding(new Insets(12));
        grid.setAlignment(Pos.CENTER);
        botones = new Button[FILAS][COLUMNAS];

        for (int i = 0; i < FILAS; i++) {
            for (int j = 0; j < COLUMNAS; j++) {
                Button b = new Button();
                b.setPrefSize(CARD_BTN_WIDTH, CARD_BTN_HEIGHT);
                b.setStyle("-fx-background-color: linear-gradient(to bottom, #ECEFF1, #CFD8DC); -fx-border-color: #90A4AE;");
                final int fi = i;
                final int cj = j;
                b.setOnAction(ev -> manejarClick(fi, cj));
                botones[i][j] = b;
                grid.add(b, j, i);
            }
        }

        refrescarTodo();

        ScrollPane scrollGrid = new ScrollPane(grid);
        scrollGrid.setFitToWidth(true);
        scrollGrid.setFitToHeight(true);
        scrollGrid.setStyle("-fx-background-color: transparent;");

        root.setCenter(scrollGrid);

        // Panel lateral más colorido
        panelJugadores = new VBox(12);
        panelJugadores.setPadding(new Insets(12));
        panelJugadores.setPrefWidth(340);
        panelJugadores.setStyle("-fx-background-color: linear-gradient(to bottom, #FFFDE7, #FFF9C4); -fx-border-color: #FFD54F; -fx-border-width: 2;");
        Label tituloPanel = new Label("Jugadores");
        tituloPanel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #3E2723;");
        panelJugadores.getChildren().add(tituloPanel);

        for (int idx = 0; idx < juego.getJugadores().size(); idx++) {
            Jugador j = juego.getJugadores().get(idx);
            HBox fila = crearFilaJugador(j, idx);
            panelJugadores.getChildren().add(fila);
        }

        lblEstado = new Label("Turno: " + juego.getJugadorActual().getNombre());
        lblEstado.setStyle("-fx-font-weight: bold; -fx-text-fill: #4E342E;");

        Button btnVolver = new Button("Volver al inicio");
        btnVolver.setOnAction(e -> {
            imageCache.clear();
            try { start(primaryStage); } catch (Exception ex) { ex.printStackTrace(); }
        });
        btnVolver.setStyle("-fx-background-color:#B39DDB; -fx-text-fill: white; -fx-font-weight: bold;");

        VBox pie = new VBox(12, lblEstado, btnVolver);
        pie.setAlignment(Pos.CENTER);
        pie.setPadding(new Insets(8));

        panelJugadores.getChildren().add(new Separator());
        panelJugadores.getChildren().add(pie);

        root.setRight(panelJugadores);

        Scene scene = new Scene(root, 1360, 920);
        primaryStage.setScene(scene);
        primaryStage.show();

        refrescarTodo();
        actualizarEstado();
    }

    private HBox crearFilaJugador(Jugador jugador, int indiceJugador) {
        Region colorBox = crearColorBox(indiceJugador);

        Label lblNombre = new Label(jugador.getNombre());
        lblNombre.setPrefWidth(140);
        lblNombre.setStyle("-fx-font-weight: bold; -fx-text-fill: #3E2723;");

        Label lblPares = new Label("Pares: " + jugador.getNumeroDePares());
        lblPares.setStyle("-fx-text-fill:#5D4037;");

        Button btnVerPares = new Button("Ver pares");
        // Ahora "Ver pares" en la ronda usa la misma vista visual que el fin de ronda
        btnVerPares.setOnAction(e -> mostrarParesConImagenes(jugador, "Pares de " + jugador.getNombre()));
        btnVerPares.setStyle("-fx-background-color: #FFCC80; -fx-font-weight: bold;");

        HBox fila = new HBox(10, colorBox, lblNombre, lblPares, btnVerPares);
        fila.setAlignment(Pos.CENTER_LEFT);
        fila.setUserData(lblPares);
        return fila;
    }

    private Region crearColorBox(int idx) {
        Region r = new Region();
        r.setPrefSize(18, 18);
        String color = PLAYER_COLORS[Math.min(idx, PLAYER_COLORS.length - 1)];
        r.setStyle("-fx-background-color: " + color + "; -fx-border-color: #6D4C41; -fx-border-radius: 3; -fx-background-radius: 3;");
        return r;
    }

    // Mostrar pares en texto (queda para compatibilidad, no se usa para "Ver pares" ahora)
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

    /**
     * Mostrar pares con imágenes en formato grande y vistoso.
     * Usado tanto por botón "Ver pares" en la ronda como por la ventana final.
     */
    private void mostrarParesConImagenes(Jugador jugador, String title) {
        Stage dialog = new Stage();
        dialog.initOwner(primaryStage);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle(title);

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(12));
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #FFF8E1, #FFF3E0);");

        Label header = new Label(title);
        header.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill:#4E342E;");
        HBox top = new HBox(header);
        top.setPadding(new Insets(8));
        top.setAlignment(Pos.CENTER);
        root.setTop(top);

        List<Carta> ganadas = jugador.getCartasGanadas();
        if (ganadas.isEmpty()) {
            VBox center = new VBox(new Label("No ha realizado pares aún."));
            center.setAlignment(Pos.CENTER);
            root.setCenter(center);
        } else {
            FlowPane fp = new FlowPane();
            fp.setHgap(12);
            fp.setVgap(12);
            fp.setPadding(new Insets(12));
            fp.setPrefWrapLength(1000);

            for (int i = 0; i < ganadas.size(); i += 2) {
                Carta a = ganadas.get(i);
                Carta b = (i + 1 < ganadas.size()) ? ganadas.get(i + 1) : null;

                VBox vbPar = new VBox(6);
                vbPar.setAlignment(Pos.CENTER);
                vbPar.setStyle("-fx-background-color: rgba(255,255,255,0.85); -fx-padding: 8; -fx-border-radius: 6; -fx-background-radius: 6; -fx-border-color:#E0E0E0;");

                HBox hb = new HBox(8);
                hb.setAlignment(Pos.CENTER);

                Image ia = getImageForCarta(a);
                if (ia != null) {
                    ImageView iva = new ImageView(ia);
                    iva.setFitWidth(CARD_IMG_WIDTH + 20);
                    iva.setFitHeight(CARD_IMG_HEIGHT + 20);
                    iva.setPreserveRatio(true);
                    hb.getChildren().add(iva);
                } else {
                    Label la = new Label(a.toString());
                    la.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
                    hb.getChildren().add(la);
                }

                if (b != null) {
                    Image ib = getImageForCarta(b);
                    if (ib != null) {
                        ImageView ivb = new ImageView(ib);
                        ivb.setFitWidth(CARD_IMG_WIDTH + 20);
                        ivb.setFitHeight(CARD_IMG_HEIGHT + 20);
                        ivb.setPreserveRatio(true);
                        hb.getChildren().add(ivb);
                    } else {
                        Label lb = new Label(b.toString());
                        lb.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
                        hb.getChildren().add(lb);
                    }
                }

                vbPar.getChildren().add(hb);
                vbPar.getChildren().add(new Label("Par " + ((i / 2) + 1)));
                fp.getChildren().add(vbPar);
            }

            ScrollPane sp = new ScrollPane(fp);
            sp.setFitToWidth(true);
            root.setCenter(sp);
        }

        // Pie con botones
        HBox botones = new HBox(12);
        botones.setAlignment(Pos.CENTER);
        botones.setPadding(new Insets(10));

        Button btnCerrar = new Button("Cerrar");
        btnCerrar.setStyle("-fx-background-color:#BCAAA4; -fx-text-fill:white;");
        btnCerrar.setOnAction(e -> dialog.close());

        botones.getChildren().addAll(btnCerrar);
        root.setBottom(botones);

        Scene scene = new Scene(root, 1000, 700);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private void manejarClick(int fila, int columna) {
        if (juego == null || tablero == null) return;
        Casilla cas = tablero.getCasillaEn(fila, columna);
        if (cas == null) return;
        if (cas.estaEmparejada()) return;

        int visibles = contarVolteadasNoEmparejadas();
        if (visibles == 0) {
            juego.voltearCarta(fila, columna);
            refrescarTodo();
            actualizarEstado();
            revisarFin();
        } else if (visibles == 1) {
            // mostrar inmediatamente y luego procesar
            tablero.voltearCarta(fila, columna);
            refrescarTodo();
            PauseTransition pause = new PauseTransition(Duration.millis(700));
            pause.setOnFinished(ev -> {
                juego.voltearCarta(fila, columna);
                refrescarTodo();
                actualizarEstado();
                revisarFin();
            });
            pause.play();
        }
    }

    private int contarVolteadasNoEmparejadas() {
        int c = 0;
        if (tablero == null) return 0;
        for (int i = 0; i < FILAS; i++) {
            for (int j = 0; j < COLUMNAS; j++) {
                Casilla cas = tablero.getCasillaEn(i, j);
                if (cas != null && cas.estaVolteada() && !cas.estaEmparejada()) c++;
            }
        }
        return c;
    }

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
                    Object ud = fila.getUserData();
                    if (ud instanceof Label) {
                        Label lblPares = (Label) ud;
                        lblPares.setText("Pares: " + lista.get(i).getNumeroDePares());
                    } else {
                        // buscar label dentro del HBox
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

        if (juego != null && juego.isTerminado() && !endDialogShown) {
            revisarFin();
        }
    }

    // refrescarBoton implementado con los tamaños grandes
    private void refrescarBoton(int fila, int columna) {
        Button b = botones[fila][columna];
        if (b == null) return;
        Casilla cas = tablero.getCasillaEn(fila, columna);
        if (cas == null) {
            b.setText("");
            b.setGraphic(null);
            b.setDisable(true);
            return;
        }
        Carta carta = cas.getCarta();

        if (cas.estaEmparejada()) {
            int owner = buscarJugadorQueTieneCarta(carta);
            String jugadorIndicador = owner >= 0 ? juego.getJugadores().get(owner).getNombre() : "";
            String color = owner >= 0 ? PLAYER_COLORS[Math.min(owner, PLAYER_COLORS.length - 1)] : "#C8E6C9";

            Image img = getImageForCarta(carta);
            if (img != null) {
                ImageView iv = new ImageView(img);
                iv.setFitWidth(CARD_IMG_WIDTH);
                iv.setFitHeight(CARD_IMG_HEIGHT);
                iv.setPreserveRatio(true);
                b.setGraphic(iv);
                b.setText(jugadorIndicador.isEmpty() ? "" : " " + jugadorIndicador.charAt(0));
            } else {
                b.setGraphic(null);
                b.setText(carta.toString());
            }

            b.setDisable(true);
            b.setStyle("-fx-background-color: " + color + "; -fx-border-color: #8D6E63; -fx-font-weight: bold;");
        } else if (cas.estaVolteada()) {
            Image img = getImageForCarta(carta);
            if (img != null) {
                ImageView iv = new ImageView(img);
                iv.setFitWidth(CARD_IMG_WIDTH);
                iv.setFitHeight(CARD_IMG_HEIGHT);
                iv.setPreserveRatio(true);
                b.setGraphic(iv);
                b.setText("");
            } else {
                b.setGraphic(null);
                b.setText(carta.toString());
            }
            b.setDisable(false);
            b.setStyle("-fx-background-color: white; -fx-border-color: #90A4AE;");
        } else {
            if (backImage != null) {
                ImageView iv = new ImageView(backImage);
                iv.setFitWidth(CARD_IMG_WIDTH);
                iv.setFitHeight(CARD_IMG_HEIGHT);
                iv.setPreserveRatio(true);
                b.setGraphic(iv);
                b.setText("");
            } else {
                b.setGraphic(null);
                b.setText("");
            }
            b.setDisable(false);
            b.setStyle("-fx-background-color: linear-gradient(to bottom, #ECEFF1, #CFD8DC); -fx-border-color:#B0BEC5;");
        }
    }

    private int buscarJugadorQueTieneCarta(Carta carta) {
        if (juego == null) return -1;
        List<Jugador> lista = juego.getJugadores();
        for (int i = 0; i < lista.size(); i++) {
            Jugador j = lista.get(i);
            if (j.getCartasGanadas().contains(carta)) return i;
        }
        return -1;
    }

    private Image getImageForCarta(Carta carta) {
        if (carta == null) return null;
        int valorParaFichero = (carta.getValor() == 14) ? 1 : carta.getValor();
        String paloNombre = paloParaFichero(carta.getPalo());

        String fileNameJpg = String.format("Carta %d %s.jpg", valorParaFichero, paloNombre);
        String fileNamePng = String.format("Carta %d %s.png", valorParaFichero, paloNombre);

        Image img = loadImageFromAnyLocation(fileNameJpg);
        if (img != null) return img;
        img = loadImageFromAnyLocation(fileNamePng);
        if (img != null) return img;

        String fileNameJpgAlt = String.format("Carta_%d_%s.jpg", valorParaFichero, paloNombre);
        String fileNamePngAlt = String.format("Carta_%d_%s.png", valorParaFichero, paloNombre);
        img = loadImageFromAnyLocation(fileNameJpgAlt);
        if (img != null) return img;
        img = loadImageFromAnyLocation(fileNamePngAlt);
        if (img != null) return img;

        return null;
    }

    private Image loadImageFromAnyLocation(String fileName) {
        if (imageCache.containsKey(fileName)) return imageCache.get(fileName);

        if (candidateDirs != null) {
            for (Path dir : candidateDirs) {
                try {
                    Path p = dir.resolve(fileName);
                    if (Files.exists(p) && Files.isRegularFile(p)) {
                        try (FileInputStream fis = new FileInputStream(p.toFile())) {
                            Image img = new Image(fis);
                            imageCache.put(fileName, img);
                            System.out.println("[MemoriaFX] Imagen cargada desde fichero: " + p.toAbsolutePath());
                            return img;
                        } catch (Exception ex) {
                            System.out.println("[MemoriaFX] Error leyendo imagen desde " + p.toAbsolutePath() + " : " + ex.getMessage());
                        }
                    }
                } catch (Exception ex) {
                    System.out.println("[MemoriaFX] Excepción comprobando " + dir + " : " + ex.getMessage());
                }
            }
        }

        String resourcePath = "/cardImages/" + fileName;
        try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
            if (is != null) {
                Image img = new Image(is);
                imageCache.put(fileName, img);
                System.out.println("[MemoriaFX] Imagen cargada desde classpath: " + resourcePath);
                return img;
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

        imageCache.put(fileName, null);
        return null;
    }

    private String paloParaFichero(Palo palo) {
        if (palo == null) return "";
        switch (palo) {
            case TREBOL: return "Trebol";
            case DIAMANTE: return "Diamante";
            case CORAZON: return "Corazon";
            case PICA: return "Pica";
            default: return palo.name();
        }
    }

    private void actualizarEstado() {
        if (juego == null) return;
        Jugador actual = juego.getJugadorActual();
        lblEstado.setText("Turno: " + actual.getNombre() + " | Pares: " + actual.getNumeroDePares());
        refrescarTodo();
    }

    private void revisarFin() {
        if (juego == null) return;
        if (!juego.isTerminado()) return;
        if (endDialogShown) return;
        endDialogShown = true;
        Platform.runLater(() -> {
            List<Jugador> ganadores = juego.obtenerGanadores();
            showEndGameDialogWithButtons(ganadores);
        });
    }

    // Dialogo final reutilizando la vista grande y colorida
    private void showEndGameDialogWithButtons(List<Jugador> ganadores) {
        Stage dialog = new Stage();
        dialog.initOwner(primaryStage);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Fin del juego");

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(12));
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #E8F5E9, #E3F2FD);");

        Label header;
        if (ganadores.size() == 1) header = new Label("Ganador: " + ganadores.get(0).getNombre());
        else {
            StringBuilder sb = new StringBuilder("Empate entre: ");
            for (int i = 0; i < ganadores.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(ganadores.get(i).getNombre());
            }
            header = new Label(sb.toString());
        }
        header.setStyle("-fx-font-size:18px; -fx-font-weight:bold; -fx-text-fill:#2E7D32;");
        HBox top = new HBox(header);
        top.setAlignment(Pos.CENTER);
        top.setPadding(new Insets(6));
        root.setTop(top);

        VBox content = new VBox(10);
        content.setPadding(new Insets(8));

        // Lista de jugadores con botón "Ver pares" que reutiliza mostrarParesConImagenes
        for (Jugador j : juego.getJugadores()) {
            HBox fila = new HBox(12);
            fila.setAlignment(Pos.CENTER_LEFT);
            Label lbl = new Label(j.getNombre() + " - Pares: " + j.getNumeroDePares());
            Button btnVer = new Button("Ver pares");
            btnVer.setOnAction(e -> mostrarParesConImagenes(j, "Pares de " + j.getNombre()));
            btnVer.setStyle("-fx-background-color:#FFECB3; -fx-font-weight:bold;");
            fila.getChildren().addAll(lbl, btnVer);
            content.getChildren().add(fila);
        }

        ScrollPane sp = new ScrollPane(content);
        sp.setFitToWidth(true);
        root.setCenter(sp);

        HBox botones = new HBox(10);
        botones.setAlignment(Pos.CENTER);
        botones.setPadding(new Insets(10));
        Button btnRevancha = new Button("Revancha");
        Button btnSeleccionModo = new Button("Seleccion de modo");
        Button btnSalir = new Button("Salir");

        btnRevancha.setOnAction(e -> {
            dialog.close();
            try { start(primaryStage); } catch (Exception ex) { ex.printStackTrace(); }
        });
        btnSeleccionModo.setOnAction(e -> {
            dialog.close();
            imageCache.clear();
            try { start(primaryStage); } catch (Exception ex) { ex.printStackTrace(); }
        });
        btnSalir.setOnAction(e -> { dialog.close(); Platform.exit(); });

        botones.getChildren().addAll(btnRevancha, btnSeleccionModo, btnSalir);
        root.setBottom(botones);

        Scene scene = new Scene(root, 980, 680);
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}