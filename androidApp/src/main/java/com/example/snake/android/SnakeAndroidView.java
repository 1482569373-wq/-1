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
import com.example.snake.GameScreen;
import com.example.snake.GameState;
import com.example.snake.Point;

public class SnakeAndroidView extends View {
    private static final int BACKGROUND = Color.rgb(18, 22, 29);
    private static final int PANEL = Color.rgb(27, 34, 45);
    private static final int GRID = Color.rgb(36, 44, 56);
    private static final int SNAKE_HEAD = Color.rgb(90, 226, 142);
    private static final int SNAKE_BODY = Color.rgb(44, 168, 99);
    private static final int FOOD = Color.rgb(244, 79, 96);
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
        drawFood(canvas);
        drawSnake(canvas);
        drawHud(canvas);

        if (screen == GameScreen.MENU) {
            drawDifficultyOverlay(canvas, titleSnake(), subtitleChoose());
        } else if (screen == GameScreen.GAME_OVER) {
            drawDifficultyOverlay(canvas, titleGameOver(), subtitleScore());
        } else {
            drawControls(canvas);
            if (screen == GameScreen.PAUSED) {
                drawCenteredMessage(canvas, titlePaused(), subtitlePaused());
            }
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
        float sidePadding = Math.max(18f, width * 0.045f);
        float topPadding = Math.max(48f, height * 0.052f);
        float controlAreaHeight = Math.min(300f, height * 0.34f);
        float availableBoardHeight = height - topPadding - controlAreaHeight - 18f;

        cellSize = Math.min((width - sidePadding * 2f) / state.columns(), availableBoardHeight / state.rows());
        float boardWidth = cellSize * state.columns();
        float boardHeight = cellSize * state.rows();
        float boardLeft = (width - boardWidth) / 2f;
        boardRect.set(boardLeft, topPadding, boardLeft + boardWidth, topPadding + boardHeight);

        float buttonSize = Math.min(78f, width * 0.18f);
        float gap = Math.max(10f, width * 0.025f);
        float centerX = width / 2f;
        float controlsTop = boardRect.bottom + Math.max(20f, height * 0.025f);
        upButton.set(centerX - buttonSize / 2f, controlsTop, centerX + buttonSize / 2f, controlsTop + buttonSize);
        leftButton.set(centerX - buttonSize * 1.5f - gap, controlsTop + buttonSize + gap,
                centerX - buttonSize / 2f - gap, controlsTop + buttonSize * 2f + gap);
        downButton.set(centerX - buttonSize / 2f, controlsTop + buttonSize + gap,
                centerX + buttonSize / 2f, controlsTop + buttonSize * 2f + gap);
        rightButton.set(centerX + buttonSize / 2f + gap, controlsTop + buttonSize + gap,
                centerX + buttonSize * 1.5f + gap, controlsTop + buttonSize * 2f + gap);

        float commandTop = Math.min(height - 58f, downButton.bottom + 16f);
        pauseButton.set(sidePadding, commandTop, width / 2f - 8f, commandTop + 44f);
        restartButton.set(width / 2f + 8f, commandTop, width - sidePadding, commandTop + 44f);

        float menuWidth = Math.min(width - sidePadding * 2f, 360f);
        float menuLeft = (width - menuWidth) / 2f;
        float menuTop = height / 2f - 24f;
        for (int i = 0; i < difficultyButtons.length; i++) {
            difficultyButtons[i].set(menuLeft, menuTop + i * 58f, menuLeft + menuWidth, menuTop + i * 58f + 46f);
        }
    }

    private boolean handleTap(float x, float y) {
        if (screen == GameScreen.MENU || screen == GameScreen.GAME_OVER) {
            for (int i = 0; i < difficultyButtons.length; i++) {
                if (difficultyButtons[i].contains(x, y)) {
                    startGame(Difficulty.values()[i]);
                    return true;
                }
            }
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
        state.reset();
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

    private void drawFood(Canvas canvas) {
        Point food = state.food();
        if (food == null) {
            return;
        }
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(FOOD);
        float cx = boardRect.left + food.x() * cellSize + cellSize / 2f;
        float cy = boardRect.top + food.y() * cellSize + cellSize / 2f;
        canvas.drawCircle(cx, cy, cellSize * 0.32f, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2f);
        paint.setColor(Color.rgb(255, 175, 185));
        canvas.drawCircle(cx, cy, cellSize * 0.22f, paint);
    }

    private void drawSnake(Canvas canvas) {
        boolean first = true;
        int index = 0;
        for (Point segment : state.snake()) {
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(first ? SNAKE_HEAD : fadeBody(index));
            float inset = first ? cellSize * 0.10f : cellSize * 0.17f;
            RectF rect = new RectF(
                    boardRect.left + segment.x() * cellSize + inset,
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

    private void drawHud(Canvas canvas) {
        paint.setStyle(Paint.Style.FILL);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setColor(TEXT);
        paint.setTextSize(32f);
        paint.setFakeBoldText(true);
        canvas.drawText(textScore() + state.score(), 24f, 40f, paint);
        paint.setFakeBoldText(false);
        paint.setColor(MUTED_TEXT);
        paint.setTextSize(20f);
        canvas.drawText(difficulty.chineseLabel() + " " + difficulty.tickMillis() + "ms", getWidth() - 128f, 39f, paint);
    }

    private void drawControls(Canvas canvas) {
        drawButton(canvas, upButton, "\u2191", CONTROL);
        drawButton(canvas, leftButton, "\u2190", CONTROL);
        drawButton(canvas, downButton, "\u2193", CONTROL);
        drawButton(canvas, rightButton, "\u2192", CONTROL);
        drawCommandButton(canvas, pauseButton, screen == GameScreen.PAUSED ? textContinue() : textPause());
        drawCommandButton(canvas, restartButton, textRestart());
    }

    private void drawDifficultyOverlay(Canvas canvas, String title, String subtitle) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(OVERLAY);
        canvas.drawRect(0, 0, getWidth(), getHeight(), paint);

        RectF titleRect = new RectF(0, getHeight() / 2f - 150f, getWidth(), getHeight() / 2f - 94f);
        RectF subtitleRect = new RectF(0, getHeight() / 2f - 94f, getWidth(), getHeight() / 2f - 52f);
        drawCenteredText(canvas, titleRect, title, 42f, TEXT, true);
        drawCenteredText(canvas, subtitleRect, subtitle, 23f, MUTED_TEXT, false);

        Difficulty[] values = Difficulty.values();
        for (int i = 0; i < values.length; i++) {
            Difficulty option = values[i];
            int color = option == difficulty ? CONTROL_ACTIVE : PANEL;
            drawButton(canvas, difficultyButtons[i], option.chineseLabel() + "  " + option.tickMillis() + "ms", color);
        }
    }

    private void drawCenteredMessage(Canvas canvas, String title, String subtitle) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(OVERLAY);
        canvas.drawRect(0, 0, getWidth(), getHeight(), paint);
        RectF titleRect = new RectF(0, getHeight() / 2f - 72f, getWidth(), getHeight() / 2f - 12f);
        RectF subtitleRect = new RectF(0, getHeight() / 2f - 4f, getWidth(), getHeight() / 2f + 42f);
        drawCenteredText(canvas, titleRect, title, 42f, TEXT, true);
        drawCenteredText(canvas, subtitleRect, subtitle, 23f, MUTED_TEXT, false);
    }

    private void drawButton(Canvas canvas, RectF rect, String label, int color) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);
        canvas.drawRoundRect(rect, 18f, 18f, paint);
        drawCenteredText(canvas, rect, label, 28f, TEXT, true);
    }

    private void drawCommandButton(Canvas canvas, RectF rect, String label) {
        drawButton(canvas, rect, label, CONTROL_ACTIVE);
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

    private String titleSnake() {
        return "\u8d2a\u5403\u86c7";
    }

    private String titleGameOver() {
        return "\u6e38\u620f\u7ed3\u675f";
    }

    private String titlePaused() {
        return "\u6682\u505c";
    }

    private String subtitleChoose() {
        return "\u9009\u62e9\u96be\u5ea6\u5f00\u59cb\u6e38\u620f";
    }

    private String subtitleScore() {
        return textScore() + state.score() + "  \u00b7  \u91cd\u65b0\u9009\u62e9\u96be\u5ea6";
    }

    private String subtitlePaused() {
        return "\u70b9\u51fb\u7ee7\u7eed\u56de\u5230\u6e38\u620f";
    }

    private String textScore() {
        return "\u5206\u6570: ";
    }

    private String textPause() {
        return "\u6682\u505c";
    }

    private String textContinue() {
        return "\u7ee7\u7eed";
    }

    private String textRestart() {
        return "\u91cd\u5f00";
    }
}
