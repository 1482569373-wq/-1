package com.example.snake;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class DifficultyTest {
    @Test
    void difficultiesUseRequestedTickIntervals() {
        assertEquals(180, Difficulty.EASY.tickMillis());
        assertEquals(120, Difficulty.NORMAL.tickMillis());
        assertEquals(80, Difficulty.HARD.tickMillis());
    }

    @Test
    void nanosecondIntervalsMatchMillisecondIntervals() {
        assertEquals(180_000_000L, Difficulty.EASY.tickNanos());
        assertEquals(120_000_000L, Difficulty.NORMAL.tickNanos());
        assertEquals(80_000_000L, Difficulty.HARD.tickNanos());
    }

    @Test
    void difficultiesUseIncreasingScoreMultipliers() {
        assertEquals(1, Difficulty.EASY.scoreMultiplier());
        assertEquals(2, Difficulty.NORMAL.scoreMultiplier());
        assertEquals(3, Difficulty.HARD.scoreMultiplier());
    }
}
