package com.example.snake;

import java.util.Objects;

/**
 * 棋盘上的一个格子坐标。
 *
 * <p>这个类是不可变对象：x 和 y 创建后就不会再改变。
 * 贪吃蛇需要频繁判断“蛇头是否等于食物位置”“新蛇头是否撞到身体”，
 * 所以这里手动实现 equals() 和 hashCode()，让坐标可以按数值比较。</p>
 *
 * <p>这里没有使用 Java record，是为了让同一份代码更稳定地被 Android 工具链编译。</p>
 */
public final class Point {
    private final int x;
    private final int y;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    /**
     * 根据方向得到移动后的新坐标。
     *
     * <p>这里不修改当前 Point，而是返回一个新的 Point。
     * 这样坐标对象是不可变的，游戏状态更容易推理，也不容易因为共享引用产生 bug。</p>
     */
    public Point translate(Direction direction) {
        return new Point(x + direction.dx(), y + direction.dy());
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Point point)) {
            return false;
        }
        return x == point.x && y == point.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return "Point[x=" + x + ", y=" + y + "]";
    }
}
