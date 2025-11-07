// java
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
        snake = new ArrayList<>();
        snake.add(new Point(BOARD_WIDTH / 2, BOARD_HEIGHT / 2));
        direction = Direction.RIGHT;
        nextDirection = Direction.RIGHT;

        if (twoPlayer) {
            snake2 = new ArrayList<>();
            snake2.add(new Point(BOARD_WIDTH / 2 - 3, BOARD_HEIGHT / 2));
            direction2 = Direction.RIGHT;
            nextDirection2 = Direction.RIGHT;
        } else {
            snake2 = null;
        }
        gameOver = false;
        score = 0;
        score2 = 0;
        generateFood();
        if (twoPlayer) {
            createWall();
        }

    }

    public void reset() {
        initializeGame();
    }

    public void setDirection(Direction newDirection) {
        // Prevent snake from going backwards
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

        direction = nextDirection;
        Point head = snake.get(0);
        Point newHead = new Point(head.getX(), head.getY());

        Point newHead2 = null;
        if (twoPlayer) {
            direction2 = nextDirection2;
            Point head2 = snake2.get(0);
            newHead2 = new Point(head2.getX(), head2.getY());

            switch (direction2) {
                case UP:
                    newHead2.setY(newHead2.getY() - 1);
                    break;
                case DOWN:
                    newHead2.setY(newHead2.getY() + 1);
                    break;
                case LEFT:
                    newHead2.setX(newHead2.getX() - 1);
                    break;
                case RIGHT:
                    newHead2.setX(newHead2.getX() + 1);
                    break;
            }

            // Wrap P2 head around board edges (toroidal)
            newHead2.setX((newHead2.getX() % BOARD_WIDTH + BOARD_WIDTH) % BOARD_WIDTH);
            newHead2.setY((newHead2.getY() % BOARD_HEIGHT + BOARD_HEIGHT) % BOARD_HEIGHT);
        }


        // Move head based on direction
        switch (direction) {
            case UP:
                newHead.setY(newHead.getY() - 1);
                break;
            case DOWN:
                newHead.setY(newHead.getY() + 1);
                break;
            case LEFT:
                newHead.setX(newHead.getX() - 1);
                break;
            case RIGHT:
                newHead.setX(newHead.getX() + 1);
                break;
        }

        // Wrap main head around board edges (toroidal)
        newHead.setX((newHead.getX() % BOARD_WIDTH + BOARD_WIDTH) % BOARD_WIDTH);
        newHead.setY((newHead.getY() % BOARD_HEIGHT + BOARD_HEIGHT) % BOARD_HEIGHT);

        // Determine if we will eat food this move
        boolean willEat = newHead.equals(food);
        boolean willEat2 = twoPlayer && newHead2.equals(food);

        // If not eating, remove tail first to avoid false self-collision when moving into previous tail cell
        if (!willEat) {
            if (!snake.isEmpty()) {
                snake.remove(snake.size() - 1);
            }
        }

        if (twoPlayer && !willEat2) {
            if (!snake2.isEmpty()) {
                snake2.remove(snake2.size() - 1);
            }
        }

        // Check self collision against current body (after potential tail removal)
        if (snake.contains(newHead)) {
            gameOver = true;
            return;
        }
        // Collision with wall
        if (twoPlayer && wall != null && wall.contains(newHead)) {
            gameOver = true;
            return;
        }
        if (twoPlayer && wall != null && wall.contains(newHead2)) {
            gameOver = true;
            return;
        }


        if (twoPlayer) {
            // P2 self collision
            if (snake2.contains(newHead2)) {
                gameOver = true;
                return;
            }
            // Cross collision: head into other body
            if (snake.contains(newHead2) || snake2.contains(newHead)) {
                gameOver = true;
                return;
            }
            // Head-to-head collision
            if (newHead.equals(newHead2)) {
                gameOver = true;
                return;
            }
        }

        // Add new head
        snake.add(0, newHead);
        if (twoPlayer) {
            snake2.add(0, newHead2);
        }

        // If ate food, increase score and generate new food (no tail removal needed here)
        if (willEat) {
            score += 10;
            generateFood();
        }
        if (twoPlayer && willEat2) {
            score2 += 10;
            generateFood();
        }
    }

    private void generateFood() {
        do {
            food = new Point(random.nextInt(BOARD_WIDTH), random.nextInt(BOARD_HEIGHT));
        } while (
                snake.contains(food) ||
                        (twoPlayer && snake2 != null && snake2.contains(food)) ||
                        (twoPlayer && wall != null && wall.contains(food))
        );

    }

    public List<Point> getSnake() {
        return new ArrayList<>(snake);
    }

    public List<Point> getSnake2() {
        return twoPlayer ? new ArrayList<>(snake2) : null;
    }

    public Point getFood() {
        return food;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public int getScore() {
        return score;
    }

    public int getScore2() {
        return score2;
    }

    public boolean isTwoPlayer() {
        return twoPlayer;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public int getBoardWidth() {
        return BOARD_WIDTH;
    }

    public int getBoardHeight() {
        return BOARD_HEIGHT;
    }

    // Expose current directions so controller can pick the right head image
    public Direction getDirection() {
        return direction;
    }

    public Direction getDirection2() {
        return direction2;
    }

    public static class Point {
        private int x, y;

        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public int getX() {
            return x;
        }

        public void setX(int x) {
            this.x = x;
        }

        public int getY() {
            return y;
        }

        public void setY(int y) {
            this.y = y;
        }

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
    public List<Point> getWall() {
        return wall;
    }


}