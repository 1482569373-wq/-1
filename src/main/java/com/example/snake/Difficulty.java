package com.example.snake;

/**
 * 游戏难度配置。
 *
 * <p>桌面版和 Android 版都会读取这里的速度、中文显示名和积分倍率，
 * 所以修改难度规则时只需要改这一处，两端会保持一致。</p>
 */
public enum Difficulty {
    EASY("简单", 180, 1),
    NORMAL("普通", 120, 2),
    HARD("困难", 80, 3);

    // 展示给玩家看的中文难度名称，例如菜单里的“简单”“普通”“困难”。
    private final String chineseLabel;
    // 每次移动之间等待的毫秒数；数值越小，蛇移动越快，难度越高。
    private final long tickMillis;
    // 吃到豆子时使用的基础积分倍率；困难模式倍率最高，用来奖励更高风险。
    private final int scoreMultiplier;

    Difficulty(String chineseLabel, long tickMillis, int scoreMultiplier) {
        this.chineseLabel = chineseLabel;
        this.tickMillis = tickMillis;
        this.scoreMultiplier = scoreMultiplier;
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