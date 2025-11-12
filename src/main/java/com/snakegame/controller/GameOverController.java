package com.snakegame.controller;

import com.snakegame.database.DatabaseManager;
import com.snakegame.model.GameBoard;
import com.snakegame.model.Player;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class GameOverController {

    @FXML
    private Label gameOverLabel;

    @FXML
    private Label finalScoreLabel;

    @FXML
    private Label difficultyLabel;

    @FXML
    private TextField playerNameField;

    @FXML
    private Button saveScoreButton;

    @FXML
    private Button playAgainButton;

    @FXML
    private Button menuButton;
    @FXML
    private Label finalScoreLabel2;

    private int finalScore;
    private GameBoard.Difficulty difficulty;
    private DatabaseManager dbManager;
    private boolean wasTwoPlayer;  // trạng thái game trước khi kết thúc
    private int finalScore2;


    public GameOverController() {
        this.dbManager = DatabaseManager.getInstance();
    }

    public void setGameData(int score1,int score2, GameBoard.Difficulty difficulty, boolean twoPlayer) {
        this.finalScore = score1;
        this.finalScore2 = score2;
        this.difficulty = difficulty;
        this.wasTwoPlayer = twoPlayer;

        // Hiển thị điểm
        finalScoreLabel.setText("P1: " + score1);

        if (twoPlayer && finalScoreLabel2 != null) {
            finalScoreLabel2.setText("P2: " + score2);
            finalScoreLabel2.setVisible(true);
        } else if (finalScoreLabel2 != null) {
            finalScoreLabel2.setVisible(false);
        }

        difficultyLabel.setText("Độ khó: " + getDifficultyText(difficulty));
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
    private void saveScore() {
        String playerName = playerNameField.getText().trim();
        if (playerName.isEmpty()) {
            playerName = "Người chơi ẩn danh";
        }

        Player player = new Player(playerName, finalScore, difficulty.name());
        boolean saved = dbManager.savePlayer(player);

        if (saved) {
            saveScoreButton.setText("ĐÃ LƯU!");
            saveScoreButton.setDisable(true);
        } else {
            saveScoreButton.setText("LỖI LƯU!");
        }
    }

    @FXML
    private void playAgain() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Game.fxml"));
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

            GameController gameController = loader.getController();
            gameController.initializeGame(difficulty,wasTwoPlayer);

            Stage stage = (Stage) playAgainButton.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
}
