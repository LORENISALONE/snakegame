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
    
    private int finalScore;
    private GameBoard.Difficulty difficulty;
    private DatabaseManager dbManager;

    public GameOverController() {
        this.dbManager = DatabaseManager.getInstance();
    }

    public void setGameData(int score, GameBoard.Difficulty difficulty) {
        this.finalScore = score;
        this.difficulty = difficulty;
        
        finalScoreLabel.setText("Điểm cuối: " + score);
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
            gameController.initializeGame(difficulty);
            
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
