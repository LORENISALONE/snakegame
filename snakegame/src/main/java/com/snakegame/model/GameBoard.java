package com.snakegame.model;

import javafx.scene.paint.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameBoard {
    public enum Direction {
        UP, DOWN, LEFT, RIGHT
    }

    public enum Difficulty {
        EASY(200), MEDIUM(150), HARD(100);

        private final int speed;

        Difficulty(int speed) {
            this.speed = speed;
        }

        public int getSpeed() {
            return speed;
        }
    }

    private static final int BOARD_WIDTH = 20;
    private static final int BOARD_HEIGHT = 20;

    private List<Point> snake;
    private List<Point> snake2; // optional for 2P
    private Point food;
    private Point food2; // thức ăn cho P2
    private Direction direction;
    private Direction nextDirection;
    private Direction direction2;
    private Direction nextDirection2;
    private boolean gameOver;
    private boolean twoPlayer;
    private int score;
    private int score2;
    private Difficulty difficulty;
    private Random random;
    private List<Point> wall;

    public GameBoard(Difficulty difficulty) {
        this(difficulty, false);
    }

    public GameBoard(Difficulty difficulty, boolean twoPlayer) {
        this.difficulty = difficulty;
        this.twoPlayer = twoPlayer;
        this.random = new Random();
        initializeGame();
    }

    private void initializeGame() {
        gameOver = false;
        score = 0;
        score2 = 0;

        if (twoPlayer) {
            // P1 bên trái
            snake = new ArrayList<>();
            snake.add(new Point(BOARD_WIDTH / 4, BOARD_HEIGHT / 2));
            direction = Direction.RIGHT;
            nextDirection = Direction.RIGHT;

            // P2 bên phải
            snake2 = new ArrayList<>();
            snake2.add(new Point(3 * BOARD_WIDTH / 4, BOARD_HEIGHT / 2));
            direction2 = Direction.LEFT;
            nextDirection2 = Direction.LEFT;

            createWall();
        } else {
            // 1P → giữa màn hình, wrap-around
            snake = new ArrayList<>();
            snake.add(new Point(BOARD_WIDTH / 2, BOARD_HEIGHT / 2));
            direction = Direction.RIGHT;
            nextDirection = Direction.RIGHT;
            snake2 = null;
        }

        generateFood();
        if (twoPlayer) generateFood2();
    }

    public void reset() {
        initializeGame();
    }

    public void setDirection(Direction newDirection) {
        if ((direction == Direction.UP && newDirection != Direction.DOWN) ||
                (direction == Direction.DOWN && newDirection != Direction.UP) ||
                (direction == Direction.LEFT && newDirection != Direction.RIGHT) ||
                (direction == Direction.RIGHT && newDirection != Direction.LEFT)) {
            nextDirection = newDirection;
        }
    }

    public void setDirectionP2(Direction newDirection) {
        if (!twoPlayer) return;
        if ((direction2 == Direction.UP && newDirection != Direction.DOWN) ||
                (direction2 == Direction.DOWN && newDirection != Direction.UP) ||
                (direction2 == Direction.LEFT && newDirection != Direction.RIGHT) ||
                (direction2 == Direction.RIGHT && newDirection != Direction.LEFT)) {
            nextDirection2 = newDirection;
        }
    }

    public void update() {
        if (gameOver) return;

        // --- Update P1 ---
        direction = nextDirection;
        Point head = snake.get(0);
        Point newHead = new Point(head.getX(), head.getY());

        switch (direction) {
            case UP -> newHead.setY(newHead.getY() - 1);
            case DOWN -> newHead.setY(newHead.getY() + 1);
            case LEFT -> newHead.setX(newHead.getX() - 1);
            case RIGHT -> newHead.setX(newHead.getX() + 1);
        }

        if (!twoPlayer) {
            // 1P wrap-around
            newHead.setX((newHead.getX() % BOARD_WIDTH + BOARD_WIDTH) % BOARD_WIDTH);
            newHead.setY((newHead.getY() % BOARD_HEIGHT + BOARD_HEIGHT) % BOARD_HEIGHT);
        }

        // --- Update P2 ---
        Point newHead2 = null;
        if (twoPlayer) {
            direction2 = nextDirection2;
            Point head2 = snake2.get(0);
            newHead2 = new Point(head2.getX(), head2.getY());

            switch (direction2) {
                case UP -> newHead2.setY(newHead2.getY() - 1);
                case DOWN -> newHead2.setY(newHead2.getY() + 1);
                case LEFT -> newHead2.setX(newHead2.getX() - 1);
                case RIGHT -> newHead2.setX(newHead2.getX() + 1);
            }

            // Giới hạn bàn cho 2P: P1 [0, BOARD_WIDTH/2), P2 [BOARD_WIDTH/2, BOARD_WIDTH)
            if (newHead.getX() < 0 || newHead.getX() >= BOARD_WIDTH / 2 ||
                    newHead.getY() < 0 || newHead.getY() >= BOARD_HEIGHT ||
                    newHead2.getX() < BOARD_WIDTH / 2 || newHead2.getX() >= BOARD_WIDTH ||
                    newHead2.getY() < 0 || newHead2.getY() >= BOARD_HEIGHT) {
                gameOver = true;
                return;
            }
        }

        // --- Determine eating ---
        boolean willEat = newHead.equals(food);
        boolean willEat2 = twoPlayer && newHead2.equals(food2);

        // --- Remove tail if not eating ---
        if (!willEat && !snake.isEmpty()) snake.remove(snake.size() - 1);
        if (twoPlayer && !willEat2 && !snake2.isEmpty()) snake2.remove(snake2.size() - 1);

        // --- Self collision ---
        if (snake.contains(newHead) || (twoPlayer && snake2.contains(newHead))) { gameOver = true; return; }
        if (twoPlayer && snake2.contains(newHead2)) { gameOver = true; return; }

        // --- Wall collision ---
        if (twoPlayer && wall != null) {
            if (wall.contains(newHead) || wall.contains(newHead2)) { gameOver = true; return; }
        }

        // --- Cross collision & head-to-head ---
        if (twoPlayer) {
            if (snake.contains(newHead2) || snake2.contains(newHead)) { gameOver = true; return; }
            if (newHead.equals(newHead2)) { gameOver = true; return; }
        }

        // --- Add new heads ---
        snake.add(0, newHead);
        if (twoPlayer) snake2.add(0, newHead2);

        // --- Eating food ---
        if (willEat) {
            score += 10;
            generateFood();
        }
        if (twoPlayer && willEat2) {
            score2 += 10;
            generateFood2();
        }
    }

    private void generateFood() {
        do {
            food = new Point(random.nextInt(BOARD_WIDTH / 2), random.nextInt(BOARD_HEIGHT));
        } while (snake.contains(food) ||
                (twoPlayer && snake2 != null && snake2.contains(food)) ||
                (twoPlayer && wall != null && wall.contains(food)) ||
                food.equals(snake.get(0)));
    }

    private void generateFood2() {
        do {
            food2 = new Point(random.nextInt(BOARD_WIDTH / 2) + BOARD_WIDTH / 2, random.nextInt(BOARD_HEIGHT));
        } while (snake.contains(food2) ||
                (twoPlayer && snake2 != null && snake2.contains(food2)) ||
                (twoPlayer && wall != null && wall.contains(food2)) ||
                food2.equals(food) ||
                food2.equals(snake2.get(0)));
    }

    public List<Point> getSnake() { return new ArrayList<>(snake); }
    public List<Point> getSnake2() { return twoPlayer ? new ArrayList<>(snake2) : null; }
    public Point getFood() { return food; }
    public Point getFood2() { return food2; }
    public boolean isGameOver() { return gameOver; }
    public int getScore() { return score; }
    public int getScore2() { return score2; }
    public boolean isTwoPlayer() { return twoPlayer; }
    public Difficulty getDifficulty() { return difficulty; }
    public int getBoardWidth() { return BOARD_WIDTH; }
    public int getBoardHeight() { return BOARD_HEIGHT; }
    public Direction getDirection() { return direction; }
    public Direction getDirection2() { return direction2; }

    public static class Point {
        private int x, y;
        public Point(int x, int y) { this.x = x; this.y = y; }
        public int getX() { return x; }
        public void setX(int x) { this.x = x; }
        public int getY() { return y; }
        public void setY(int y) { this.y = y; }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            Point point = (Point) obj;
            return x == point.x && y == point.y;
        }

        @Override
        public int hashCode() {
            return x * 31 + y;
        }
    }

    private void createWall() {
        wall = new ArrayList<>();
        int midX = BOARD_WIDTH / 2;
        for (int y = 0; y < BOARD_HEIGHT; y++) {
            wall.add(new Point(midX, y));
        }
    }

    public List<Point> getWall() { return wall; }
}
