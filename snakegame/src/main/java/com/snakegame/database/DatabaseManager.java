package com.snakegame.database;

import com.snakegame.model.Player;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private static final String URL = "jdbc:mysql://localhost:3306/snake_game?useSSL=false&serverTimezone=UTC";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "1";
    
    private static DatabaseManager instance;
    private Connection connection;

    private DatabaseManager() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("Database connection failed: " + e.getMessage());
        }
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }

    public boolean savePlayer(Player player) {
        String sql = "INSERT INTO players (player_name, score, difficulty) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, player.getPlayerName());
            stmt.setInt(2, player.getScore());
            stmt.setString(3, player.getDifficulty());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error saving player: " + e.getMessage());
            return false;
        }
    }

    public List<Player> getTopPlayers(int limit) {
        List<Player> players = new ArrayList<>();
        String sql = "SELECT * FROM players ORDER BY score DESC LIMIT ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, limit);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                players.add(new Player(
                    rs.getInt("id"),
                    rs.getString("player_name"),
                    rs.getInt("score"),
                    rs.getString("difficulty"),
                    rs.getTimestamp("play_date").toLocalDateTime()
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error getting top players: " + e.getMessage());
        }
        return players;
    }

    public List<Player> getTopPlayersByDifficulty(String difficulty, int limit) {
        List<Player> players = new ArrayList<>();
        String sql = "SELECT * FROM players WHERE difficulty = ? ORDER BY score DESC LIMIT ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, difficulty);
            stmt.setInt(2, limit);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                players.add(new Player(
                    rs.getInt("id"),
                    rs.getString("player_name"),
                    rs.getInt("score"),
                    rs.getString("difficulty"),
                    rs.getTimestamp("play_date").toLocalDateTime()
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error getting top players by difficulty: " + e.getMessage());
        }
        return players;
    }

    public int getHighScore(String difficulty) {
        String sql = "SELECT MAX(score) FROM players WHERE difficulty = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, difficulty);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error getting high score: " + e.getMessage());
        }
        return 0;
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("Error closing database connection: " + e.getMessage());
        }
    }
}

