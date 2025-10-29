package com.snakegame.controller;

import com.snakegame.model.GameBoard;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;

public class MenuController {
    
    @FXML
    private Button easyButton;
    
    @FXML
    private Button mediumButton;
    
    @FXML
    private Button hardButton;
    
    @FXML
    private Button highScoreButton;
    
    @FXML
    private Button exitButton;
    
    @FXML
    private Button twoPlayersButton;

    @FXML
    private void startEasyGame() {
        startGame(GameBoard.Difficulty.EASY);
    }

    @FXML
    private void startMediumGame() {
        startGame(GameBoard.Difficulty.MEDIUM);
    }

    @FXML
    private void startHardGame() {
        startGame(GameBoard.Difficulty.HARD);
    }

    @FXML
    private void startTwoPlayers() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Game.fxml"));
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

            GameController gameController = loader.getController();
            gameController.initializeGame(GameBoard.Difficulty.MEDIUM, true);

            Stage stage = (Stage) easyButton.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showHighScores() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/HighScore.fxml"));
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            
            Stage stage = (Stage) highScoreButton.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void exitGame() {
        System.exit(0);
    }

    private void startGame(GameBoard.Difficulty difficulty) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Game.fxml"));
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            
            GameController gameController = loader.getController();
            gameController.initializeGame(difficulty);
            
            Stage stage = (Stage) easyButton.getScene().getWindow();
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
