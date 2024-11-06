package com.cs407.tilt_2048;

import java.util.Random;

public class MainGame {
    private int[][] board;  // 4x4 游戏棋盘
    private int score;      // 当前分数
    private Random random;  // 随机数生成器

    public MainGame() {
        board = new int[4][4]; // 初始化 4x4 的棋盘
        score = 0;
        random = new Random();
        initBoard();
    }

    // 初始化棋盘并生成两个初始数字
    public void initBoard() {
        // 将棋盘清空
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                board[i][j] = 0;
            }
        }

        // 生成两个初始的数字
        generateNumber();
        generateNumber();
    }

    // 在棋盘上随机空位置生成一个新的数字（2 或 4）
    public void generateNumber() {
        int row, col;
        do {
            row = random.nextInt(4);
            col = random.nextInt(4);
        } while (board[row][col] != 0); // 找到一个空位置

        // 90% 生成 2，10% 生成 4
        board[row][col] = random.nextInt(10) < 9 ? 2 : 4;
    }

    // 获取当前的棋盘状态（用于调试）
    public int[][] getBoard() {
        return board;
    }

    // 获取当前分数
    public int getScore() {
        return score;
    }

    // 向左滑动棋盘
    public void slideLeft() {
        boolean moved = false; // 用于判断是否有移动发生

        for (int i = 0; i < 4; i++) {
            if (compressRow(board[i])) {
                moved = true;
            }
        }

        if (moved) {
            generateNumber(); // 如果有移动，生成一个新数字
        }
    }

    // 压缩并合并一行（向左滑动）
    private boolean compressRow(int[] row) {
        boolean moved = false;

        // 移动所有非零元素到左边
        int[] newRow = new int[4];
        int index = 0;
        for (int num : row) {
            if (num != 0) {
                newRow[index++] = num;
            }
        }

        // 合并相同的数字
        for (int j = 0; j < 3; j++) {
            if (newRow[j] != 0 && newRow[j] == newRow[j + 1]) {
                newRow[j] *= 2;
                score += newRow[j]; // 更新分数
                newRow[j + 1] = 0;
                moved = true;
            }
        }

        // 再次移动，去除合并后产生的空格
        index = 0;
        for (int num : newRow) {
            if (num != 0) {
                row[index++] = num;
            } else {
                row[index++] = 0;
            }
        }

        return moved;
    }

    // 向右滑动棋盘
    public void slideRight() {
        boolean moved = false;

        for (int i = 0; i < 4; i++) {
            reverseRow(board[i]);          // 反转行
            if (compressRow(board[i])) {   // 向左合并
                moved = true;
            }
            reverseRow(board[i]);          // 再次反转回原来的顺序
        }

        if (moved) {
            generateNumber();
        }
    }

    // 向上滑动棋盘
    public void slideUp() {
        boolean moved = false;

        for (int col = 0; col < 4; col++) {
            int[] column = getColumn(board, col); // 获取列数据
            if (compressRow(column)) {           // 向左合并
                moved = true;
            }
            setColumn(board, col, column);       // 更新原始棋盘列数据
        }

        if (moved) {
            generateNumber();
        }
    }

    // 向下滑动棋盘
    public void slideDown() {
        boolean moved = false;

        for (int col = 0; col < 4; col++) {
            int[] column = getColumn(board, col); // 获取列数据
            reverseRow(column);                   // 反转列
            if (compressRow(column)) {            // 向左合并
                moved = true;
            }
            reverseRow(column);                   // 反转回原来的顺序
            setColumn(board, col, column);        // 更新原始棋盘列数据
        }

        if (moved) {
            generateNumber();
        }
    }

    // 反转一行（用于向右和向下滑动）
    private void reverseRow(int[] row) {
        for (int i = 0; i < row.length / 2; i++) {
            int temp = row[i];
            row[i] = row[row.length - 1 - i];
            row[row.length - 1 - i] = temp;
        }
    }

    // 获取指定列的数据
    private int[] getColumn(int[][] board, int col) {
        int[] column = new int[4];
        for (int i = 0; i < 4; i++) {
            column[i] = board[i][col];
        }
        return column;
    }

    // 设置指定列的数据
    private void setColumn(int[][] board, int col, int[] column) {
        for (int i = 0; i < 4; i++) {
            board[i][col] = column[i];
        }
    }

    // 检查是否获胜（是否存在2048）
    public boolean checkWin() {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (board[i][j] == 2048) {
                    return true;
                }
            }
        }
        return false;
    }

    // 检查是否游戏结束
    public boolean isGameOver() {
        // 如果有空位，则游戏尚未结束
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (board[i][j] == 0) {
                    return false;
                }
            }
        }

        // 如果可以进行合并，则游戏尚未结束
        return !canMerge();
    }

    // 检查棋盘上是否有可以合并的数字
    private boolean canMerge() {
        // 检查每个数字的上下左右是否存在相同的数字
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                int current = board[i][j];
                if ((i > 0 && board[i - 1][j] == current) ||  // 上方
                        (i < 3 && board[i + 1][j] == current) ||  // 下方
                        (j > 0 && board[i][j - 1] == current) ||  // 左方
                        (j < 3 && board[i][j + 1] == current)) {  // 右方
                    return true;
                }
            }
        }
        return false;
    }
}
