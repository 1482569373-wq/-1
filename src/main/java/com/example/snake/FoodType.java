package com.example.snake;

public enum FoodType {
    NORMAL(10, true),
    BONUS(25, true),
    INVINCIBLE(15, false),
    DOUBLE_SCORE(18, true);

    private final int baseScore;
    private final boolean growsSnake;

    FoodType(int baseScore, boolean growsSnake) {
        this.baseScore = baseScore;
        this.growsSnake = growsSnake;
    }

    public int baseScore() {
        return baseScore;
    }

    public boolean growsSnake() {
        return growsSnake;
    }
}
