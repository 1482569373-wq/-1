package com.example.snake;

public enum Difficulty {
    EASY("Simple", "\u7b80\u5355", 180, 1),
    NORMAL("Normal", "\u666e\u901a", 120, 2),
    HARD("Hard", "\u56f0\u96be", 80, 3);

    private final String englishLabel;
    private final String chineseLabel;
    private final long tickMillis;
    private final int scoreMultiplier;

    Difficulty(String englishLabel, String chineseLabel, long tickMillis, int scoreMultiplier) {
        this.englishLabel = englishLabel;
        this.chineseLabel = chineseLabel;
        this.tickMillis = tickMillis;
        this.scoreMultiplier = scoreMultiplier;
    }

    public String englishLabel() {
        return englishLabel;
    }

    public String chineseLabel() {
        return chineseLabel;
    }

    public long tickMillis() {
        return tickMillis;
    }

    public long tickNanos() {
        return tickMillis * 1_000_000L;
    }

    public int scoreMultiplier() {
        return scoreMultiplier;
    }
}
