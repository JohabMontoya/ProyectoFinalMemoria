package ClasesMemoria;

import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.*;
import java.util.*;

public class MemoriaFX extends Application {
    /**
     * Interfaz JavaFX que presenta el juego y opera sólo en la capa visual.
     * Consulta y manda acciones al JuegoController para la lógica.
     */
    private JuegoController controller;
    private GridPane grid;
    private Button[][] botones;
    private Label lblEstado;
    private HBox topBar;

    private static final int FILAS = 4;
    private static final int COLUMNAS = 13;

    private static final String[] PLAYER_COLORS = {
            "#FFD54F", "#90CAF9", "#A5D6A7", "#EF9A9A"
    };

    private Stage primaryStage;
    private final Map<String, Image> imageCache = new HashMap<>();
    private Image backImage = null;
    private final List<Path> candidateDirs = new ArrayList<>();
    private boolean endDialogShown = false;

    private static final double CARD_MIN_WIDTH = 64;
    private static final double CARD_MAX_WIDTH = 100;
    private static final double CARD_DEFAULT_WIDTH = 88;
    private static final double CARD_ASPECT = 1.36;

    private double currentImgFitW = 72;
    private double currentImgFitH = 100;

    /**
     * Punto de entrada JavaFX: muestra la pantalla inicial con selección de modo.
     */
    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("MemoriaFX - Memorama");

        candidateDirs.clear();
        candidateDirs.add(Paths.get("src", "cardImages"));

        precargarBackImage();

        VBox inicio = new VBox(14);
        inicio.setPadding(new Insets(28));
        inicio.setAlignment(Pos.CENTER);

        Label titulo = new Label("MemoriaFX");
        titulo.setStyle("-fx-font-size:28px; -fx-font-weight:bold; -fx-text-fill:#2E2E2E;");

        Button btnPorValor = new Button("Por Valor");
        Button btnPorValorColor = new Button("Por Valor y Color");
        Button btnSalir = new Button("Salir");

        btnPorValor.setPrefWidth(220);
        btnPorValorColor.setPrefWidth(220);
        btnSalir.setPrefWidth(220);

        btnPorValor.setOnAction(e -> iniciarFlujoSeleccionModalidad("Por Valor"));
        btnPorValorColor.setOnAction(e -> iniciarFlujoSeleccionModalidad("Por Valor y Color"));
        btnSalir.setOnAction(e -> primaryStage.close());

        inicio.getChildren().addAll(titulo, btnPorValor, btnPorValorColor, btnSalir);
        inicio.setStyle("-fx-background-color: linear-gradient(to bottom, #EDE7F6, #E3F2FD);");

        Scene scene = new Scene(inicio, 900, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Intenta precargar la imagen de reverso desde rutas conocidas.
     */
    private void precargarBackImage() {
        String[] posibles = {"back.jpg", "Carta Atras.jpg", "back.png", "Carta_Atras.jpg"};
        for (String n : posibles) {
            Image img = loadImageFromAnyLocation(n);
            if (img != null) {
                backImage = img;
                return;
            }
        }
    }

    /**
     * Muestra un diálogo para seleccionar modalidad y nombres de jugadores.
     */
    private void iniciarFlujoSeleccionModalidad(String modalidadSeleccionada) {
        Dialog<List<String>> dialog = new Dialog<>();
        dialog.initOwner(primaryStage);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Configuración de jugadores - " + modalidadSeleccionada);

        ButtonType iniciarType = new ButtonType("Iniciar Juego", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(iniciarType, ButtonType.CANCEL);

        VBox content = new VBox(10);
        content.setPadding(new Insets(12));

        HBox top = new HBox(8);
        top.setAlignment(Pos.CENTER_LEFT);
        Label lbl = new Label("Cantidad jugadores (2-4):");
        Spinner<Integer> sp = new Spinner<>(2, 4, 2);
        sp.setEditable(false);
        top.getChildren().addAll(lbl, sp);

        GridPane gridFields = new GridPane();
        gridFields.setHgap(8);
        gridFields.setVgap(8);
        List<TextField> nameFields = new ArrayList<>();
        updateNameFields(gridFields, nameFields, sp.getValue());

        ScrollPane scroll = new ScrollPane(gridFields);
        scroll.setFitToWidth(true);
        scroll.setPrefViewportHeight(200);

        sp.valueProperty().addListener((obs, oldV, newV) -> {
            updateNameFields(gridFields, nameFields, newV);
            dialog.getDialogPane().requestLayout();
            Platform.runLater(() -> { if (!nameFields.isEmpty()) nameFields.get(0).requestFocus(); });
        });

        content.getChildren().addAll(top, new Separator(), scroll);
        dialog.getDialogPane().setContent(content);
        dialog.setResizable(true);
        dialog.getDialogPane().setPrefSize(600, 360);

        Platform.runLater(() -> { if (!nameFields.isEmpty()) nameFields.get(0).requestFocus(); });

        dialog.setResultConverter(bt -> {
            if (bt == iniciarType) {
                List<String> res = new ArrayList<>();
                for (TextField tf : nameFields) res.add(tf.getText().trim());
                return res;
            }
            return null;
        });

        Optional<List<String>> opt = dialog.showAndWait();
        opt.ifPresent(nombres -> {
            List<Jugador> jugadores = new ArrayList<>();
            for (int i = 0; i < nombres.size(); i++) {
                String nom = nombres.get(i);
                if (nom == null || nom.isEmpty()) nom = "Jugador" + (i + 1);
                jugadores.add(new Jugador(nom, new ArrayList<>()));
            }
            endDialogShown = false;
            crearYIniciarJuego(modalidadSeleccionada, jugadores);
        });
    }

    /**
     * Actualiza los campos de nombre según la cantidad de jugadores.
     */
    private void updateNameFields(GridPane gridFields, List<TextField> nameFields, int cantidad) {
        gridFields.getChildren().clear();
        nameFields.clear();
        for (int i = 0; i < cantidad; i++) {
            Label l = new Label("Jugador " + (i + 1) + ":");
            TextField tf = new TextField();
            tf.setPromptText("Nombre jugador " + (i + 1));
            tf.setPrefWidth(360);
            gridFields.add(l, 0, i);
            gridFields.add(tf, 1, i);
            nameFields.add(tf);
        }
    }

    /**
     * Crea el controller, inicia la partida y construye la interfaz de juego.
     */
    private void crearYIniciarJuego(String modalidad, List<Jugador> jugadores) {
        controller = new JuegoController();
        controller.iniciarPartida(modalidad, jugadores, FILAS, COLUMNAS);
        endDialogShown = false;
        construirInterfazJuego(jugadores);
    }

    /**
     * Construye la interfaz del juego: barra superior, tablero y controles visuales.
     */
    private void construirInterfazJuego(List<Jugador> jugadores) {
        primaryStage.setTitle("MemoriaFX - Juego");

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));
        root.setBackground(new Background(new BackgroundFill(Color.web("#F9FBE7"), CornerRadii.EMPTY, Insets.EMPTY)));

        topBar = new HBox(12);
        topBar.setPadding(new Insets(8));
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setStyle("-fx-background-color: linear-gradient(to right, #FFFDE7, #FFF9C4); -fx-border-color: #FFE082; -fx-border-width: 0 0 2 0;");

        Label gameTitle = new Label("MemoriaFX");
        gameTitle.setStyle("-fx-font-size:18px; -fx-font-weight:bold; -fx-text-fill:#3E2723;");
        topBar.getChildren().add(gameTitle);
        topBar.getChildren().add(new Separator(javafx.geometry.Orientation.VERTICAL));

        for (int i = 0; i < jugadores.size(); i++) {
            Jugador j = jugadores.get(i);
            VBox playerBox = new VBox(4);
            playerBox.setAlignment(Pos.CENTER);
            playerBox.setPadding(new Insets(4));
            Region color = new Region();
            color.setPrefSize(16, 16);
            color.setStyle("-fx-background-color: " + PLAYER_COLORS[Math.min(i, PLAYER_COLORS.length - 1)] + "; -fx-border-color:#6D4C41;");
            Label name = new Label(j.getNombre());
            name.setStyle("-fx-font-weight:bold;");
            Label pares = new Label("Pares: " + j.getNumeroDePares());
            pares.setStyle("-fx-font-size:11px;");
            Button ver = new Button("Ver pares");
            int idx = i;
            ver.setOnAction(e -> mostrarParesConImagenes(controller.getJugadores().get(idx), "Pares de " + controller.getJugadores().get(idx).getNombre()));
            ver.setStyle("-fx-background-color:#FFE082; -fx-font-weight:bold;");
            playerBox.getChildren().addAll(color, name, pares, ver);
            playerBox.setUserData(pares);
            topBar.getChildren().add(playerBox);
        }

        Region leftGap = new Region();
        HBox.setHgrow(leftGap, Priority.ALWAYS);

        Region rightGap = new Region();
        HBox.setHgrow(rightGap, Priority.ALWAYS);

        lblEstado = new Label("Turno: " + (controller.getJugadorActual() != null ? controller.getJugadorActual().getNombre() : ""));
        lblEstado.setStyle("-fx-font-weight:bold; -fx-font-size:16px;");
        lblEstado.setAlignment(Pos.CENTER);

        Button btnSalirPartida = new Button("Salir");
        btnSalirPartida.setStyle("-fx-background-color:#FFCDD2; -fx-font-weight:bold;");
        btnSalirPartida.setOnAction(e -> {
            controller = null;
            endDialogShown = false;
            try {
                start(primaryStage);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        topBar.getChildren().addAll(leftGap, lblEstado, rightGap, btnSalirPartida);

        root.setTop(topBar);

        grid = new GridPane();
        grid.setHgap(6);
        grid.setVgap(6);
        grid.setPadding(new Insets(12));
        grid.setAlignment(Pos.CENTER);
        botones = new Button[FILAS][COLUMNAS];

        for (int r = 0; r < FILAS; r++) {
            for (int c = 0; c < COLUMNAS; c++) {
                Button b = new Button();
                b.setPrefSize(CARD_DEFAULT_WIDTH, CARD_DEFAULT_WIDTH * CARD_ASPECT);
                b.setStyle("-fx-background-color: linear-gradient(to bottom, #ECEFF1, #CFD8DC); -fx-border-color:#90A4AE;");
                final int fr = r, fc = c;
                b.setOnAction(ev -> this.manejarClick(fr, fc));
                b.setOnMouseEntered(ev -> { b.setScaleX(1.04); b.setScaleY(1.04); });
                b.setOnMouseExited(ev -> { b.setScaleX(1.0); b.setScaleY(1.0); });
                botones[r][c] = b;
                grid.add(b, c, r);
            }
        }

        ScrollPane centerScroll = new ScrollPane(grid);
        centerScroll.setFitToWidth(true);
        centerScroll.setFitToHeight(true);
        centerScroll.setStyle("-fx-background-color:transparent;");
        root.setCenter(centerScroll);

        Scene scene = new Scene(root, 1280, 840);
        primaryStage.setScene(scene);

        scene.widthProperty().addListener((obs, o, n) -> ajustarTamanoCartas());
        scene.heightProperty().addListener((obs, o, n) -> ajustarTamanoCartas());

        primaryStage.show();

        refrescarTodo();
        ajustarTamanoCartas();
    }

    /**
     * Maneja el click del usuario en la posición (fila, columna).
     * La UI muestra la carta y delega la evaluación al controller.
     */
    public void manejarClick(int fila, int columna) {
        if (controller == null || controller.getJuego() == null || controller.getTablero() == null) return;
        Casilla cas = controller.getTablero().getCasillaEn(fila, columna);
        if (cas == null) return;
        if (cas.estaEmparejada()) return;

        int visibles = contarVolteadasNoEmparejadas();
        if (visibles == 0) {
            controller.voltearCarta(fila, columna);
            refrescarTodo();
            actualizarEstado();
            revisarFin();
        } else if (visibles == 1) {
            controller.getTablero().voltearCarta(fila, columna);
            refrescarTodo();

            PauseTransition pause = new PauseTransition(Duration.millis(700));
            pause.setOnFinished(ev -> {
                controller.voltearCarta(fila, columna);
                refrescarTodo();
                actualizarEstado();
                revisarFin();
            });
            pause.play();
        }
    }

    /**
     * Cuenta cuántas casillas están volteadas y no emparejadas.
     */
    private int contarVolteadasNoEmparejadas() {
        int c = 0;
        Tablero tb = controller != null ? controller.getTablero() : null;
        if (tb == null) return 0;
        for (int i = 0; i < FILAS; i++) {
            for (int j = 0; j < COLUMNAS; j++) {
                Casilla cas = tb.getCasillaEn(i, j);
                if (cas != null && cas.estaVolteada() && !cas.estaEmparejada()) c++;
            }
        }
        return c;
    }

    /**
     * Ajusta el tamaño de los botones/cartas para que se muestren correctamente según la ventana.
     */
    private void ajustarTamanoCartas() {
        if (grid == null || botones == null || primaryStage == null || primaryStage.getScene() == null) return;

        double sceneW = primaryStage.getScene().getWidth();
        double sceneH = primaryStage.getScene().getHeight();

        double topBarHeight = (topBar != null) ? topBar.getHeight() : 80;
        double reserved = 40;
        double availableW = Math.max(200, sceneW - reserved);
        double availableH = Math.max(200, sceneH - topBarHeight - reserved);

        double hgap = grid.getHgap();
        double vgap = grid.getVgap();

        double cols = COLUMNAS;
        double rows = FILAS;

        double cellWidthByW = (availableW - (cols - 1) * hgap) / cols;
        double cellHeightByH = (availableH - (rows - 1) * vgap) / rows;

        double candidateWidth = Math.min(cellWidthByW, cellHeightByH / CARD_ASPECT);
        candidateWidth = Math.max(CARD_MIN_WIDTH, Math.min(CARD_MAX_WIDTH, candidateWidth));

        double btnW = candidateWidth;
        double btnH = candidateWidth * CARD_ASPECT;
        double imgW = btnW * 0.84;
        double imgH = btnH * 0.84;

        currentImgFitW = imgW;
        currentImgFitH = imgH;

        final double finalBtnW = btnW;
        final double finalBtnH = btnH;
        final double finalImgW = imgW;
        final double finalImgH = imgH;

        Platform.runLater(() -> {
            for (int r = 0; r < FILAS; r++) {
                for (int c = 0; c < COLUMNAS; c++) {
                    Button b = botones[r][c];
                    if (b == null) continue;
                    b.setPrefWidth(finalBtnW);
                    b.setPrefHeight(finalBtnH);
                    if (b.getGraphic() instanceof ImageView) {
                        ImageView iv = (ImageView) b.getGraphic();
                        iv.setFitWidth(finalImgW);
                        iv.setFitHeight(finalImgH);
                    }
                }
            }
        });
    }

    /**
     * Refresca toda la interfaz: botones y estado de jugadores.
     */
    private void refrescarTodo() {
        if (grid == null || botones == null) return;
        for (int r = 0; r < FILAS; r++) for (int c = 0; c < COLUMNAS; c++) refrescarBoton(r, c);

        if (topBar != null && controller != null && controller.getJuego() != null) {
            List<Jugador> lista = controller.getJugadores();
            int offset = 2;
            for (int i = 0; i < lista.size(); i++) {
                int idx = offset + i;
                if (idx < topBar.getChildren().size()) {
                    javafx.scene.Node node = topBar.getChildren().get(idx);
                    if (node instanceof VBox) {
                        VBox vb = (VBox) node;
                        Object ud = vb.getUserData();
                        Label paresLbl = null;
                        if (ud instanceof Label) paresLbl = (Label) ud;
                        if (paresLbl == null) {
                            for (javafx.scene.Node n : vb.getChildren())
                                if (n instanceof Label) {
                                    Label l = (Label) n;
                                    if (l.getText().startsWith("Pares:")) paresLbl = l;
                                }
                        }
                        if (paresLbl != null) paresLbl.setText("Pares: " + lista.get(i).getNumeroDePares());
                    }
                }
            }
        }

        if (controller != null && controller.getJugadorActual() != null && lblEstado != null) {
            lblEstado.setText("Turno: " + controller.getJugadorActual().getNombre());
        }

        if (controller != null && controller.isTerminado() && !endDialogShown) revisarFin();
    }

    /**
     * Refresca el botón en la celda (fila, columna) según el estado de la casilla.
     */
    private void refrescarBoton(int fila, int columna) {
        Button b = botones[fila][columna];
        if (b == null || controller == null) return;
        Tablero tb = controller.getTablero();
        if (tb == null) return;
        Casilla cas = tb.getCasillaEn(fila, columna);
        if (cas == null) {
            b.setGraphic(null);
            b.setText("");
            b.setDisable(true);
            return;
        }
        Carta carta = cas.getCarta();
        if (cas.estaEmparejada()) {
            int owner = buscarJugadorQueTieneCarta(carta);
            String color = owner >= 0 ? PLAYER_COLORS[Math.min(owner, PLAYER_COLORS.length - 1)] : "#E0F2F1";
            Image img = getImageForCarta(carta);
            if (img != null) {
                ImageView iv = new ImageView(img);
                iv.setFitWidth(currentImgFitW);
                iv.setFitHeight(currentImgFitH);
                iv.setPreserveRatio(true);
                b.setGraphic(iv);
                b.setText("");
            } else {
                b.setGraphic(null);
                b.setText(carta.toString());
            }
            b.setDisable(true);
            b.setStyle("-fx-background-color:" + color + "; -fx-border-color:#8D6E63; -fx-font-weight:bold;");
        } else if (cas.estaVolteada()) {
            Image img = getImageForCarta(carta);
            if (img != null) {
                ImageView iv = new ImageView(img);
                iv.setFitWidth(currentImgFitW);
                iv.setFitHeight(currentImgFitH);
                iv.setPreserveRatio(true);
                b.setGraphic(iv);
                b.setText("");
            } else {
                b.setGraphic(null);
                b.setText(carta.toString());
            }
            b.setDisable(false);
            b.setStyle("-fx-background-color:white; -fx-border-color:#90A4AE;");
        } else {
            if (backImage != null) {
                ImageView iv = new ImageView(backImage);
                iv.setFitWidth(currentImgFitW);
                iv.setFitHeight(currentImgFitH);
                iv.setPreserveRatio(true);
                b.setGraphic(iv);
                b.setText("");
            } else {
                b.setGraphic(null);
                b.setText("");
            }
            b.setDisable(false);
            b.setStyle("-fx-background-color:linear-gradient(to bottom, #ECEFF1, #CFD8DC); -fx-border-color:#B0BEC5;");
        }
    }

    /**
     * Busca el índice del jugador que contiene la carta dada.
     */
    private int buscarJugadorQueTieneCarta(Carta carta) {
        if (controller == null) return -1;
        List<Jugador> lista = controller.getJugadores();
        for (int i = 0; i < lista.size(); i++) {
            if (lista.get(i).getCartasGanadas().contains(carta)) return i;
        }
        return -1;
    }

    /**
     * Intenta obtener la imagen asociada a una carta probando varios nombres y rutas.
     */
    private Image getImageForCarta(Carta carta) {
        if (carta == null) return null;
        int valor = (carta.getValor() == 14) ? 1 : carta.getValor();
        String palo = paloParaFichero(carta.getPalo());
        String[] names = {
                String.format("Carta %d %s.jpg", valor, palo),
                String.format("Carta %d %s.png", valor, palo),
                String.format("Carta_%d_%s.jpg", valor, palo),
                String.format("Carta_%d_%s.png", valor, palo)
        };
        for (String n : names) {
            Image img = loadImageFromAnyLocation(n);
            if (img != null) return img;
        }
        return null;
    }

    /**
     * Intenta cargar una imagen desde disco o recursos y la almacena en caché.
     */
    private Image loadImageFromAnyLocation(String fileName) {
        if (imageCache.containsKey(fileName)) return imageCache.get(fileName);

        try {
            for (Path dir : candidateDirs) {
                Path p = dir.resolve(fileName);
                if (Files.exists(p) && Files.isRegularFile(p)) {
                    try (FileInputStream fis = new FileInputStream(p.toFile())) {
                        Image img = new Image(fis);
                        imageCache.put(fileName, img);
                        return img;
                    } catch (Exception ex) {
                    }
                }
            }
        } catch (Exception ex) {
        }

        String resource = "/cardImages/" + fileName;
        try (InputStream is = getClass().getResourceAsStream(resource)) {
            if (is != null) {
                Image img = new Image(is);
                imageCache.put(fileName, img);
                return img;
            }
        } catch (Exception ex) {
        }

        try (InputStream is2 = Thread.currentThread().getContextClassLoader().getResourceAsStream("cardImages/" + fileName)) {
            if (is2 != null) {
                Image img = new Image(is2);
                imageCache.put(fileName, img);
                return img;
            }
        } catch (Exception ex) {
        }

        imageCache.put(fileName, null);
        return null;
    }

    /**
     * Traduce el enumerado Palo a la parte de nombre usada en los ficheros.
     */
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

    /**
     * Revisa si la partida finalizó y muestra el diálogo final si procede.
     */
    private void revisarFin() {
        if (controller == null) return;
        if (!controller.isTerminado()) return;
        if (endDialogShown) return;
        endDialogShown = true;
        Platform.runLater(() -> {
            List<Jugador> ganadores = controller.obtenerGanadores();
            showEndGameDialogWithButtons(ganadores);
        });
    }

    /**
     * Muestra el diálogo de fin de partida con opciones y posibilidad de ver pares por jugador.
     */
    private void showEndGameDialogWithButtons(List<Jugador> ganadores) {
        Stage dialog = new Stage();
        dialog.initOwner(primaryStage);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Fin del juego");

        VBox content = new VBox(12);
        content.setPadding(new Insets(12));
        content.setAlignment(Pos.CENTER);

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
        header.setStyle("-fx-font-size:16px; -fx-font-weight:bold;");
        content.getChildren().add(header);

        for (Jugador j : controller.getJugadores()) {
            HBox row = new HBox(8);
            row.setAlignment(Pos.CENTER_LEFT);
            Label l = new Label(j.getNombre() + " - Pares: " + j.getNumeroDePares());
            Button ver = new Button("Ver pares");
            ver.setOnAction(e -> mostrarParesConImagenes(j, "Pares de " + j.getNombre()));
            row.getChildren().addAll(l, ver);
            content.getChildren().add(row);
        }

        HBox botones = new HBox(10);
        botones.setAlignment(Pos.CENTER);
        Button revancha = new Button("Revancha");
        Button modo = new Button("Seleccion de modo");
        Button salir = new Button("Salir");
        revancha.setOnAction(e -> { dialog.close(); try { start(primaryStage); } catch (Exception ex) { ex.printStackTrace(); }});
        modo.setOnAction(e -> { dialog.close(); try { start(primaryStage); } catch (Exception ex) { ex.printStackTrace(); }});
        salir.setOnAction(e -> { dialog.close(); Platform.exit(); });
        botones.getChildren().addAll(revancha, modo, salir);

        content.getChildren().add(new Separator());
        content.getChildren().add(botones);

        Scene s = new Scene(new ScrollPane(content), 920, 560);
        dialog.setScene(s);
        dialog.showAndWait();
    }

    /**
     * Muestra los pares de un jugador con opción de ordenarlos por valor o palo.
     */
    private void mostrarParesConImagenes(Jugador jugador, String title) {
        Stage dialog = new Stage();
        dialog.initOwner(primaryStage);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle(title);

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #FFF8E1, #FFF3E0);");

        Label header = new Label(title);
        header.setStyle("-fx-font-size:18px; -fx-font-weight:bold;");
        HBox top = new HBox(header);
        top.setAlignment(Pos.CENTER_LEFT);
        top.setPadding(new Insets(8));

        ComboBox<String> sortCombo = new ComboBox<>(FXCollections.observableArrayList("Sin ordenar", "Por valor", "Por palo"));
        sortCombo.setValue("Sin ordenar");
        HBox controls = new HBox(12, new Label("Orden:"), sortCombo);
        controls.setAlignment(Pos.CENTER_RIGHT);
        HBox topRow = new HBox(top, controls);
        HBox.setHgrow(top, Priority.ALWAYS);
        topRow.setAlignment(Pos.CENTER);
        topRow.setPadding(new Insets(6));
        root.setTop(topRow);

        List<Carta> ganadas = new ArrayList<>(jugador.getCartasGanadas());

        Runnable render = () -> {
            if (ganadas.isEmpty()) {
                root.setCenter(new Label("No ha realizado pares aún."));
                return;
            }

            List<Carta> copia = new ArrayList<>(ganadas);
            String sel = sortCombo.getValue();
            if ("Por valor".equals(sel)) {
                copia.sort(new ComparatorValor());
            } else if ("Por palo".equals(sel)) {
                copia.sort(new ComparatorPalo());
            }

            FlowPane fp = new FlowPane();
            fp.setHgap(12);
            fp.setVgap(12);
            fp.setPadding(new Insets(12));
            fp.setPrefWrapLength(1000);

            for (int i = 0; i < copia.size(); i += 2) {
                Carta a = copia.get(i);
                Carta b = (i + 1 < copia.size()) ? copia.get(i + 1) : null;

                VBox vb = new VBox(6);
                vb.setAlignment(Pos.CENTER);
                vb.setStyle("-fx-background-color: rgba(255,255,255,0.9); -fx-padding:8; -fx-border-color:#E0E0E0;");

                HBox cards = new HBox(8);
                cards.setAlignment(Pos.CENTER);

                Image ia = getImageForCarta(a);
                if (ia != null) {
                    ImageView iva = new ImageView(ia);
                    iva.setFitWidth(currentImgFitW * 1.2);
                    iva.setFitHeight(currentImgFitH * 1.2);
                    iva.setPreserveRatio(true);
                    cards.getChildren().add(iva);
                } else cards.getChildren().add(new Label(a.toString()));

                if (b != null) {
                    Image ib = getImageForCarta(b);
                    if (ib != null) {
                        ImageView ivb = new ImageView(ib);
                        ivb.setFitWidth(currentImgFitW * 1.2);
                        ivb.setFitHeight(currentImgFitH * 1.2);
                        ivb.setPreserveRatio(true);
                        cards.getChildren().add(ivb);
                    } else cards.getChildren().add(new Label(b.toString()));
                }

                vb.getChildren().add(cards);
                vb.getChildren().add(new Label("Par " + ((i / 2) + 1)));
                fp.getChildren().add(vb);
            }

            ScrollPane sp = new ScrollPane(fp);
            sp.setFitToWidth(true);
            root.setCenter(sp);
        };

        sortCombo.setOnAction(e -> render.run());

        render.run();

        Button cerrar = new Button("Cerrar");
        cerrar.setOnAction(e -> dialog.close());
        HBox bot = new HBox(cerrar);
        bot.setAlignment(Pos.CENTER);
        bot.setPadding(new Insets(8));
        root.setBottom(bot);

        Scene s = new Scene(root, 980, 700);
        dialog.setScene(s);
        dialog.showAndWait();
    }

    /**
     * Actualiza la etiqueta de turno con el jugador actual.
     */
    private void actualizarEstado() {
        if (lblEstado != null && controller != null && controller.getJugadorActual() != null) {
            lblEstado.setText("Turno: " + controller.getJugadorActual().getNombre());
        }
    }

    /**
     * Entrada principal para ejecutar la aplicación.
     */
    public static void main(String[] args) {
        launch(args);
    }
}