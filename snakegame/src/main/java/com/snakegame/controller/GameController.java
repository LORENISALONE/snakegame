package com.snakegame.controller;

import com.snakegame.model.GameBoard;
import javafx.application.Platform;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.AudioClip;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class GameController {

    @FXML
    private Canvas gameCanvas;
    @FXML
    private Label scoreLabel, scoreLabel2, difficultyLabel;
    @FXML
    private Button pauseButton, menuButton, skinButton;

    private GameBoard gameBoard;
    private Timeline gameLoop;
    private boolean isPaused = false;
    private GraphicsContext gc;
    private boolean twoPlayer;
    private int previousScore = 0; // để kiểm tra ăn mồi

    private AudioClip eatSound;
    private MediaPlayer diePlayer;
    private boolean deathSoundPlayed = false; // đảm bảo âm thanh chết chỉ phát 1 lần

    private double cellSize = 25;
    private static final int BOARD_WIDTH = 20;
    private static final int BOARD_HEIGHT = 20;

    private String[] availableSkins = new String[]{"default"};
    private int currentSkinIndex = 0;
    private final Map<String, Image> images = new HashMap<>();

    // ========== INIT ==========
    public void initializeGame(GameBoard.Difficulty difficulty) {
        detectAvailableSkins();
        this.gameBoard = new GameBoard(difficulty);
        this.gc = gameCanvas.getGraphicsContext2D();
        setupGame(difficulty);
    }

    public void initializeGame(GameBoard.Difficulty difficulty, boolean twoPlayer) {
        detectAvailableSkins();
        this.twoPlayer = twoPlayer;
        this.gameBoard = new GameBoard(difficulty, twoPlayer);
        this.gc = gameCanvas.getGraphicsContext2D();
        setupGame(difficulty);
    }

    private void setupGame(GameBoard.Difficulty difficulty) {
        loadAssets();
        loadSounds();
        updateUI();
        setupKeyboardControls();
        setupGameLoop();
        drawGame();

        Platform.runLater(() -> {
            Scene scene = gameCanvas.getScene();
            if (scene != null) {
                scene.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
                    if (e.getCode() == KeyCode.SPACE) {
                        togglePause();
                        e.consume();
                    } else {
                        handleKeyPress(e);
                    }
                });
            }

            for (Button b : new Button[]{pauseButton, menuButton, skinButton}) {
                if (b != null) b.setFocusTraversable(false);
            }

            gameCanvas.setFocusTraversable(true);
            gameCanvas.requestFocus();
        });

        gameLoop.play();
    }

    // ========== ASSETS ==========
    private void loadAssets() {
        images.clear();
        String[] names = {
                "apple.png", "head_up.png", "head_down.png", "head_left.png", "head_right.png",
                "tail_up.png", "tail_down.png", "tail_left.png", "tail_right.png",
                "body_horizontal.png", "body_vertical.png",
                "body_topleft.png", "body_topright.png", "body_bottomleft.png", "body_bottomright.png"
        };

        String skinPath = (currentSkinIndex > 0)
                ? "/com/snakegame/assets/skin/" + availableSkins[currentSkinIndex] + "/"
                : "/com/snakegame/assets/";

        for (String n : names) {
            Image img = null;
            try (var is = getClass().getResourceAsStream(skinPath + n)) {
                if (is != null) img = new Image(is);
                else {
                    var def = getClass().getResourceAsStream("/com/snakegame/assets/" + n);
                    if (def != null) img = new Image(def);
                }
            } catch (Exception ignored) {}

            if (img == null) {
                File f1 = new File("src/main/resources/com/snakegame/assets/skin/" + availableSkins[currentSkinIndex] + "/" + n);
                File f2 = new File("src/main/resources/com/snakegame/assets/" + n);
                if (f1.exists()) img = new Image("file:" + f1.getPath());
                else if (f2.exists()) img = new Image("file:" + f2.getPath());
            }

            if (img != null) images.put(n, img);
            else System.out.println("⚠️ Không thể tải ảnh: " + n);
        }
    }

    private void detectAvailableSkins() {
        File skinDir = new File("src/main/resources/com/snakegame/assets/skin");
        if (!skinDir.exists()) skinDir = new File("src/main/java/com/snakegame/assets/skin");

        if (skinDir.exists() && skinDir.isDirectory()) {
            var dirs = skinDir.listFiles(File::isDirectory);
            if (dirs != null && dirs.length > 0) {
                availableSkins = new String[dirs.length + 1];
                availableSkins[0] = "default";
                for (int i = 0; i < dirs.length; i++) availableSkins[i + 1] = dirs[i].getName();
                return;
            }
        }
        availableSkins = new String[]{"default"};
    }

    @FXML
    private void changeSkin() {
        detectAvailableSkins();
        currentSkinIndex = (currentSkinIndex + 1) % availableSkins.length;
        loadAssets();
        drawGame();
        skinButton.setText("SKIN: " + availableSkins[currentSkinIndex]);
        Platform.runLater(() -> gameCanvas.requestFocus());
    }

    // ========== LOOP ==========
    private void setupGameLoop() {
        previousScore = gameBoard.getScore();

        gameLoop = new Timeline(new KeyFrame(Duration.millis(gameBoard.getDifficulty().getSpeed()), e -> {
            if (!isPaused && !gameBoard.isGameOver()) {
                gameBoard.update();

                int currentScore = gameBoard.getScore();
                if (currentScore > previousScore) {
                    if (eatSound != null) {
                        eatSound.play();
                    }
                }

                Platform.runLater(() -> {
                    drawGame();
                    updateUI();
                });

                if (gameBoard.isGameOver()) {
                    if (!deathSoundPlayed && diePlayer != null) {
                        diePlayer.stop();
                        diePlayer.play();
                        deathSoundPlayed = true;
                    }
                    gameLoop.stop();
                    Platform.runLater(this::showGameOver);
                }

                previousScore = currentScore;
            }
        }));
        gameLoop.setCycleCount(Timeline.INDEFINITE);
    }

    // ========== SOUND ==========
    private void loadSounds() {
        try {
            // Sử dụng AudioClip cho âm thanh ngắn (eat)
            var eatUrl = getClass().getResource("/sound/eat.wav");
            if (eatUrl != null) eatSound = new AudioClip(eatUrl.toExternalForm());

            // Sử dụng MediaPlayer cho âm thanh dài hoặc nhạc nền (die)
            var dieUrl = getClass().getResource("/sound/die.wav");
            if (dieUrl != null) diePlayer = new MediaPlayer(new Media(dieUrl.toExternalForm()));

        } catch (Exception e) {
            System.out.println("⚠️ Không thể tải âm thanh: " + e.getMessage());
        }
    }


    // ========== CONTROLS ==========
    private void setupKeyboardControls() {
        gameCanvas.setFocusTraversable(true);
        gameCanvas.setOnKeyPressed(this::handleKeyPress);
    }

    private void handleKeyPress(KeyEvent e) {
        if (gameBoard.isGameOver()) return;

        if (gameBoard.isTwoPlayer()) {
            switch (e.getCode()) {
                case W -> gameBoard.setDirection(GameBoard.Direction.UP);
                case S -> gameBoard.setDirection(GameBoard.Direction.DOWN);
                case A -> gameBoard.setDirection(GameBoard.Direction.LEFT);
                case D -> gameBoard.setDirection(GameBoard.Direction.RIGHT);
                case UP -> gameBoard.setDirectionP2(GameBoard.Direction.UP);
                case DOWN -> gameBoard.setDirectionP2(GameBoard.Direction.DOWN);
                case LEFT -> gameBoard.setDirectionP2(GameBoard.Direction.LEFT);
                case RIGHT -> gameBoard.setDirectionP2(GameBoard.Direction.RIGHT);
            }
        } else {
            switch (e.getCode()) {
                case UP -> gameBoard.setDirection(GameBoard.Direction.UP);
                case DOWN -> gameBoard.setDirection(GameBoard.Direction.DOWN);
                case LEFT -> gameBoard.setDirection(GameBoard.Direction.LEFT);
                case RIGHT -> gameBoard.setDirection(GameBoard.Direction.RIGHT);
            }
        }
    }

    // ========== DRAW ==========
    private void drawGame() {
        if (gc == null) return;
        double cw = gameCanvas.getWidth() / gameBoard.getBoardWidth();
        double ch = gameCanvas.getHeight() / gameBoard.getBoardHeight();
        cellSize = Math.min(cw, ch);

        drawCheckerboard(cellSize);
        drawFood();
        drawSnake(gameBoard.getSnake(), gameBoard.getDirection(), Color.web("#2E8B57"));
        if (gameBoard.getSnake2() != null)
            drawSnake(gameBoard.getSnake2(), gameBoard.getDirection2(), Color.web("#4169E1"));
        drawWall();
        drawObstacles();
    }

    private void drawObstacles() {
        var obs = gameBoard.getObstacles();
        if (obs == null) return;

        gc.setFill(Color.web("#555"));
        for (var o : obs) {
            gc.fillRect(o.getX() * cellSize, o.getY() * cellSize, cellSize, cellSize);
        }
    }

    private void drawCheckerboard(double cs) {
        Color c1 = Color.web("#1e2a31");
        Color c2 = Color.web("#25333b");
        for (int y = 0; y < gameBoard.getBoardHeight(); y++)
            for (int x = 0; x < gameBoard.getBoardWidth(); x++) {
                gc.setFill(((x + y) % 2 == 0) ? c1 : c2);
                gc.fillRect(x * cs, y * cs, cs, cs);
            }
    }

    private void drawFood() {
        Image apple = images.get("apple.png");

        var food1 = gameBoard.getFood();
        double fx1 = food1.getX() * cellSize;
        double fy1 = food1.getY() * cellSize;
        if (apple != null) gc.drawImage(apple, fx1 + 1, fy1 + 1, cellSize - 2, cellSize - 2);
        else {
            gc.setFill(Color.RED);
            gc.fillOval(fx1 + 2, fy1 + 2, cellSize - 4, cellSize - 4);
        }

        if (gameBoard.isTwoPlayer()) {
            var food2 = gameBoard.getFood2();
            double fx2 = food2.getX() * cellSize;
            double fy2 = food2.getY() * cellSize;
            if (apple != null) gc.drawImage(apple, fx2 + 1, fy2 + 1, cellSize - 2, cellSize - 2);
            else {
                gc.setFill(Color.RED);
                gc.fillOval(fx2 + 2, fy2 + 2, cellSize - 4, cellSize - 4);
            }
        }
    }

    private void drawSnake(java.util.List<GameBoard.Point> snake, GameBoard.Direction dir, Color fallback) {
        for (int i = 0; i < snake.size(); i++) {
            var seg = snake.get(i);
            double x = seg.getX() * cellSize;
            double y = seg.getY() * cellSize;
            Image img = null;

            if (i == 0) {
                switch (dir) {
                    case UP -> img = images.get("head_up.png");
                    case DOWN -> img = images.get("head_down.png");
                    case LEFT -> img = images.get("head_left.png");
                    case RIGHT -> img = images.get("head_right.png");
                }
            } else if (i == snake.size() - 1) {
                var prev = snake.get(i - 1);
                img = chooseTailImage(prev, seg);
            } else {
                var prev = snake.get(i - 1);
                var next = snake.get(i + 1);
                img = chooseBodyImage(prev, seg, next);
            }

            if (img != null)
                gc.drawImage(img, x + 1, y + 1, cellSize - 2, cellSize - 2);
            else {
                gc.setFill(fallback);
                gc.fillRoundRect(x + 1, y + 1, cellSize - 2, cellSize - 2, 5, 5);
            }
        }
    }

    private void drawWall() {
        var wall = gameBoard.getWall();
        if (wall == null) return;
        gc.setFill(Color.web("#444"));
        for (var b : wall)
            gc.fillRect(b.getX() * cellSize, b.getY() * cellSize, cellSize, cellSize);
    }

    private Image chooseTailImage(GameBoard.Point prev, GameBoard.Point tail) {
        int dx = tail.getX() - prev.getX();
        int dy = tail.getY() - prev.getY();
        if (dx == 1) return images.get("tail_right.png");
        if (dx == -1) return images.get("tail_left.png");
        if (dy == 1) return images.get("tail_down.png");
        return images.get("tail_up.png");
    }

    private Image chooseBodyImage(GameBoard.Point prev, GameBoard.Point curr, GameBoard.Point next) {
        int dx1 = prev.getX() - curr.getX();
        int dy1 = prev.getY() - curr.getY();
        int dx2 = next.getX() - curr.getX();
        int dy2 = next.getY() - curr.getY();

        if (dx1 == 0 && dx2 == 0) return images.get("body_vertical.png");
        if (dy1 == 0 && dy2 == 0) return images.get("body_horizontal.png");
        if ((dx1 == -1 && dy2 == -1) || (dy1 == -1 && dx2 == -1)) return images.get("body_topleft.png");
        if ((dx1 == 1 && dy2 == -1) || (dy1 == -1 && dx2 == 1)) return images.get("body_topright.png");
        if ((dx1 == -1 && dy2 == 1) || (dy1 == 1 && dx2 == -1)) return images.get("body_bottomleft.png");
        if ((dx1 == 1 && dy2 == 1) || (dy1 == 1 && dx2 == 1)) return images.get("body_bottomright.png");
        return images.get("body_horizontal.png");
    }

    // ========== UI ==========
    private void updateUI() {
        scoreLabel.setText("P1: " + gameBoard.getScore());
        if (scoreLabel2 != null && gameBoard.isTwoPlayer())
            scoreLabel2.setText("P2: " + gameBoard.getScore2());
        difficultyLabel.setText("Độ khó: " + switch (gameBoard.getDifficulty()) {
            case EASY -> "DỄ";
            case MEDIUM -> "TRUNG BÌNH";
            case HARD -> "KHÓ";
        });
    }

    @FXML
    private void togglePause() {
        isPaused = !isPaused;
        if (isPaused) {
            gameLoop.pause();
            pauseButton.setText("TIẾP TỤC");
        } else {
            gameLoop.play();
            pauseButton.setText("TẠM DỪNG");
        }
        Platform.runLater(() -> gameCanvas.requestFocus());
    }

    @FXML
    private void backToMenu() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Menu.fxml"));
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            ((Stage) menuButton.getScene().getWindow()).setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showGameOver() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/GameOver.fxml"));
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

            GameOverController ctrl = loader.getController();
            int score2 = gameBoard.isTwoPlayer() ? gameBoard.getScore2() : 0;
            ctrl.setGameData(gameBoard.getScore(), score2, gameBoard.getDifficulty(), twoPlayer);

            Stage stage = (Stage) gameCanvas.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
