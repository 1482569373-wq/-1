# 贪吃蛇

这是一个使用 Java 21、JavaFX、Maven 和 Android 原生 Canvas 实现的贪吃蛇游戏。

项目同时支持：

- 电脑端 JavaFX 窗口版
- 手机端 Android APK 版

## 运行电脑端

如果已经安装好 JDK 和 Maven，可以运行：

```powershell
mvn javafx:run
```

也可以使用项目自带脚本：

```powershell
.\scripts\run-game.ps1
```

如果 Java 和 Maven 没有安装，可以先运行：

```powershell
.\scripts\install-dev-tools.ps1
```

## 运行测试

```powershell
.\scripts\run-tests.ps1
```

## 打包 Android APK

构建 debug APK：

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\build-apk.ps1
```

APK 生成位置：

```text
androidApp\build\outputs\apk\debug\androidApp-debug.apk
```

如果手机已开启 USB 调试并连接电脑，可以安装：

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\install-apk.ps1
```

## 游戏玩法

### 难度

不同难度会影响移动速度和积分倍率：

- 简单：180ms，积分倍率 x1
- 普通：120ms，积分倍率 x2
- 困难：80ms，积分倍率 x3

### 障碍墙

棋盘中会生成多段障碍墙。正常状态下，撞到障碍墙会游戏结束。

### 特殊豆子

不同颜色的豆子有不同效果：

- 红色：普通豆子，正常加分并增长
- 黄色：奖励豆子，获得更高分数并增长
- 蓝色：无敌豆子，获得无敌时间，无敌期间撞墙、撞障碍、撞身体不会立刻结束
- 紫色：双倍豆子，获得双倍积分时间，期间吃豆子的得分翻倍

### 积分记录

游戏会记录每次结束时的分数，并显示当前最高分。记录保存在本次运行内，关闭游戏后会清空。

## 电脑端操作

- `1`、`2`、`3`：选择难度
- `Enter` 或 `Space`：开始或重试当前难度
- 方向键或 `WASD`：控制移动
- `Space`：游戏中暂停或继续
- `R`：重开当前难度
- `M` 或 `Esc`：从暂停/结束界面返回菜单

## 手机端操作

手机端为竖屏布局：

- 上方约 3/4 区域显示游戏画面
- 游戏画面下方显示难度、分数、最高分和当前豆子/特殊效果
- 最下方留白区域居中放置方向键、暂停和重开按钮

手机端操作方式：

- 点击难度按钮开始或切换难度
- 在游戏区域滑动控制方向
- 也可以使用底部方向按钮控制方向
- 点击暂停/继续按钮暂停或恢复游戏
- 点击重开按钮重新开始当前难度
