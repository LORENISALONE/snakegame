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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class GameController {

    @FXML
    private Canvas gameCanvas;

    @FXML
    private Label scoreLabel;

    @FXML
    private Label scoreLabel2;

    @FXML
    private Label difficultyLabel;

    @FXML
    private Button pauseButton;

    @FXML
    private Button menuButton;

    private GameBoard gameBoard;
    private Timeline gameLoop;
    private boolean isPaused = false;
    private GraphicsContext gc;

    private double cellSize = 25;
    private static final int BOARD_WIDTH = 20;
    private static final int BOARD_HEIGHT = 20;

    // Images
    private Map<String, Image> images = new HashMap<>();

    public void initializeGame(GameBoard.Difficulty difficulty) {
        this.gameBoard = new GameBoard(difficulty);
        this.gc = gameCanvas.getGraphicsContext2D();

        loadAssets();

        // Compute dynamic cell size to fill canvas
        double width = gameCanvas.getWidth();
        double height = gameCanvas.getHeight();
        double cw = width / BOARD_WIDTH;
        double ch = height / BOARD_HEIGHT;
        cellSize = Math.min(cw, ch);

        drawGameScaled(cellSize);

        // Update UI
        updateUI();

        // Set up game loop
        setupGameLoop();

        // Set up keyboard controls
        setupKeyboardControls();

        // Draw initial frame
        drawGame();

        // Ensure focus and scene-level key handling after scene is set
        Platform.runLater(() -> {
            Scene scene = gameCanvas.getScene();
            if (scene != null) {
                // Use event filter so SPACE is caught before buttons consume it
                scene.addEventFilter(KeyEvent.KEY_PRESSED, this::handleKeyPress);
                scene.addEventFilter(KeyEvent.KEY_TYPED, e -> {
                    if (" ".equals(e.getCharacter())) {
                        togglePause();
                        e.consume();
                    }
                });
            }
            // Avoid SPACE activating buttons
            if (pauseButton != null) pauseButton.setFocusTraversable(false);
            if (menuButton != null) menuButton.setFocusTraversable(false);
            gameCanvas.requestFocus();
        });

        // Start the game
        gameLoop.play();

    }

    public void initializeGame(GameBoard.Difficulty difficulty, boolean twoPlayer) {
        this.gameBoard = new GameBoard(difficulty, twoPlayer);
        this.gc = gameCanvas.getGraphicsContext2D();

        loadAssets();

        // Set up canvas size
        gameCanvas.setWidth(BOARD_WIDTH * cellSize);
        gameCanvas.setHeight(BOARD_HEIGHT * cellSize);

        // Update UI
        updateUI();

        // Set up game loop
        setupGameLoop();

        // Set up keyboard controls
        setupKeyboardControls();

        // Draw initial frame
        drawGame();

        // Ensure focus and scene-level key handling after scene is set
        Platform.runLater(() -> {
            Scene scene = gameCanvas.getScene();
            if (scene != null) {
                // Use event filter so SPACE is caught before buttons consume it
                scene.addEventFilter(KeyEvent.KEY_PRESSED, this::handleKeyPress);
                scene.addEventFilter(KeyEvent.KEY_TYPED, e -> {
                    if (" ".equals(e.getCharacter())) {
                        togglePause();
                        e.consume();
                    }
                });
            }
            // Avoid SPACE activating buttons
            if (pauseButton != null) pauseButton.setFocusTraversable(false);
            if (menuButton != null) menuButton.setFocusTraversable(false);
            gameCanvas.requestFocus();
        });

        // Start the game
        gameLoop.play();
    }

    private void loadAssets() {
        // Load images from assets package (in resources/classpath) via resource stream
        String base = "/com/snakegame/assets/";
        String[] names = new String[]{
                "apple.png",
                "head_up.png",
                "head_down.png",
                "head_left.png",
                "head_right.png",
                "tail_up.png",
                "tail_down.png",
                "tail_left.png",
                "tail_right.png",
                "body_horizontal.png",
                "body_vertical.png",
                "body_topleft.png",
                "body_topright.png",
                "body_bottomleft.png",
                "body_bottomright.png"
        };

        for (String n : names) {
            Image img = null;
            // Try classpath resource first (works when assets are in resources or inside jar)
            try {
                var is = getClass().getResourceAsStream(base + n);
                if (is != null) {
                    img = new Image(is);
                }
            } catch (Exception ignored) {
            }

            // Fallback: try filesystem paths (useful when running from IDE and assets are under src/main/java or src/main/resources)
            if (img == null) {
                String[] fallbackPaths = new String[]{
                        "src/main/resources/com/snakegame/assets/" + n,
                        "src/main/java/com/snakegame/assets/" + n,
                        "./src/main/resources/com/snakegame/assets/" + n,
                        "./src/main/java/com/snakegame/assets/" + n
                };
                for (String p : fallbackPaths) {
                    try {
                        // Use file: URL so Image can load from filesystem
                        Image tryImg = new Image("file:" + p);
                        if (!tryImg.isError()) {
                            img = tryImg;
                            break;
                        }
                    } catch (Exception ignored) {
                    }
                }
            }

            if (img != null) {
                images.put(n, img);
            } else {
                System.out.println("Could not load asset: " + n + " from classpath or fallback paths");
            }
        }
    }

    private void setupGameLoop() {
        gameLoop = new Timeline(new KeyFrame(Duration.millis(gameBoard.getDifficulty().getSpeed()), e -> {
            if (!isPaused && !gameBoard.isGameOver()) {
                gameBoard.update();

                // Dùng Platform.runLater để render trên JavaFX thread → tránh lệch frame
                Platform.runLater(() -> {
                    drawGame();
                    updateUI();
                });

                if (gameBoard.isGameOver()) {
                    gameLoop.stop();
                    Platform.runLater(this::showGameOver);
                }
            }
        }));
        gameLoop.setCycleCount(Timeline.INDEFINITE);
    }


    private void setupKeyboardControls() {
        gameCanvas.setFocusTraversable(true);
        gameCanvas.setOnKeyPressed(this::handleKeyPress);
    }

    private void handleKeyPress(KeyEvent event) {
        // SPACE should work regardless of game state
        if (event.getCode() == KeyCode.SPACE) {
            togglePause();
            return;
        }

        if (gameBoard.isGameOver()) return;

        switch (event.getCode()) {
            case W:
                gameBoard.setDirection(GameBoard.Direction.UP);
                break;
            case S:
                gameBoard.setDirection(GameBoard.Direction.DOWN);
                break;
            case A:
                gameBoard.setDirection(GameBoard.Direction.LEFT);
                break;
            case D:
                gameBoard.setDirection(GameBoard.Direction.RIGHT);
                break;
            case UP:
                if (gameBoard.isTwoPlayer()) gameBoard.setDirectionP2(GameBoard.Direction.UP);
                else gameBoard.setDirection(GameBoard.Direction.UP);
                break;
            case DOWN:
                if (gameBoard.isTwoPlayer()) gameBoard.setDirectionP2(GameBoard.Direction.DOWN);
                else gameBoard.setDirection(GameBoard.Direction.DOWN);
                break;
            case LEFT:
                if (gameBoard.isTwoPlayer()) gameBoard.setDirectionP2(GameBoard.Direction.LEFT);
                else gameBoard.setDirection(GameBoard.Direction.LEFT);
                break;
            case RIGHT:
                if (gameBoard.isTwoPlayer()) gameBoard.setDirectionP2(GameBoard.Direction.RIGHT);
                else gameBoard.setDirection(GameBoard.Direction.RIGHT);
                break;
        }
    }

    private void drawGame() {
        if (gc == null) return;

        // Recompute cell size in case canvas resized
        double width = gameCanvas.getWidth();
        double height = gameCanvas.getHeight();
        double cw = width / gameBoard.getBoardWidth();
        double ch = height / gameBoard.getBoardHeight();
        cellSize = Math.min(cw, ch);

        // Draw checkerboard
        drawCheckerboard(cellSize);

        // Draw food as image if available
        var food = gameBoard.getFood();
        double fx = food.getX() * cellSize;
        double fy = food.getY() * cellSize;
        Image apple = images.get("apple.png");
        if (apple != null) {
            gc.drawImage(apple, fx + 1, fy + 1, cellSize - 2, cellSize - 2);
        } else {
            gc.setFill(Color.web("#DC143C"));
            gc.fillOval(fx + 2, fy + 2, cellSize - 4, cellSize - 4);
        }

        // Draw snake P1 using images
        var snake = gameBoard.getSnake();
        for (int i = 0; i < snake.size(); i++) {
            var segment = snake.get(i);
            double x = segment.getX() * cellSize;
            double y = segment.getY() * cellSize;

            if (i == 0) {
                // head based on direction
                Image headImg = null;
                switch (gameBoard.getDirection()) {
                    case UP: headImg = images.get("head_up.png"); break;
                    case DOWN: headImg = images.get("head_down.png"); break;
                    case LEFT: headImg = images.get("head_left.png"); break;
                    case RIGHT: headImg = images.get("head_right.png"); break;
                }
                if (headImg != null) gc.drawImage(headImg, x + 1, y + 1, cellSize - 2, cellSize - 2);
                else {
                    gc.setFill(Color.web("#228B22"));
                    gc.fillRoundRect(x + 1, y + 1, cellSize - 2, cellSize - 2, 5, 5);
                }
            } else if (i == snake.size() - 1) {
                // tail: choose orientation based on previous segment
                var prev = snake.get(i - 1);
                Image tailImg = chooseTailImage(prev, segment);
                if (tailImg != null) gc.drawImage(tailImg, x + 1, y + 1, cellSize - 2, cellSize - 2);
                else {
                    gc.setFill(Color.web("#2E8B57"));
                    gc.fillRoundRect(x + 1, y + 1, cellSize - 2, cellSize - 2, 5, 5);
                }
            } else {
                // body: choose between straight or corner
                var prev = snake.get(i - 1);
                var next = snake.get(i + 1);
                Image bodyImg = chooseBodyImage(prev, segment, next);
                if (bodyImg != null) gc.drawImage(bodyImg, x + 1, y + 1, cellSize - 2, cellSize - 2);
                else {
                    gc.setFill(Color.web("#2E8B57"));
                    gc.fillRoundRect(x + 1, y + 1, cellSize - 2, cellSize - 2, 5, 5);
                }
            }
        }

        // Draw snake P2 if enabled
        var snake2 = gameBoard.getSnake2();
        if (snake2 != null) {
            for (int i = 0; i < snake2.size(); i++) {
                var segment = snake2.get(i);
                double x = segment.getX() * cellSize;
                double y = segment.getY() * cellSize;

                if (i == 0) {
                    Image headImg = null;
                    switch (gameBoard.getDirection2()) {
                        case UP: headImg = images.get("head_up.png"); break;
                        case DOWN: headImg = images.get("head_down.png"); break;
                        case LEFT: headImg = images.get("head_left.png"); break;
                        case RIGHT: headImg = images.get("head_right.png"); break;
                    }
                    if (headImg != null) gc.drawImage(headImg, x + 1, y + 1, cellSize - 2, cellSize - 2);
                    else {
                        gc.setFill(Color.web("#1E90FF"));
                        gc.fillRoundRect(x + 1, y + 1, cellSize - 2, cellSize - 2, 5, 5);
                    }
                } else if (i == snake2.size() - 1) {
                    var prev = snake2.get(i - 1);
                    Image tailImg = chooseTailImage(prev, segment);
                    if (tailImg != null) gc.drawImage(tailImg, x + 1, y + 1, cellSize - 2, cellSize - 2);
                    else {
                        gc.setFill(Color.web("#4169E1"));
                        gc.fillRoundRect(x + 1, y + 1, cellSize - 2, cellSize - 2, 5, 5);
                    }
                } else {
                    var prev = snake2.get(i - 1);
                    var next = snake2.get(i + 1);
                    Image bodyImg = chooseBodyImage(prev, segment, next);
                    if (bodyImg != null) gc.drawImage(bodyImg, x + 1, y + 1, cellSize - 2, cellSize - 2);
                    else {
                        gc.setFill(Color.web("#4169E1"));
                        gc.fillRoundRect(x + 1, y + 1, cellSize - 2, cellSize - 2, 5, 5);
                    }
                }
            }
        }

        // Draw wall (for 2-player mode)
        var wall = gameBoard.getWall();
        if (wall != null) {
            gc.setFill(Color.web("#444444"));
            gc.setStroke(Color.web("#666666"));
            gc.setLineWidth(1);
            for (var block : wall) {
                gc.fillRect(block.getX() * cellSize, block.getY() * cellSize, cellSize, cellSize);
                gc.strokeRect(block.getX() * cellSize, block.getY() * cellSize, cellSize, cellSize);
            }
        }
    }

    // Draw a more contrasted checkerboard background using two distinct colors
    private void drawCheckerboard(double cellSize) {
        if (gc == null) return;
        int cols = (gameBoard != null) ? gameBoard.getBoardWidth() : BOARD_WIDTH;
        int rows = (gameBoard != null) ? gameBoard.getBoardHeight() : BOARD_HEIGHT;

        Color color1 = Color.web("#1e2a31"); // darker
        Color color2 = Color.web("#25333b"); // lighter

        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                gc.setFill(((x + y) % 2 == 0) ? color1 : color2);
                gc.fillRect(x * cellSize, y * cellSize, cellSize, cellSize);
            }
        }
    }

    private Image chooseTailImage(GameBoard.Point prev, GameBoard.Point tail) {
        int dx = tail.getX() - prev.getX();
        int dy = tail.getY() - prev.getY();

        // wrap-around
        if (dx > 1) dx -= gameBoard.getBoardWidth();
        if (dx < -1) dx += gameBoard.getBoardWidth();
        if (dy > 1) dy -= gameBoard.getBoardHeight();
        if (dy < -1) dy += gameBoard.getBoardHeight();

        if (dx == 1 && dy == 0) return images.get("tail_right.png");
        if (dx == -1 && dy == 0) return images.get("tail_left.png");
        if (dx == 0 && dy == 1) return images.get("tail_down.png");
        if (dx == 0 && dy == -1) return images.get("tail_up.png");

        return images.get("tail_right.png");
    }

    private Image chooseBodyImage(GameBoard.Point prev, GameBoard.Point curr, GameBoard.Point next) {
        int dxPrev = prev.getX() - curr.getX();
        int dyPrev = prev.getY() - curr.getY();
        int dxNext = next.getX() - curr.getX();
        int dyNext = next.getY() - curr.getY();

        // wrap-around fix
        if (dxPrev > 1) dxPrev -= gameBoard.getBoardWidth();
        if (dxPrev < -1) dxPrev += gameBoard.getBoardWidth();
        if (dyPrev > 1) dyPrev -= gameBoard.getBoardHeight();
        if (dyPrev < -1) dyPrev += gameBoard.getBoardHeight();
        if (dxNext > 1) dxNext -= gameBoard.getBoardWidth();
        if (dxNext < -1) dxNext += gameBoard.getBoardWidth();
        if (dyNext > 1) dyNext -= gameBoard.getBoardHeight();
        if (dyNext < -1) dyNext += gameBoard.getBoardHeight();

        // Straight lines
        if (dxPrev == 0 && dxNext == 0) return images.get("body_vertical.png");
        if (dyPrev == 0 && dyNext == 0) return images.get("body_horizontal.png");

        // Corners (adjusted for correct sprite orientation)
        if ((dxPrev == -1 && dyNext == -1) || (dyPrev == -1 && dxNext == -1)) return images.get("body_topleft.png");
        if ((dxPrev == 1 && dyNext == -1) || (dyPrev == -1 && dxNext == 1)) return images.get("body_topright.png");
        if ((dxPrev == -1 && dyNext == 1) || (dyPrev == 1 && dxNext == -1)) return images.get("body_bottomleft.png");
        if ((dxPrev == 1 && dyNext == 1) || (dyPrev == 1 && dxNext == 1)) return images.get("body_bottomright.png");

        return images.get("body_horizontal.png");
    }



    private void updateUI() {
        scoreLabel.setText("P1: " + gameBoard.getScore());
        if (scoreLabel2 != null && gameBoard.isTwoPlayer()) {
            scoreLabel2.setText("P2: " + gameBoard.getScore2());
        }
        difficultyLabel.setText("Độ khó: " + getDifficultyText(gameBoard.getDifficulty()));
    }

    private String getDifficultyText(GameBoard.Difficulty difficulty) {
        switch (difficulty) {
            case EASY: return "DỄ";
            case MEDIUM: return "TRUNG BÌNH";
            case HARD: return "KHÓ";
            default: return "DỄ";
        }
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
        // Re-focus canvas so arrow keys work after clicking buttons
        gameCanvas.requestFocus();
    }

    @FXML
    private void backToMenu() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Menu.fxml"));
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

            Stage stage = (Stage) menuButton.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showGameOver() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/GameOver.fxml"));
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

            GameOverController gameOverController = loader.getController();
            gameOverController.setGameData(gameBoard.getScore(), gameBoard.getDifficulty());

            // Lấy stage từ gameCanvas (luôn tồn tại)
            Stage stage = (Stage) gameCanvas.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void drawGameScaled(double cellSize) {
        // Draw checkerboard background scaled to the computed cell size
        drawCheckerboard(cellSize);

        var snake = gameBoard.getSnake();
        for (int i = 0; i < snake.size(); i++) {
            var segment = snake.get(i);
            int x = segment.getX();
            int y = segment.getY();

            if (i == 0) {
                gc.setFill(Color.web("#228B22"));
                gc.setStroke(Color.web("#32CD32"));
                gc.setLineWidth(2);
            } else {
                gc.setFill(Color.web("#2E8B57"));
                gc.setStroke(Color.web("#3CB371"));
                gc.setLineWidth(1);
            }
            gc.fillRoundRect(x * cellSize + 1, y * cellSize + 1, cellSize - 2, cellSize - 2, 5, 5);
            gc.strokeRoundRect(x * cellSize + 1, y * cellSize + 1, cellSize - 2, cellSize - 2, 5, 5);
        }

        // Rắn 2 nếu có
        var snake2 = gameBoard.getSnake2();
        if (snake2 != null) {
            for (int i = 0; i < snake2.size(); i++) {
                var segment = snake2.get(i);
                int x = segment.getX();
                int y = segment.getY();

                if (i == 0) {
                    gc.setFill(Color.web("#1E90FF"));
                    gc.setStroke(Color.web("#87CEFA"));
                    gc.setLineWidth(2);

                    gc.setLineWidth(2);
                } else {
                    gc.setFill(Color.web("#4169E1"));
                    gc.setStroke(Color.web("#87CEFA"));
                    gc.setLineWidth(1);
                }
                gc.fillRoundRect(x * cellSize + 1, y * cellSize + 1, cellSize - 2, cellSize - 2, 5, 5);
                gc.strokeRoundRect(x * cellSize + 1, y * cellSize + 1, cellSize - 2, cellSize - 2, 5, 5);
            }
        }

        var food = gameBoard.getFood();
        gc.setFill(Color.web("#DC143C"));
        gc.setStroke(Color.web("#FF6347"));
        gc.setLineWidth(1);
        gc.fillOval(food.getX() * cellSize + 2, food.getY() * cellSize + 2, cellSize - 4, cellSize - 4);
        gc.strokeOval(food.getX() * cellSize + 2, food.getY() * cellSize + 2, cellSize - 4, cellSize - 4);
    }


}
