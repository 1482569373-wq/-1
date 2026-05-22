package com.example.snake;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class SnakeGameApp extends Application {
    private final GameState state = new GameState();
    private Difficulty difficulty = Difficulty.NORMAL;
    private GameScreen screen = GameScreen.MENU;
    private GameBoard board;

    @Override
    public void start(Stage stage) {
        board = new GameBoard(state);
        StackPane root = new StackPane(board);
        Scene scene = new Scene(root);

        scene.setOnKeyPressed(event -> {
            handleKey(event.getCode());
            board.draw(screen, difficulty);
        });

        AnimationTimer timer = new AnimationTimer() {
            private long lastTick;

            @Override
            public void handle(long now) {
                if (lastTick == 0) {
                    lastTick = now;
                    board.draw(screen, difficulty);
                    return;
                }
                if (screen == GameScreen.PLAYING && now - lastTick >= difficulty.tickNanos()) {
                    state.tick();
                    if (state.isGameOver()) {
                        screen = GameScreen.GAME_OVER;
                    }
                    board.draw(screen, difficulty);
                    lastTick = now;
                }
            }
        };

        stage.setTitle("Snake - Difficulty Select");
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();
        board.requestFocus();
        board.draw(screen, difficulty);
        timer.start();
    }

    private void handleKey(KeyCode code) {
        Difficulty selected = difficultyFromKey(code);
        if (selected != null) {
            startGame(selected);
            return;
        }

        if (screen == GameScreen.MENU) {
            if (code == KeyCode.ENTER || code == KeyCode.SPACE) {
                startGame(difficulty);
            }
            return;
        }

        if (screen == GameScreen.GAME_OVER) {
            if (code == KeyCode.R || code == KeyCode.ENTER || code == KeyCode.SPACE) {
                startGame(difficulty);
            } else if (code == KeyCode.M || code == KeyCode.ESCAPE) {
                screen = GameScreen.MENU;
            }
            return;
        }

        if (screen == GameScreen.PAUSED) {
            if (code == KeyCode.SPACE || code == KeyCode.ENTER) {
                screen = GameScreen.PLAYING;
            } else if (code == KeyCode.M || code == KeyCode.ESCAPE) {
                screen = GameScreen.MENU;
            }
            return;
        }

        if (code == KeyCode.UP || code == KeyCode.W) {
            state.requestDirection(Direction.UP);
        } else if (code == KeyCode.DOWN || code == KeyCode.S) {
            state.requestDirection(Direction.DOWN);
        } else if (code == KeyCode.LEFT || code == KeyCode.A) {
            state.requestDirection(Direction.LEFT);
        } else if (code == KeyCode.RIGHT || code == KeyCode.D) {
            state.requestDirection(Direction.RIGHT);
        } else if (code == KeyCode.SPACE) {
            screen = GameScreen.PAUSED;
        } else if (code == KeyCode.R) {
            startGame(difficulty);
        } else if (code == KeyCode.M || code == KeyCode.ESCAPE) {
            screen = GameScreen.MENU;
        }
    }

    private void startGame(Difficulty nextDifficulty) {
        difficulty = nextDifficulty;
        state.reset(nextDifficulty);
        screen = GameScreen.PLAYING;
    }

    private Difficulty difficultyFromKey(KeyCode code) {
        if (code == KeyCode.DIGIT1 || code == KeyCode.NUMPAD1) {
            return Difficulty.EASY;
        }
        if (code == KeyCode.DIGIT2 || code == KeyCode.NUMPAD2) {
            return Difficulty.NORMAL;
        }
        if (code == KeyCode.DIGIT3 || code == KeyCode.NUMPAD3) {
            return Difficulty.HARD;
        }
        return null;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
