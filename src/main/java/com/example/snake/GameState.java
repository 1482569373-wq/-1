package com.example.snake;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

public class GameState {
    public static final int DEFAULT_COLUMNS = 30;
    public static final int DEFAULT_ROWS = 20;
    public static final int INVINCIBLE_TICKS = 42;
    public static final int DOUBLE_SCORE_TICKS = 36;

    private final int columns;
    private final int rows;
    private final Random random;
    private final Deque<Point> snake = new ArrayDeque<>();
    private final List<Point> obstacles = new ArrayList<>();
    private final List<Integer> scoreHistory = new ArrayList<>();

    private Difficulty difficulty = Difficulty.NORMAL;
    private Direction direction = Direction.RIGHT;
    private Direction requestedDirection = Direction.RIGHT;
    private Food food;
    private int score;
    private int invincibleTicks;
    private int doubleScoreTicks;
    private boolean gameOver;
    private boolean paused;
    private boolean scoreRecorded;

    public GameState() {
        this(DEFAULT_COLUMNS, DEFAULT_ROWS, new Random());
    }

    public GameState(int columns, int rows, Random random) {
        if (columns < 8 || rows < 8) {
            throw new IllegalArgumentException("Board must be at least 8 x 8.");
        }
        this.columns = columns;
        this.rows = rows;
        this.random = Objects.requireNonNull(random, "random");
        reset(Difficulty.NORMAL);
    }

    GameState(int columns, int rows, List<Point> initialSnake, Direction initialDirection, Food food) {
        if (columns < 8 || rows < 8) {
            throw new IllegalArgumentException("Board must be at least 8 x 8.");
        }
        if (initialSnake.isEmpty()) {
            throw new IllegalArgumentException("Snake must contain at least one segment.");
        }
        this.columns = columns;
        this.rows = rows;
        this.random = new Random(0);
        this.snake.addAll(initialSnake);
        this.direction = Objects.requireNonNull(initialDirection, "initialDirection");
        this.requestedDirection = initialDirection;
        this.food = Objects.requireNonNull(food, "food");
        this.difficulty = Difficulty.NORMAL;
    }

    public void reset() {
        reset(difficulty);
    }

    public void reset(Difficulty nextDifficulty) {
        difficulty = Objects.requireNonNull(nextDifficulty, "nextDifficulty");
        snake.clear();
        obstacles.clear();
        int centerX = columns / 2;
        int centerY = rows / 2;
        snake.addFirst(new Point(centerX, centerY));
        snake.addLast(new Point(centerX - 1, centerY));
        snake.addLast(new Point(centerX - 2, centerY));
        direction = Direction.RIGHT;
        requestedDirection = Direction.RIGHT;
        score = 0;
        invincibleTicks = 0;
        doubleScoreTicks = 0;
        gameOver = false;
        paused = false;
        scoreRecorded = false;
        generateObstacles();
        food = randomFood();
    }

    public void tick() {
        if (paused || gameOver) {
            return;
        }

        direction = requestedDirection;
        Point newHead = head().translate(direction);
        boolean invincible = isInvincible();
        if (isOutsideBoard(newHead)) {
            if (invincible) {
                newHead = wrap(newHead);
            } else {
                endGame();
                return;
            }
        }

        boolean eating = food != null && newHead.equals(food.position());
        boolean growing = eating && food.type().growsSnake();
        Point tail = snake.peekLast();
        boolean hitsBody = snake.contains(newHead) && (growing || !newHead.equals(tail));
        boolean hitsObstacle = obstacles.contains(newHead);
        if ((hitsBody || hitsObstacle) && !invincible) {
            endGame();
            return;
        }

        snake.addFirst(newHead);
        if (eating) {
            applyFood(food.type());
            food = randomFood();
        }
        if (!growing) {
            snake.removeLast();
        }

        if (invincibleTicks > 0) {
            invincibleTicks--;
        }
        if (doubleScoreTicks > 0) {
            doubleScoreTicks--;
        }
    }

    public void requestDirection(Direction nextDirection) {
        Objects.requireNonNull(nextDirection, "nextDirection");
        if (!nextDirection.isOpposite(direction)) {
            requestedDirection = nextDirection;
        }
    }

    public void togglePause() {
        if (!gameOver) {
            paused = !paused;
        }
    }

    public int columns() {
        return columns;
    }

    public int rows() {
        return rows;
    }

    public Difficulty difficulty() {
        return difficulty;
    }

    public List<Point> snake() {
        return Collections.unmodifiableList(new ArrayList<>(snake));
    }

    public List<Point> obstacles() {
        return Collections.unmodifiableList(new ArrayList<>(obstacles));
    }

    public Point head() {
        return snake.peekFirst();
    }

    public Food food() {
        return food;
    }

    public Point foodPosition() {
        return food == null ? null : food.position();
    }

    public int score() {
        return score;
    }

    public int invincibleTicks() {
        return invincibleTicks;
    }

    public int doubleScoreTicks() {
        return doubleScoreTicks;
    }

    public boolean isInvincible() {
        return invincibleTicks > 0;
    }

    public boolean isDoubleScoreActive() {
        return doubleScoreTicks > 0;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public boolean isPaused() {
        return paused;
    }

    public Direction direction() {
        return direction;
    }

    public List<Integer> scoreHistory() {
        return Collections.unmodifiableList(new ArrayList<>(scoreHistory));
    }

    public int highScore() {
        int best = 0;
        for (int value : scoreHistory) {
            if (value > best) {
                best = value;
            }
        }
        return Math.max(best, score);
    }

    private void applyFood(FoodType type) {
        int earned = type.baseScore() * difficulty.scoreMultiplier();
        if (isDoubleScoreActive()) {
            earned *= 2;
        }
        score += earned;
        if (type == FoodType.INVINCIBLE) {
            invincibleTicks = INVINCIBLE_TICKS;
        } else if (type == FoodType.DOUBLE_SCORE) {
            doubleScoreTicks = DOUBLE_SCORE_TICKS;
        }
    }

    private void endGame() {
        gameOver = true;
        if (!scoreRecorded) {
            scoreHistory.add(score);
            scoreRecorded = true;
        }
    }

    private boolean isOutsideBoard(Point point) {
        return point.x() < 0 || point.x() >= columns || point.y() < 0 || point.y() >= rows;
    }

    private Point wrap(Point point) {
        int x = point.x();
        int y = point.y();
        if (x < 0) {
            x = columns - 1;
        } else if (x >= columns) {
            x = 0;
        }
        if (y < 0) {
            y = rows - 1;
        } else if (y >= rows) {
            y = 0;
        }
        return new Point(x, y);
    }

    private void generateObstacles() {
        Set<Point> reserved = new HashSet<>(snake);
        int centerX = columns / 2;
        int centerY = rows / 2;
        for (int y = centerY - 2; y <= centerY + 2; y++) {
            for (int x = centerX - 4; x <= centerX + 4; x++) {
                reserved.add(new Point(x, y));
            }
        }

        addWallSegment(4, 4, 9, 4, reserved);
        addWallSegment(columns - 10, rows - 5, columns - 5, rows - 5, reserved);
        addWallSegment(6, rows - 8, 6, rows - 4, reserved);
        addWallSegment(columns - 7, 4, columns - 7, 8, reserved);
        addWallSegment(columns / 2 - 3, rows / 2 + 5, columns / 2 + 3, rows / 2 + 5, reserved);
    }

    private void addWallSegment(int x1, int y1, int x2, int y2, Set<Point> reserved) {
        int dx = Integer.compare(x2, x1);
        int dy = Integer.compare(y2, y1);
        int x = x1;
        int y = y1;
        while (true) {
            Point point = new Point(x, y);
            if (!reserved.contains(point) && !isOutsideBoard(point)) {
                obstacles.add(point);
                reserved.add(point);
            }
            if (x == x2 && y == y2) {
                break;
            }
            x += dx;
            y += dy;
        }
    }

    private Food randomFood() {
        List<Point> availableCells = new ArrayList<>(columns * rows - snake.size() - obstacles.size());
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < columns; x++) {
                Point candidate = new Point(x, y);
                if (!snake.contains(candidate) && !obstacles.contains(candidate)) {
                    availableCells.add(candidate);
                }
            }
        }
        if (availableCells.isEmpty()) {
            endGame();
            return null;
        }
        return new Food(availableCells.get(random.nextInt(availableCells.size())), randomFoodType());
    }

    private FoodType randomFoodType() {
        int roll = random.nextInt(100);
        if (roll < 66) {
            return FoodType.NORMAL;
        }
        if (roll < 80) {
            return FoodType.BONUS;
        }
        if (roll < 91) {
            return FoodType.INVINCIBLE;
        }
        return FoodType.DOUBLE_SCORE;
    }
}
