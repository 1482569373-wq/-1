package com.example.snake;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

public class GameBoard extends Canvas {
    public static final int CELL_SIZE = 24;

    private static final Color BACKGROUND = Color.rgb(18, 22, 29);
    private static final Color PANEL = Color.rgb(27, 34, 45);
    private static final Color GRID = Color.rgb(36, 44, 56);
    private static final Color SNAKE_HEAD = Color.rgb(90, 226, 142);
    private static final Color SNAKE_BODY = Color.rgb(44, 168, 99);
    private static final Color FOOD = Color.rgb(244, 79, 96);
    private static final Color BONUS_FOOD = Color.rgb(255, 196, 87);
    private static final Color INVINCIBLE_FOOD = Color.rgb(105, 214, 255);
    private static final Color DOUBLE_FOOD = Color.rgb(189, 130, 255);
    private static final Color OBSTACLE = Color.rgb(116, 127, 143);
    private static final Color TEXT = Color.rgb(239, 244, 250);
    private static final Color MUTED_TEXT = Color.rgb(160, 171, 186);
    private static final Color ACCENT = Color.rgb(89, 155, 255);
    private static final Color OVERLAY = Color.rgb(0, 0, 0, 0.66);

    private final GameState state;

    public GameBoard(GameState state) {
        super(state.columns() * CELL_SIZE, state.rows() * CELL_SIZE);
        this.state = state;
        setFocusTraversable(true);
    }

    public void draw(GameScreen screen, Difficulty difficulty) {
        GraphicsContext gc = getGraphicsContext2D();
        drawBackground(gc);
        drawObstacles(gc);
        drawFood(gc);
        drawSnake(gc);
        drawScore(gc, difficulty);

        if (screen == GameScreen.MENU) {
            drawMenu(gc, difficulty);
        } else if (screen == GameScreen.PAUSED) {
            drawCenteredMessage(gc, "已暂停", "按空格继续");
        } else if (screen == GameScreen.GAME_OVER) {
            drawGameOver(gc, difficulty);
        }
    }

    private void drawBackground(GraphicsContext gc) {
        gc.setFill(BACKGROUND);
        gc.fillRect(0, 0, getWidth(), getHeight());
        gc.setStroke(GRID);
        gc.setLineWidth(1);
        for (int x = 0; x <= state.columns(); x++) {
            double px = x * CELL_SIZE + 0.5;
            gc.strokeLine(px, 0, px, getHeight());
        }
        for (int y = 0; y <= state.rows(); y++) {
            double py = y * CELL_SIZE + 0.5;
            gc.strokeLine(0, py, getWidth(), py);
        }
    }

    private void drawObstacles(GraphicsContext gc) {
        gc.setFill(OBSTACLE);
        for (Point obstacle : state.obstacles()) {
            double inset = 3;
            gc.fillRoundRect(obstacle.x() * CELL_SIZE + inset, obstacle.y() * CELL_SIZE + inset,
                    CELL_SIZE - inset * 2, CELL_SIZE - inset * 2, 5, 5);
        }
    }

    private void drawFood(GraphicsContext gc) {
        Food food = state.food();
        if (food == null) {
            return;
        }
        Point point = food.position();
        double x = point.x() * CELL_SIZE;
        double y = point.y() * CELL_SIZE;
        double inset = CELL_SIZE * 0.18;
        gc.setFill(foodColor(food.type()));
        gc.fillOval(x + inset, y + inset, CELL_SIZE - inset * 2, CELL_SIZE - inset * 2);
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(2);
        gc.strokeOval(x + inset + 2, y + inset + 2, CELL_SIZE - inset * 2 - 4, CELL_SIZE - inset * 2 - 4);
    }

    private Color foodColor(FoodType type) {
        if (type == FoodType.BONUS) {
            return BONUS_FOOD;
        }
        if (type == FoodType.INVINCIBLE) {
            return INVINCIBLE_FOOD;
        }
        if (type == FoodType.DOUBLE_SCORE) {
            return DOUBLE_FOOD;
        }
        return FOOD;
    }

    private void drawSnake(GraphicsContext gc) {
        boolean first = true;
        int index = 0;
        for (Point segment : state.snake()) {
            gc.setFill(first ? SNAKE_HEAD : SNAKE_BODY.interpolate(Color.rgb(28, 120, 78), Math.min(0.55, index * 0.02)));
            double inset = first ? 2.5 : 4;
            gc.fillRoundRect(segment.x() * CELL_SIZE + inset, segment.y() * CELL_SIZE + inset,
                    CELL_SIZE - inset * 2, CELL_SIZE - inset * 2, 8, 8);
            if (first) {
                drawEyes(gc, segment);
            }
            first = false;
            index++;
        }
    }

    private void drawEyes(GraphicsContext gc, Point head) {
        gc.setFill(Color.rgb(12, 18, 22));
        double baseX = head.x() * CELL_SIZE;
        double baseY = head.y() * CELL_SIZE;
        gc.fillOval(baseX + 7, baseY + 7, 4, 4);
        gc.fillOval(baseX + 14, baseY + 7, 4, 4);
    }

    private void drawScore(GraphicsContext gc, Difficulty difficulty) {
        gc.setFill(TEXT);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.fillText("分数: " + state.score(), 12, 24);
        gc.setFill(MUTED_TEXT);
        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        gc.fillText("难度: " + difficulty.chineseLabel() + " x" + difficulty.scoreMultiplier()
                + "  最高: " + state.highScore(), 12, 44);
        String effect = state.isInvincible()
                ? "无敌 " + state.invincibleTicks()
                : (state.isDoubleScoreActive() ? "双倍 " + state.doubleScoreTicks() : "");
        if (!effect.isEmpty()) {
            gc.setFill(ACCENT);
            gc.fillText(effect, 12, 64);
        }
    }

    private void drawMenu(GraphicsContext gc, Difficulty difficulty) {
        drawOverlay(gc);
        drawTitle(gc, "贪吃蛇");
        drawSubtitle(gc, "选择难度开始游戏");
        drawDifficultyRows(gc, difficulty, getHeight() / 2 - 12);
        drawHint(gc, "按 1 简单  |  2 普通  |  3 困难", getHeight() / 2 + 116);
        drawHint(gc, "红豆=普通  黄豆=奖励  蓝豆=无敌  紫豆=双倍积分", getHeight() / 2 + 146);
    }

    private void drawGameOver(GraphicsContext gc, Difficulty difficulty) {
        drawOverlay(gc);
        drawTitle(gc, "游戏结束");
        drawSubtitle(gc, "分数: " + state.score() + "  最高: " + state.highScore());
        drawDifficultyRows(gc, difficulty, getHeight() / 2 - 12);
        drawHint(gc, "按 1/2/3 选择难度重新开始", getHeight() / 2 + 116);
        drawHint(gc, "按 R 重试当前难度", getHeight() / 2 + 146);
    }

    private void drawCenteredMessage(GraphicsContext gc, String title, String subtitle) {
        drawOverlay(gc);
        drawTitle(gc, title);
        drawSubtitle(gc, subtitle);
    }

    private void drawOverlay(GraphicsContext gc) {
        gc.setFill(OVERLAY);
        gc.fillRect(0, 0, getWidth(), getHeight());
    }

    private void drawTitle(GraphicsContext gc, String title) {
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFill(TEXT);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 48));
        gc.fillText(title, getWidth() / 2, getHeight() / 2 - 116);
    }

    private void drawSubtitle(GraphicsContext gc, String subtitle) {
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFill(MUTED_TEXT);
        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 18));
        gc.fillText(subtitle, getWidth() / 2, getHeight() / 2 - 76);
    }

    private void drawDifficultyRows(GraphicsContext gc, Difficulty selected, double top) {
        double rowWidth = 280;
        double rowHeight = 36;
        double left = (getWidth() - rowWidth) / 2;
        int i = 0;
        for (Difficulty difficulty : Difficulty.values()) {
            double y = top + i * 46;
            boolean active = difficulty == selected;
            gc.setFill(active ? ACCENT : PANEL);
            gc.fillRoundRect(left, y, rowWidth, rowHeight, 8, 8);
            gc.setTextAlign(TextAlignment.LEFT);
            gc.setFill(TEXT);
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 15));
            gc.fillText((i + 1) + ". " + difficulty.chineseLabel(), left + 18, y + 24);
            gc.setTextAlign(TextAlignment.RIGHT);
            gc.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
            gc.fillText(difficulty.tickMillis() + "ms x" + difficulty.scoreMultiplier(), left + rowWidth - 18, y + 24);
            i++;
        }
    }

    private void drawHint(GraphicsContext gc, String hint, double y) {
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFill(MUTED_TEXT);
        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        gc.fillText(hint, getWidth() / 2, y);
    }
}
