package com.example.snake;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * 贪吃蛇的“游戏规则层”。
 *
 * <p>这个类只负责保存和更新游戏状态，不负责画图，也不负责键盘事件。
 * 这样做的好处是：移动、吃食物、撞墙、撞自己这些规则可以被单元测试直接验证，
 * 不需要真的打开 JavaFX 窗口。</p>
 */
public class GameState {
    // 默认棋盘宽度：30 个格子。
    public static final int DEFAULT_COLUMNS = 30;
    // 默认棋盘高度：20 个格子。
    public static final int DEFAULT_ROWS = 20;

    // 棋盘列数，也就是 x 坐标允许的范围是 0 到 columns - 1。
    private final int columns;
    // 棋盘行数，也就是 y 坐标允许的范围是 0 到 rows - 1。
    private final int rows;
    // 随机数生成器，用来随机生成食物位置。测试时可以传入固定随机源。
    private final Random random;
    // 用双端队列保存蛇身：队头是蛇头，队尾是蛇尾。
    private final Deque<Point> snake = new ArrayDeque<>();

    // 当前真正生效的移动方向。
    private Direction direction = Direction.RIGHT;
    // 玩家刚刚请求的方向。它会在下一次 tick 时变成 direction。
    private Direction requestedDirection = Direction.RIGHT;
    // 当前食物所在格子。
    private Point food;
    // 分数。这里每吃到一个食物加 10 分。
    private int score;
    // 游戏是否结束。
    private boolean gameOver;
    // 游戏是否暂停。
    private boolean paused;

    /**
     * 创建一个默认大小的游戏。
     */
    public GameState() {
        this(DEFAULT_COLUMNS, DEFAULT_ROWS, new Random());
    }

    /**
     * 创建指定大小的游戏。
     *
     * @param columns 棋盘列数
     * @param rows 棋盘行数
     * @param random 随机数生成器，用于生成食物
     */
    public GameState(int columns, int rows, Random random) {
        if (columns < 5 || rows < 5) {
            throw new IllegalArgumentException("Board must be at least 5 x 5.");
        }
        this.columns = columns;
        this.rows = rows;
        this.random = Objects.requireNonNull(random, "random");
        reset();
    }

    /**
     * 测试专用构造方法。
     *
     * <p>它允许测试直接指定蛇的位置、方向和食物位置，
     * 这样可以稳定复现“下一步吃到食物”“下一步撞墙”等场景。</p>
     */
    GameState(int columns, int rows, List<Point> initialSnake, Direction initialDirection, Point food) {
        if (columns < 5 || rows < 5) {
            throw new IllegalArgumentException("Board must be at least 5 x 5.");
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
    }

    /**
     * 重置游戏到初始状态。
     *
     * <p>蛇会出现在棋盘中间，长度为 3，向右移动。
     * 分数归零，暂停和结束状态也会清空。</p>
     */
    public void reset() {
        snake.clear();
        int centerX = columns / 2;
        int centerY = rows / 2;
        snake.addFirst(new Point(centerX, centerY));
        snake.addLast(new Point(centerX - 1, centerY));
        snake.addLast(new Point(centerX - 2, centerY));
        direction = Direction.RIGHT;
        requestedDirection = Direction.RIGHT;
        score = 0;
        gameOver = false;
        paused = false;
        food = randomFood();
    }

    /**
     * 推进一帧游戏逻辑。
     *
     * <p>AnimationTimer 会按照固定间隔调用这个方法。
     * 一次 tick 就代表蛇尝试向前移动一格。</p>
     */
    public void tick() {
        // 暂停或游戏结束时，不再更新任何状态。
        if (paused || gameOver) {
            return;
        }

        // 将玩家最近一次合法输入应用到当前移动方向。
        direction = requestedDirection;
        // 根据当前方向计算下一格蛇头位置。
        Point newHead = head().translate(direction);

        // 如果新蛇头越过棋盘边界，游戏结束。
        if (isOutsideBoard(newHead)) {
            gameOver = true;
            return;
        }

        // 新蛇头刚好等于食物位置，说明这一帧会吃到食物并增长。
        boolean growing = newHead.equals(food);
        // 记录当前尾巴。没有增长时尾巴会被移除，所以蛇头移动到“旧尾巴位置”是允许的。
        Point tail = snake.peekLast();

        // 撞到身体则游戏结束。
        // 例外：如果没有增长，旧尾巴会在本帧移走，因此移动到旧尾巴位置不算自撞。
        if (snake.contains(newHead) && (growing || !newHead.equals(tail))) {
            gameOver = true;
            return;
        }

        // 把新蛇头放到队头，完成“向前走一格”的动作。
        snake.addFirst(newHead);
        if (growing) {
            // 吃到食物时不删除尾巴，所以蛇身自然变长一格。
            score += 10;
            food = randomFood();
        } else {
            // 没吃到食物时删除尾巴，蛇的总长度保持不变。
            snake.removeLast();
        }
    }

    /**
     * 接收玩家输入的方向。
     *
     * <p>这里不会立刻移动蛇，而是先保存到 requestedDirection。
     * 等下一次 tick 再统一应用，可以让输入处理和游戏循环保持清晰。</p>
     */
    public void requestDirection(Direction nextDirection) {
        Objects.requireNonNull(nextDirection, "nextDirection");
        // 禁止 180 度反向，避免蛇头直接撞向自己的身体。
        if (!nextDirection.isOpposite(direction)) {
            requestedDirection = nextDirection;
        }
    }

    /**
     * 暂停或继续游戏。
     *
     * <p>游戏结束后不再允许切换暂停，因为结束画面应保持稳定，
     * 玩家需要按 R 调用 reset() 重新开始。</p>
     */
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

    /**
     * 返回蛇身坐标列表。
     *
     * <p>这里返回的是拷贝后的不可修改列表，而不是直接暴露内部 Deque。
     * 这样外部绘图代码可以读取蛇身，但不能意外修改游戏状态。</p>
     */
    public List<Point> snake() {
        return Collections.unmodifiableList(new ArrayList<>(snake));
    }

    /**
     * 当前蛇头，也就是 snake 队列的第一个元素。
     */
    public Point head() {
        return snake.peekFirst();
    }

    public Point food() {
        return food;
    }

    public int score() {
        return score;
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

    /**
     * 判断某个坐标是否在棋盘外。
     */
    private boolean isOutsideBoard(Point point) {
        return point.x() < 0 || point.x() >= columns || point.y() < 0 || point.y() >= rows;
    }

    /**
     * 随机生成一个不在蛇身上的食物位置。
     *
     * <p>做法是先枚举棋盘上所有空格子，再从空格子里随机选一个。
     * 这种写法对 30 x 20 的小棋盘非常直观，也避免了随机到蛇身后反复重试的问题。</p>
     */
    private Point randomFood() {
        List<Point> availableCells = new ArrayList<>(columns * rows - snake.size());
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < columns; x++) {
                Point candidate = new Point(x, y);
                if (!snake.contains(candidate)) {
                    availableCells.add(candidate);
                }
            }
        }
        if (availableCells.isEmpty()) {
            // 棋盘被蛇占满时，已经没有地方放食物。这里直接结束游戏。
            gameOver = true;
            return null;
        }
        return availableCells.get(random.nextInt(availableCells.size()));
    }
}
