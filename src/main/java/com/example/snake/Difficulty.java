package com.example.snake;

public enum Difficulty {
    EASY("Simple", "\u7b80\u5355", 180),
    NORMAL("Normal", "\u666e\u901a", 120),
    HARD("Hard", "\u56f0\u96be", 80);

    private final String englishLabel;
    private final String chineseLabel;
    private final long tickMillis;

    Difficulty(String englishLabel, String chineseLabel, long tickMillis) {
        this.englishLabel = englishLabel;
        this.chineseLabel = chineseLabel;
        this.tickMillis = tickMillis;
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
}
