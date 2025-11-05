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
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

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

    private static final int CELL_SIZE = 25;
    private static final int BOARD_WIDTH = 20;
    private static final int BOARD_HEIGHT = 20;

    public void initializeGame(GameBoard.Difficulty difficulty) {
        this.gameBoard = new GameBoard(difficulty);
        this.gc = gameCanvas.getGraphicsContext2D();

        // Set up canvas size
        double width = gameCanvas.getWidth();
        double height = gameCanvas.getHeight();

// Tính lại kích thước mỗi ô để phủ hết khung
        double cellWidth = width / BOARD_WIDTH;
        double cellHeight = height / BOARD_HEIGHT;
        System.out.println(cellHeight);
        System.out.println(cellWidth);
        double cellSize = Math.min(cellWidth, cellHeight);

// Cập nhật lại CELL_SIZE tạm thời
        gc = gameCanvas.getGraphicsContext2D();

// Vẽ game với kích thước ô mới
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

        // Set up canvas size
        gameCanvas.setWidth(BOARD_WIDTH * CELL_SIZE);
        gameCanvas.setHeight(BOARD_HEIGHT * CELL_SIZE);

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

    private void setupGameLoop() {
        gameLoop = new Timeline(new KeyFrame(Duration.millis(gameBoard.getDifficulty().getSpeed()), e -> {
            if (!isPaused && !gameBoard.isGameOver()) {
                gameBoard.update();
                drawGame();
                updateUI();

                if (gameBoard.isGameOver()) {
                    gameLoop.stop();
                    showGameOver();
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
        gc.clearRect(0, 0, gameCanvas.getWidth(), gameCanvas.getHeight());

        // Draw snake P1
        var snake = gameBoard.getSnake();
        for (int i = 0; i < snake.size(); i++) {
            var segment = snake.get(i);
            int x = segment.getX() * CELL_SIZE;
            int y = segment.getY() * CELL_SIZE;

            if (i == 0) {
                // Draw head
                gc.setFill(Color.web("#228B22"));
                gc.setStroke(Color.web("#32CD32"));
                gc.setLineWidth(2);
            } else {
                // Draw body
                gc.setFill(Color.web("#2E8B57"));
                gc.setStroke(Color.web("#3CB371"));
                gc.setLineWidth(1);
            }

            gc.fillRoundRect(x + 1, y + 1, CELL_SIZE - 2, CELL_SIZE - 2, 5, 5);
            gc.strokeRoundRect(x + 1, y + 1, CELL_SIZE - 2, CELL_SIZE - 2, 5, 5);
        }

        // Draw snake P2 if enabled
        var snake2 = gameBoard.getSnake2();
        if (snake2 != null) {
            for (int i = 0; i < snake2.size(); i++) {
                var segment = snake2.get(i);
                int x = segment.getX() * CELL_SIZE;
                int y = segment.getY() * CELL_SIZE;

                if (i == 0) {
                    gc.setFill(Color.web("#1E90FF"));
                    gc.setStroke(Color.web("#87CEFA"));
                    gc.setLineWidth(2);
                } else {
                    gc.setFill(Color.web("#4169E1"));
                    gc.setStroke(Color.web("#87CEFA"));
                    gc.setLineWidth(1);
                }
                gc.fillRoundRect(x + 1, y + 1, CELL_SIZE - 2, CELL_SIZE - 2, 5, 5);
                gc.strokeRoundRect(x + 1, y + 1, CELL_SIZE - 2, CELL_SIZE - 2, 5, 5);
            }
        }

        // Draw food
        var food = gameBoard.getFood();
        int foodX = food.getX() * CELL_SIZE;
        int foodY = food.getY() * CELL_SIZE;

        gc.setFill(Color.web("#DC143C"));
        gc.setStroke(Color.web("#FF6347"));
        gc.setLineWidth(1);
        gc.fillOval(foodX + 2, foodY + 2, CELL_SIZE - 4, CELL_SIZE - 4);
        gc.strokeOval(foodX + 2, foodY + 2, CELL_SIZE - 4, CELL_SIZE - 4);
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
        gc.clearRect(0, 0, gameCanvas.getWidth(), gameCanvas.getHeight());

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
