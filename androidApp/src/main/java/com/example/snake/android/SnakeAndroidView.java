package com.example.snake.android;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;

import com.example.snake.Difficulty;
import com.example.snake.Direction;
import com.example.snake.Food;
import com.example.snake.FoodType;
import com.example.snake.GameScreen;
import com.example.snake.GameState;
import com.example.snake.Point;

public class SnakeAndroidView extends View {
    private static final int BACKGROUND = Color.rgb(18, 22, 29);
    private static final int PANEL = Color.rgb(27, 34, 45);
    private static final int GRID = Color.rgb(36, 44, 56);
    private static final int SNAKE_HEAD = Color.rgb(90, 226, 142);
    private static final int SNAKE_BODY = Color.rgb(44, 168, 99);
    private static final int NORMAL_FOOD = Color.rgb(244, 79, 96);
    private static final int BONUS_FOOD = Color.rgb(255, 196, 87);
    private static final int INVINCIBLE_FOOD = Color.rgb(105, 214, 255);
    private static final int DOUBLE_FOOD = Color.rgb(189, 130, 255);
    private static final int OBSTACLE = Color.rgb(116, 127, 143);
    private static final int TEXT = Color.rgb(239, 244, 250);
    private static final int MUTED_TEXT = Color.rgb(160, 171, 186);
    private static final int CONTROL = Color.rgb(45, 54, 68);
    private static final int CONTROL_ACTIVE = Color.rgb(89, 155, 255);
    private static final int OVERLAY = Color.argb(172, 0, 0, 0);
    private static final float MIN_SWIPE_DISTANCE = 34f;

    private final GameState state = new GameState();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF boardRect = new RectF();
    private final RectF settingsRect = new RectF();
    private final RectF controlRect = new RectF();
    private final RectF upButton = new RectF();
    private final RectF downButton = new RectF();
    private final RectF leftButton = new RectF();
    private final RectF rightButton = new RectF();
    private final RectF pauseButton = new RectF();
    private final RectF restartButton = new RectF();
    private final RectF[] difficultyButtons = new RectF[] { new RectF(), new RectF(), new RectF() };

    private Difficulty difficulty = Difficulty.NORMAL;
    private GameScreen screen = GameScreen.MENU;
    private float cellSize;
    private float touchStartX;
    private float touchStartY;
    private boolean swipeConsumed;

    private final Runnable gameLoop = new Runnable() {
        @Override
        public void run() {
            if (screen == GameScreen.PLAYING) {
                state.tick();
                if (state.isGameOver()) {
                    screen = GameScreen.GAME_OVER;
                }
                invalidate();
            }
            handler.postDelayed(this, difficulty.tickMillis());
        }
    };

    public SnakeAndroidView(Context context) {
        super(context);
        setFocusable(true);
        setKeepScreenOn(true);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        handler.post(gameLoop);
    }

    @Override
    protected void onDetachedFromWindow() {
        handler.removeCallbacks(gameLoop);
        super.onDetachedFromWindow();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        calculateLayout();
        drawBackground(canvas);
        drawBoard(canvas);
        drawObstacles(canvas);
        drawFood(canvas);
        drawSnake(canvas);
        drawSettings(canvas);
        drawControls(canvas);

        if (screen == GameScreen.MENU) {
            drawMessage(canvas, titleSnake(), subtitleChoose());
        } else if (screen == GameScreen.GAME_OVER) {
            drawMessage(canvas, titleGameOver(), subtitleScore());
        } else if (screen == GameScreen.PAUSED) {
            drawMessage(canvas, titlePaused(), subtitlePaused());
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            touchStartX = x;
            touchStartY = y;
            swipeConsumed = false;
            return true;
        }
        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            if (screen == GameScreen.PLAYING && handleSwipe(x, y)) {
                swipeConsumed = true;
                touchStartX = x;
                touchStartY = y;
                invalidate();
            }
            return true;
        }
        if (event.getAction() != MotionEvent.ACTION_UP) {
            return true;
        }
        if (handleTap(x, y)) {
            invalidate();
            performClick();
            return true;
        }
        if (!swipeConsumed && screen == GameScreen.PLAYING && handleSwipe(x, y)) {
            invalidate();
        }
        performClick();
        return true;
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true;
    }

    private void calculateLayout() {
        float width = getWidth();
        float height = getHeight();
        float margin = Math.max(12f, width * 0.035f);
        float reservedBottom = height * 0.25f;
        float settingsHeight = Math.max(92f, height * 0.13f);
        float boardAreaTop = margin;
        float boardAreaBottom = height - reservedBottom - settingsHeight - margin * 0.5f;
        float boardAreaHeight = boardAreaBottom - boardAreaTop;

        cellSize = Math.min((width - margin * 2f) / state.columns(), boardAreaHeight / state.rows());
        float boardWidth = cellSize * state.columns();
        float boardHeight = cellSize * state.rows();
        float boardLeft = (width - boardWidth) / 2f;
        float boardTop = boardAreaTop + Math.max(0, (boardAreaHeight - boardHeight) / 2f);
        boardRect.set(boardLeft, boardTop, boardLeft + boardWidth, boardTop + boardHeight);

        float settingsTop = boardAreaBottom + margin * 0.5f;
        settingsRect.set(margin, settingsTop, width - margin, settingsTop + settingsHeight);
        controlRect.set(margin, settingsRect.bottom + margin * 0.5f, width - margin, height - margin);

        float chipGap = 8f;
        float chipWidth = (settingsRect.width() - chipGap * 2f) / 3f;
        float chipTop = settingsRect.top + 42f;
        for (int i = 0; i < difficultyButtons.length; i++) {
            float left = settingsRect.left + i * (chipWidth + chipGap);
            difficultyButtons[i].set(left, chipTop, left + chipWidth, chipTop + 38f);
        }

        // 方向键是手机端最常用的操作，所以这里优先保证它们足够大。
        // 旧版最大只有 66px，手指点击时容易误触；新版会根据屏幕宽度和控制区高度动态放大。
        float desiredButtonSize = Math.min(92f, Math.max(72f, Math.min(width * 0.20f, controlRect.height() * 0.52f)));
        float gap = Math.max(12f, desiredButtonSize * 0.16f);
        // 如果遇到特别矮的屏幕，按钮会自动收一点，避免下排方向键跑出屏幕底部。
        float buttonSize = Math.min(desiredButtonSize, Math.max(56f, (controlRect.height() - gap) / 2f));
        gap = Math.max(10f, buttonSize * 0.16f);

        float centerX = width / 2f;
        float top = controlRect.top + Math.max(0, (controlRect.height() - buttonSize * 2f - gap) / 2f);
        upButton.set(centerX - buttonSize / 2f, top, centerX + buttonSize / 2f, top + buttonSize);
        leftButton.set(centerX - buttonSize * 1.5f - gap, top + buttonSize + gap,
                centerX - buttonSize / 2f - gap, top + buttonSize * 2f + gap);
        downButton.set(centerX - buttonSize / 2f, top + buttonSize + gap,
                centerX + buttonSize / 2f, top + buttonSize * 2f + gap);
        rightButton.set(centerX + buttonSize / 2f + gap, top + buttonSize + gap,
                centerX + buttonSize * 1.5f + gap, top + buttonSize * 2f + gap);

        // 暂停和重开按钮放在上排两侧，给下排的左/下/右方向键腾出完整空间。
        float commandWidth = Math.min(92f, Math.max(64f, centerX - buttonSize / 2f - gap - margin));
        pauseButton.set(margin, upButton.top, margin + commandWidth, upButton.bottom);
        restartButton.set(width - margin - commandWidth, upButton.top, width - margin, upButton.bottom);
    }

    private boolean handleTap(float x, float y) {
        for (int i = 0; i < difficultyButtons.length; i++) {
            if (difficultyButtons[i].contains(x, y)) {
                startGame(Difficulty.values()[i]);
                return true;
            }
        }
        if (screen == GameScreen.MENU || screen == GameScreen.GAME_OVER) {
            return false;
        }
        if (upButton.contains(x, y)) {
            state.requestDirection(Direction.UP);
            return true;
        }
        if (downButton.contains(x, y)) {
            state.requestDirection(Direction.DOWN);
            return true;
        }
        if (leftButton.contains(x, y)) {
            state.requestDirection(Direction.LEFT);
            return true;
        }
        if (rightButton.contains(x, y)) {
            state.requestDirection(Direction.RIGHT);
            return true;
        }
        if (pauseButton.contains(x, y)) {
            screen = screen == GameScreen.PAUSED ? GameScreen.PLAYING : GameScreen.PAUSED;
            return true;
        }
        if (restartButton.contains(x, y)) {
            startGame(difficulty);
            return true;
        }
        return false;
    }

    private boolean handleSwipe(float x, float y) {
        float dx = x - touchStartX;
        float dy = y - touchStartY;
        if (Math.hypot(dx, dy) < MIN_SWIPE_DISTANCE) {
            return false;
        }
        if (Math.abs(dx) > Math.abs(dy)) {
            state.requestDirection(dx > 0 ? Direction.RIGHT : Direction.LEFT);
        } else {
            state.requestDirection(dy > 0 ? Direction.DOWN : Direction.UP);
        }
        return true;
    }

    private void startGame(Difficulty nextDifficulty) {
        difficulty = nextDifficulty;
        state.reset(nextDifficulty);
        screen = GameScreen.PLAYING;
    }

    private void drawBackground(Canvas canvas) {
        canvas.drawColor(BACKGROUND);
    }

    private void drawBoard(Canvas canvas) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(BACKGROUND);
        canvas.drawRoundRect(boardRect, 12f, 12f, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1f);
        paint.setColor(GRID);
        for (int x = 0; x <= state.columns(); x++) {
            float px = boardRect.left + x * cellSize;
            canvas.drawLine(px, boardRect.top, px, boardRect.bottom, paint);
        }
        for (int y = 0; y <= state.rows(); y++) {
            float py = boardRect.top + y * cellSize;
            canvas.drawLine(boardRect.left, py, boardRect.right, py, paint);
        }
    }

    private void drawObstacles(Canvas canvas) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(OBSTACLE);
        for (Point obstacle : state.obstacles()) {
            float inset = Math.max(2f, cellSize * 0.13f);
            RectF rect = new RectF(boardRect.left + obstacle.x() * cellSize + inset,
                    boardRect.top + obstacle.y() * cellSize + inset,
                    boardRect.left + (obstacle.x() + 1) * cellSize - inset,
                    boardRect.top + (obstacle.y() + 1) * cellSize - inset);
            canvas.drawRoundRect(rect, cellSize * 0.14f, cellSize * 0.14f, paint);
        }
    }

    private void drawFood(Canvas canvas) {
        Food food = state.food();
        if (food == null) {
            return;
        }
        Point point = food.position();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(foodColor(food.type()));
        float cx = boardRect.left + point.x() * cellSize + cellSize / 2f;
        float cy = boardRect.top + point.y() * cellSize + cellSize / 2f;
        canvas.drawCircle(cx, cy, cellSize * 0.32f, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2f);
        paint.setColor(Color.WHITE);
        canvas.drawCircle(cx, cy, cellSize * 0.22f, paint);
    }

    private int foodColor(FoodType type) {
        if (type == FoodType.BONUS) {
            return BONUS_FOOD;
        }
        if (type == FoodType.INVINCIBLE) {
            return INVINCIBLE_FOOD;
        }
        if (type == FoodType.DOUBLE_SCORE) {
            return DOUBLE_FOOD;
        }
        return NORMAL_FOOD;
    }

    private void drawSnake(Canvas canvas) {
        boolean first = true;
        int index = 0;
        for (Point segment : state.snake()) {
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(first ? (state.isInvincible() ? INVINCIBLE_FOOD : SNAKE_HEAD) : fadeBody(index));
            float inset = first ? cellSize * 0.10f : cellSize * 0.17f;
            RectF rect = new RectF(boardRect.left + segment.x() * cellSize + inset,
                    boardRect.top + segment.y() * cellSize + inset,
                    boardRect.left + (segment.x() + 1) * cellSize - inset,
                    boardRect.top + (segment.y() + 1) * cellSize - inset);
            canvas.drawRoundRect(rect, cellSize * 0.22f, cellSize * 0.22f, paint);
            if (first) {
                drawEyes(canvas, segment);
            }
            first = false;
            index++;
        }
    }

    private int fadeBody(int index) {
        int green = Math.max(112, 168 - index * 2);
        return Color.rgb(44, green, 99);
    }

    private void drawEyes(Canvas canvas, Point head) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(12, 18, 22));
        float baseX = boardRect.left + head.x() * cellSize;
        float baseY = boardRect.top + head.y() * cellSize;
        float eye = Math.max(3f, cellSize * 0.16f);
        canvas.drawCircle(baseX + cellSize * 0.36f, baseY + cellSize * 0.36f, eye, paint);
        canvas.drawCircle(baseX + cellSize * 0.64f, baseY + cellSize * 0.36f, eye, paint);
    }

    private void drawSettings(Canvas canvas) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(PANEL);
        canvas.drawRoundRect(settingsRect, 16f, 16f, paint);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setColor(TEXT);
        paint.setTextSize(24f);
        paint.setFakeBoldText(true);
        canvas.drawText(textScore() + state.score() + "   " + textBest() + state.highScore(), settingsRect.left + 14f, settingsRect.top + 28f, paint);
        paint.setFakeBoldText(false);
        paint.setColor(MUTED_TEXT);
        paint.setTextSize(18f);
        canvas.drawText(effectText(), settingsRect.right - 148f, settingsRect.top + 28f, paint);

        Difficulty[] values = Difficulty.values();
        for (int i = 0; i < values.length; i++) {
            Difficulty option = values[i];
            drawButton(canvas, difficultyButtons[i], option.chineseLabel() + " x" + option.scoreMultiplier(),
                    option == difficulty ? CONTROL_ACTIVE : CONTROL, 20f);
        }
    }

    private String effectText() {
        if (state.isInvincible()) {
            return textInvincible() + state.invincibleTicks();
        }
        if (state.isDoubleScoreActive()) {
            return textDouble() + state.doubleScoreTicks();
        }
        Food food = state.food();
        return food == null ? "" : foodName(food.type());
    }

    private String foodName(FoodType type) {
        if (type == FoodType.BONUS) {
            return textBonusFood();
        }
        if (type == FoodType.INVINCIBLE) {
            return textInvFood();
        }
        if (type == FoodType.DOUBLE_SCORE) {
            return textDoubleFood();
        }
        return textNormalFood();
    }

    private void drawControls(Canvas canvas) {
        drawButton(canvas, upButton, "\u2191", CONTROL, 30f);
        drawButton(canvas, leftButton, "\u2190", CONTROL, 30f);
        drawButton(canvas, downButton, "\u2193", CONTROL, 30f);
        drawButton(canvas, rightButton, "\u2192", CONTROL, 30f);
        drawButton(canvas, pauseButton, screen == GameScreen.PAUSED ? textContinue() : textPause(), CONTROL_ACTIVE, 21f);
        drawButton(canvas, restartButton, textRestart(), CONTROL_ACTIVE, 21f);
    }

    private void drawMessage(Canvas canvas, String title, String subtitle) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(OVERLAY);
        canvas.drawRect(0, 0, getWidth(), settingsRect.top, paint);
        RectF titleRect = new RectF(boardRect.left, boardRect.top + boardRect.height() * 0.24f, boardRect.right, boardRect.top + boardRect.height() * 0.38f);
        RectF subtitleRect = new RectF(boardRect.left, titleRect.bottom + 4f, boardRect.right, titleRect.bottom + 48f);
        drawCenteredText(canvas, titleRect, title, 38f, TEXT, true);
        drawCenteredText(canvas, subtitleRect, subtitle, 21f, MUTED_TEXT, false);
    }

    private void drawButton(Canvas canvas, RectF rect, String label, int color, float textSize) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);
        canvas.drawRoundRect(rect, 16f, 16f, paint);
        drawCenteredText(canvas, rect, label, textSize, TEXT, true);
    }

    private void drawCenteredText(Canvas canvas, RectF rect, String text, float size, int color, boolean bold) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);
        paint.setTextSize(size);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setFakeBoldText(bold);
        Paint.FontMetrics metrics = paint.getFontMetrics();
        float baseline = rect.centerY() - (metrics.ascent + metrics.descent) / 2f;
        canvas.drawText(text, rect.centerX(), baseline, paint);
        paint.setFakeBoldText(false);
    }

    private String titleSnake() { return "\u8d2a\u5403\u86c7"; }
    private String titleGameOver() { return "\u6e38\u620f\u7ed3\u675f"; }
    private String titlePaused() { return "\u6682\u505c"; }
    private String subtitleChoose() { return "\u70b9\u51fb\u96be\u5ea6\u5f00\u59cb"; }
    private String subtitleScore() { return textScore() + state.score() + "  \u00b7  " + textBest() + state.highScore(); }
    private String subtitlePaused() { return "\u70b9\u51fb\u7ee7\u7eed\u56de\u5230\u6e38\u620f"; }
    private String textScore() { return "\u5206\u6570: "; }
    private String textBest() { return "\u6700\u9ad8: "; }
    private String textPause() { return "\u6682\u505c"; }
    private String textContinue() { return "\u7ee7\u7eed"; }
    private String textRestart() { return "\u91cd\u5f00"; }
    private String textInvincible() { return "\u65e0\u654c "; }
    private String textDouble() { return "\u53cc\u500d "; }
    private String textNormalFood() { return "\u666e\u901a\u8c46"; }
    private String textBonusFood() { return "\u5956\u52b1\u8c46"; }
    private String textInvFood() { return "\u65e0\u654c\u8c46"; }
    private String textDoubleFood() { return "\u53cc\u500d\u8c46"; }
}
