package com.example.snake;

public final class Food {
    private final Point position;
    private final FoodType type;

    public Food(Point position, FoodType type) {
        this.position = position;
        this.type = type;
    }

    public Point position() {
        return position;
    }

    public FoodType type() {
        return type;
    }
}
