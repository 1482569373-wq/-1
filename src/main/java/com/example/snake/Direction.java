package com.example.snake;

/**
 * 蛇可以移动的四个方向。
 *
 * <p>每个方向都保存一个横向偏移 dx 和纵向偏移 dy。
 * 例如 RIGHT 的 dx 是 1，表示 x 坐标加 1；UP 的 dy 是 -1，
 * 表示在屏幕坐标系里向上移动一格。</p>
 */
public enum Direction {
    // JavaFX 画布的原点在左上角，y 越小越靠上，所以 UP 的 dy 是 -1。
    UP(0, -1),
    // y 越大越靠下，所以 DOWN 的 dy 是 1。
    DOWN(0, 1),
    // x 越小越靠左。
    LEFT(-1, 0),
    // x 越大越靠右。
    RIGHT(1, 0);

    // 每次移动时 x 坐标要增加的值。
    private final int dx;
    // 每次移动时 y 坐标要增加的值。
    private final int dy;

    Direction(int dx, int dy) {
        this.dx = dx;
        this.dy = dy;
    }

    public int dx() {
        return dx;
    }

    public int dy() {
        return dy;
    }

    /**
     * 判断两个方向是否完全相反。
     *
     * <p>贪吃蛇通常不能立刻 180 度掉头。
     * 例如当前向右移动时，如果马上接受 LEFT，蛇头会直接撞到自己的第二节身体。
     * 所以 GameState 会用这个方法过滤掉反向输入。</p>
     */
    public boolean isOpposite(Direction other) {
        return dx + other.dx == 0 && dy + other.dy == 0;
    }
}
