package com.snakegame.controller;

import com.snakegame.database.DatabaseManager;
import com.snakegame.model.Player;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class HighScoreController implements Initializable {
    
    @FXML
    private Button allScoresButton;
    
    @FXML
    private Button easyScoresButton;
    
    @FXML
    private Button mediumScoresButton;
    
    @FXML
    private Button hardScoresButton;
    
    @FXML
    private Button backButton;
    
    @FXML
    private ScrollPane scoreScrollPane;
    
    @FXML
    private VBox scoreContainer;
    
    private DatabaseManager dbManager;
    private String currentFilter = "ALL";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.dbManager = DatabaseManager.getInstance();
        showAllScores();
    }

    @FXML
    private void showAllScores() {
        currentFilter = "ALL";
        updateButtonStates();
        loadScores(dbManager.getTopPlayers(20));
    }

    @FXML
    private void showEasyScores() {
        currentFilter = "EASY";
        updateButtonStates();
        loadScores(dbManager.getTopPlayersByDifficulty("EASY", 20));
    }

    @FXML
    private void showMediumScores() {
        currentFilter = "MEDIUM";
        updateButtonStates();
        loadScores(dbManager.getTopPlayersByDifficulty("MEDIUM", 20));
    }

    @FXML
    private void showHardScores() {
        currentFilter = "HARD";
        updateButtonStates();
        loadScores(dbManager.getTopPlayersByDifficulty("HARD", 20));
    }

    @FXML
    private void backToMenu() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Menu.fxml"));
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateButtonStates() {
        allScoresButton.getStyleClass().remove("selected");
        easyScoresButton.getStyleClass().remove("selected");
        mediumScoresButton.getStyleClass().remove("selected");
        hardScoresButton.getStyleClass().remove("selected");
        
        switch (currentFilter) {
            case "ALL":
                allScoresButton.getStyleClass().add("selected");
                break;
            case "EASY":
                easyScoresButton.getStyleClass().add("selected");
                break;
            case "MEDIUM":
                mediumScoresButton.getStyleClass().add("selected");
                break;
            case "HARD":
                hardScoresButton.getStyleClass().add("selected");
                break;
        }
    }

    private void loadScores(List<Player> players) {
        scoreContainer.getChildren().clear();
        
        if (players.isEmpty()) {
            Text noScoresText = new Text("Chưa có điểm số nào!");
            noScoresText.setFont(Font.font("Arial", FontWeight.BOLD, 18));
            noScoresText.setStyle("-fx-fill: #666666;");
            scoreContainer.getChildren().add(noScoresText);
            return;
        }
        
        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            HBox scoreItem = createScoreItem(i + 1, player);
            scoreContainer.getChildren().add(scoreItem);
        }
    }

    private HBox createScoreItem(int rank, Player player) {
        HBox scoreItem = new HBox(20);
        scoreItem.getStyleClass().add("score-item");
        scoreItem.setPrefHeight(60);
        
        // Rank
        Text rankText = new Text(String.valueOf(rank));
        rankText.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        rankText.setStyle("-fx-fill: #2E8B57;");
        rankText.setWrappingWidth(50);
        
        // Player name
        Text nameText = new Text(player.getPlayerName());
        nameText.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        nameText.setStyle("-fx-fill: #333333;");
        nameText.setWrappingWidth(200);
        
        // Score
        Text scoreText = new Text(String.valueOf(player.getScore()));
        scoreText.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        scoreText.setStyle("-fx-fill: #2E8B57;");
        scoreText.setWrappingWidth(100);
        
        // Difficulty
        Text difficultyText = new Text(getDifficultyText(player.getDifficulty()));
        difficultyText.setFont(Font.font("Arial", 14));
        difficultyText.setStyle("-fx-fill: #666666;");
        difficultyText.setWrappingWidth(120);
        
        // Date
        Text dateText = new Text(player.getPlayDate().toString().substring(0, 19));
        dateText.setFont(Font.font("Arial", 12));
        dateText.setStyle("-fx-fill: #999999;");
        dateText.setWrappingWidth(150);
        
        scoreItem.getChildren().addAll(rankText, nameText, scoreText, difficultyText, dateText);
        
        return scoreItem;
    }

    private String getDifficultyText(String difficulty) {
        switch (difficulty) {
            case "EASY": return "DỄ";
            case "MEDIUM": return "TRUNG BÌNH";
            case "HARD": return "KHÓ";
            default: return difficulty;
        }
    }
}
