package com.example.snake;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

class GameStateTest {
    @Test
    void tickMovesSnakeForward() {
        GameState state = new GameState(10, 10,
                List.of(new Point(4, 5), new Point(3, 5), new Point(2, 5)),
                Direction.RIGHT,
                new Food(new Point(8, 8), FoodType.NORMAL));

        state.tick();

        assertEquals(new Point(5, 5), state.head());
        assertEquals(3, state.snake().size());
        assertFalse(state.isGameOver());
    }

    @Test
    void eatingNormalFoodGrowsSnakeAndAddsDifficultyScore() {
        GameState state = new GameState(10, 10,
                List.of(new Point(4, 5), new Point(3, 5), new Point(2, 5)),
                Direction.RIGHT,
                new Food(new Point(5, 5), FoodType.NORMAL));

        state.tick();

        assertEquals(new Point(5, 5), state.head());
        assertEquals(4, state.snake().size());
        assertEquals(20, state.score());
        assertFalse(state.snake().contains(state.foodPosition()));
    }

    @Test
    void doubleScoreFoodDoublesNextFoodScore() {
        GameState state = new GameState(10, 10,
                List.of(new Point(4, 5), new Point(3, 5), new Point(2, 5)),
                Direction.RIGHT,
                new Food(new Point(5, 5), FoodType.DOUBLE_SCORE));

        state.tick();

        assertTrue(state.isDoubleScoreActive());
        assertEquals(36, state.score());
    }

    @Test
    void invincibleFoodStartsInvincibleTimerWithoutGrowing() {
        GameState state = new GameState(10, 10,
                List.of(new Point(4, 5), new Point(3, 5), new Point(2, 5)),
                Direction.RIGHT,
                new Food(new Point(5, 5), FoodType.INVINCIBLE));

        state.tick();

        assertTrue(state.isInvincible());
        assertEquals(3, state.snake().size());
        assertEquals(30, state.score());
    }

    @Test
    void hittingWallEndsGameAndRecordsScore() {
        GameState state = new GameState(10, 10,
                List.of(new Point(9, 5), new Point(8, 5), new Point(7, 5)),
                Direction.RIGHT,
                new Food(new Point(4, 4), FoodType.NORMAL));

        state.tick();

        assertTrue(state.isGameOver());
        assertEquals(List.of(0), state.scoreHistory());
    }

    @Test
    void hittingOwnBodyEndsGame() {
        GameState state = new GameState(10, 10,
                List.of(new Point(4, 4), new Point(4, 5), new Point(3, 5), new Point(3, 4)),
                Direction.RIGHT,
                new Food(new Point(5, 4), FoodType.NORMAL));

        state.requestDirection(Direction.DOWN);
        state.tick();

        assertTrue(state.isGameOver());
    }

    @Test
    void reverseDirectionInputIsIgnored() {
        GameState state = new GameState(10, 10,
                List.of(new Point(4, 5), new Point(3, 5), new Point(2, 5)),
                Direction.RIGHT,
                new Food(new Point(8, 8), FoodType.NORMAL));

        state.requestDirection(Direction.LEFT);
        state.tick();

        assertEquals(Direction.RIGHT, state.direction());
        assertEquals(new Point(5, 5), state.head());
        assertFalse(state.isGameOver());
    }

    @Test
    void pauseStopsTicksUntilResumed() {
        GameState state = new GameState(10, 10,
                List.of(new Point(4, 5), new Point(3, 5), new Point(2, 5)),
                Direction.RIGHT,
                new Food(new Point(8, 8), FoodType.NORMAL));

        state.togglePause();
        state.tick();

        assertEquals(new Point(4, 5), state.head());
        state.togglePause();
        state.tick();

        assertEquals(new Point(5, 5), state.head());
    }

    @Test
    void resetCreatesObstaclesAndUsesSelectedDifficulty() {
        GameState state = new GameState();

        state.reset(Difficulty.HARD);

        assertEquals(Difficulty.HARD, state.difficulty());
        assertFalse(state.obstacles().isEmpty());
    }
}
