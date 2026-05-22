package com.example.snake;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * GameState 的单元测试。
 *
 * <p>这些测试只验证游戏规则，不打开 JavaFX 窗口。
 * 通过测试专用构造方法手动摆放蛇和食物，可以稳定检查每一种关键情况。</p>
 */
class GameStateTest {
    @Test
    void tickMovesSnakeForward() {
        // 初始蛇头在 (4, 5)，方向向右，食物放远一点，确保不会吃到。
        GameState state = new GameState(10, 10,
                List.of(new Point(4, 5), new Point(3, 5), new Point(2, 5)),
                Direction.RIGHT,
                new Point(8, 8));

        state.tick();

        // 移动一帧后蛇头应到 (5, 5)，长度保持 3，游戏没有结束。
        assertEquals(new Point(5, 5), state.head());
        assertEquals(3, state.snake().size());
        assertFalse(state.isGameOver());
    }

    @Test
    void eatingFoodGrowsSnakeAndAddsScore() {
        // 食物放在蛇头正前方，所以下一帧会吃到。
        GameState state = new GameState(10, 10,
                List.of(new Point(4, 5), new Point(3, 5), new Point(2, 5)),
                Direction.RIGHT,
                new Point(5, 5));

        state.tick();

        // 吃到食物后：蛇头前进、长度加 1、分数加 10，并生成新食物。
        assertEquals(new Point(5, 5), state.head());
        assertEquals(4, state.snake().size());
        assertEquals(10, state.score());
        assertFalse(state.snake().contains(state.food()));
    }

    @Test
    void hittingWallEndsGame() {
        // 蛇头已经在最右边，再向右移动会越界。
        GameState state = new GameState(10, 10,
                List.of(new Point(9, 5), new Point(8, 5), new Point(7, 5)),
                Direction.RIGHT,
                new Point(4, 4));

        state.tick();

        assertTrue(state.isGameOver());
    }

    @Test
    void hittingOwnBodyEndsGame() {
        // 这条蛇摆成一个会在下一步向下撞到自己身体的形状。
        GameState state = new GameState(10, 10,
                List.of(new Point(4, 4), new Point(4, 5), new Point(3, 5), new Point(3, 4)),
                Direction.RIGHT,
                new Point(5, 4));

        // 当前方向是右，向下不是反向输入，所以会被接受。
        state.requestDirection(Direction.DOWN);
        state.tick();

        assertTrue(state.isGameOver());
    }

    @Test
    void reverseDirectionInputIsIgnored() {
        // 当前方向向右，玩家请求向左，这是 180 度反向，应该被忽略。
        GameState state = new GameState(10, 10,
                List.of(new Point(4, 5), new Point(3, 5), new Point(2, 5)),
                Direction.RIGHT,
                new Point(8, 8));

        state.requestDirection(Direction.LEFT);
        state.tick();

        assertEquals(Direction.RIGHT, state.direction());
        assertEquals(new Point(5, 5), state.head());
        assertFalse(state.isGameOver());
    }

    @Test
    void pauseStopsTicksUntilResumed() {
        // 暂停后 tick() 不应该改变蛇的位置；恢复后 tick() 才继续移动。
        GameState state = new GameState(10, 10,
                List.of(new Point(4, 5), new Point(3, 5), new Point(2, 5)),
                Direction.RIGHT,
                new Point(8, 8));

        state.togglePause();
        state.tick();

        assertEquals(new Point(4, 5), state.head());
        state.togglePause();
        state.tick();

        assertEquals(new Point(5, 5), state.head());
    }
}
