package com.cs407.tilt_2048;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import java.util.ArrayList;
import java.util.Random;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class MainGame {

    private final Context context;
    private final Random random = new Random();
    private final int[][] grid = new int[4][4];
    private final TextView[][] gridTextViews;
    private int score = 0;
    private int bestScore = 0;
    private final TextView tvScore;

    private final int[][] previousGrid = new int[4][4];
    private int previousScore = 0;

    private static final String PREFS_NAME = "GamePrefs";
    private static final String KEY_BEST_SCORE = "BestScore";

    public MainGame(Context context, TextView[][] gridTextViews, TextView tvScore) {
        this.context = context;
        this.gridTextViews = gridTextViews;
        this.tvScore = tvScore;
        loadBestScore();
    }

    public void startNewGame() {
        if (score > bestScore) {
            bestScore = score;
            saveBestScore();
        }
        score = 0;
        resetGrid();
        addRandomNumber();
        addRandomNumber();
        updateGridUI();
        backupGameState();
        updateScore(tvScore);
    }

    public void saveBestScore() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_BEST_SCORE, bestScore);
        editor.apply();
    }

    public void loadBestScore() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        bestScore = prefs.getInt(KEY_BEST_SCORE, 0);
    }

    public void updateGridUI() {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                int value = grid[i][j];
                TextView cell = gridTextViews[i][j];
                if (value == 0) {
                    cell.setText("");
                    cell.setBackground(getBackgroundDrawableForValue(value));
                } else {
                    cell.setText(String.valueOf(value));
                    cell.setBackground(getBackgroundDrawableForValue(value));
                    cell.setTextSize(34);
                    cell.setTypeface(null, android.graphics.Typeface.BOLD);
                    cell.setTextColor(value >= 8 ? Color.WHITE : Color.parseColor("#776E65"));
                }
            }
        }
    }

    public void updateScore(TextView tvScore) {
        tvScore.setText("Score: " + score);
    }

    public void updateBestScore(TextView tvBestScore) {
        tvBestScore.setText("Best: " + bestScore);
    }

    // 获取当前分数
    public int getScore() {
        return score;
    }

    // 设置分数
    public void setScore(int score) {
        this.score = score;
    }

    // 获取网格数据
    public int getGridValue(int row, int col) {
        return grid[row][col];
    }

    // 设置网格数据
    public void setGrid(int[][] grid) {
        for (int i = 0; i < 4; i++) {
            System.arraycopy(grid[i], 0, this.grid[i], 0, 4);
        }
    }

    private void resetGrid() {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                grid[i][j] = 0;
            }
        }
    }

    public void addRandomNumber() {
        int row, col;
        do {
            row = random.nextInt(4);
            col = random.nextInt(4);
        } while (grid[row][col] != 0);
        grid[row][col] = random.nextInt(10) < 9 ? 2 : 4;
    }

    public Drawable getBackgroundDrawableForValue(int value) {
        switch (value) {
            case 2:
                return ContextCompat.getDrawable(context, R.drawable.cell_background_2);
            case 4:
                return ContextCompat.getDrawable(context, R.drawable.cell_background_4);
            case 8:
                return ContextCompat.getDrawable(context, R.drawable.cell_background_8);
            case 16:
                return ContextCompat.getDrawable(context, R.drawable.cell_background_16);
            case 32:
                return ContextCompat.getDrawable(context, R.drawable.cell_background_32);
            case 64:
                return ContextCompat.getDrawable(context, R.drawable.cell_background_64);
            case 128:
                return ContextCompat.getDrawable(context, R.drawable.cell_background_128);
            case 256:
                return ContextCompat.getDrawable(context, R.drawable.cell_background_256);
            case 512:
                return ContextCompat.getDrawable(context, R.drawable.cell_background_512);
            case 1024:
                return ContextCompat.getDrawable(context, R.drawable.cell_background_1024);
            case 2048:
                return ContextCompat.getDrawable(context, R.drawable.cell_background_2048);
            default:
                return ContextCompat.getDrawable(context, R.drawable.cell_background_empty);
        }
    }

    public void backupGameState() {
        for (int i = 0; i < 4; i++) {
            System.arraycopy(grid[i], 0, previousGrid[i], 0, 4);
        }
        previousScore = score;
    }

    public void restorePreviousState() {
        boolean hasNonZero = false;
        for (int i = 0; i < 4 && !hasNonZero; i++) {
            for (int j = 0; j < 4; j++) {
                if (previousGrid[i][j] != 0) {
                    hasNonZero = true; // 检查备份状态是否存在有效数字
                    break;
                }
            }
        }

        if (!hasNonZero) {
            return; // 如果备份状态为空，则不进行恢复
        }

        for (int i = 0; i < 4; i++) {
            System.arraycopy(previousGrid[i], 0, grid[i], 0, 4);
        }
        score = previousScore;
        updateGridUI();
        updateScore(tvScore);
    }

    public void animateGridChange(String direction, ArrayList<int[]> changes) {
        ArrayList<Animator> animations = new ArrayList<>();
        float translation = 50f;

        for (int[] change : changes) {
            int row = change[0];
            int col = change[1];
            String action = change[2] == 1 ? "move" : "merge";

            TextView cell = gridTextViews[row][col];

            if ("move".equals(action)) {
                ObjectAnimator animator;
                switch (direction) {
                    case "LEFT":
                        animator = ObjectAnimator.ofFloat(cell, "translationX", -translation, 0f);
                        break;
                    case "RIGHT":
                        animator = ObjectAnimator.ofFloat(cell, "translationX", translation, 0f);
                        break;
                    case "UP":
                        animator = ObjectAnimator.ofFloat(cell, "translationY", -translation, 0f);
                        break;
                    case "DOWN":
                        animator = ObjectAnimator.ofFloat(cell, "translationY", translation, 0f);
                        break;
                    default:
                        animator = ObjectAnimator.ofFloat(cell, "translationX", 0f, 0f);
                }
                animator.setDuration(200);
                animations.add(animator);
            }

            if ("merge".equals(action)) {
                ObjectAnimator scaleX = ObjectAnimator.ofFloat(cell, "scaleX", 1f, 1.2f, 1f);
                ObjectAnimator scaleY = ObjectAnimator.ofFloat(cell, "scaleY", 1f, 1.2f, 1f);
                scaleX.setDuration(150);
                scaleY.setDuration(150);
                animations.add(scaleX);
                animations.add(scaleY);
            }
        }

        AnimatorSet finalSet = new AnimatorSet();
        finalSet.playTogether(animations);
        finalSet.start();
    }

    // 滑动逻辑
    public void onSwipeLeft() {
        backupGameState();

        ArrayList<int[]> changes = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            slideAndMergeRow(grid[i], changes, i);
        }
        addRandomNumber();
        updateGridUI();
        animateGridChange("LEFT", changes);

        updateScore(tvScore);

        if (isGameOver()) {
            showGameOverDialog();
        }
    }

    public void onSwipeRight() {
        backupGameState();

        ArrayList<int[]> changes = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            reverseArray(grid[i]);
            slideAndMergeRow(grid[i], changes, i);
            reverseArray(grid[i]);
        }
        addRandomNumber();
        updateGridUI();
        animateGridChange("RIGHT", changes);

        updateScore(tvScore);

        if (isGameOver()) {
            showGameOverDialog();
        }
    }

    public void onSwipeUp() {
        backupGameState();

        ArrayList<int[]> changes = new ArrayList<>();
        for (int j = 0; j < 4; j++) {
            int[] column = new int[4];
            for (int i = 0; i < 4; i++) column[i] = grid[i][j];
            slideAndMergeRow(column, changes, j);
            for (int i = 0; i < 4; i++) grid[i][j] = column[i];
        }
        addRandomNumber();
        updateGridUI();
        animateGridChange("UP", changes);

        updateScore(tvScore);

        if (isGameOver()) {
            showGameOverDialog();
        }
    }

    public void onSwipeDown() {
        backupGameState();

        ArrayList<int[]> changes = new ArrayList<>();
        for (int j = 0; j < 4; j++) {
            int[] column = new int[4];
            for (int i = 0; i < 4; i++) column[i] = grid[i][j];
            reverseArray(column);
            slideAndMergeRow(column, changes, j);
            reverseArray(column);
            for (int i = 0; i < 4; i++) grid[i][j] = column[i];
        }
        addRandomNumber();
        updateGridUI();
        animateGridChange("DOWN", changes);

        updateScore(tvScore);

        if (isGameOver()) {
            showGameOverDialog();
        }
    }

    private void slideAndMergeRow(int[] row, ArrayList<int[]> changes, int rowIndex) {
        int[] newRow = new int[4];
        int index = 0;

        for (int i = 0; i < 4; i++) {
            if (row[i] != 0) {
                newRow[index++] = row[i];
            }
        }

        for (int i = 0; i < 3; i++) {
            if (newRow[i] != 0 && newRow[i] == newRow[i + 1]) {
                newRow[i] *= 2;
                score += newRow[i];
                newRow[i + 1] = 0;
                changes.add(new int[]{rowIndex, i, 0}); // 合并
            }
        }

        index = 0;
        for (int i = 0; i < 4; i++) {
            if (newRow[i] != 0) {
                row[index++] = newRow[i];
                if (newRow[i] != row[i]) {
                    changes.add(new int[]{rowIndex, i, 1});
                }
            } else {
                row[index++] = 0;
            }
        }
    }

    private void reverseArray(int[] array) {
        for (int i = 0; i < array.length / 2; i++) {
            int temp = array[i];
            array[i] = array[array.length - 1 - i];
            array[array.length - 1 - i] = temp;
        }
    }

    private boolean isGameOver() {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (grid[i][j] == 0) return false;
            }
        }

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if ((i > 0 && grid[i][j] == grid[i - 1][j]) ||
                        (i < 3 && grid[i][j] == grid[i + 1][j]) ||
                        (j > 0 && grid[i][j] == grid[i][j - 1]) ||
                        (j < 3 && grid[i][j] == grid[i][j + 1])) {
                    return false;
                }
            }
        }
        return true;
    }

    private void showGameOverDialog() {
        // 保存分数逻辑
        SharedPreferences userPrefs = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        SharedPreferences recordPrefs = context.getSharedPreferences("UserRecords", Context.MODE_PRIVATE);
        String currentUser = userPrefs.getString("current_user", null);

        if (currentUser != null) {
            RankActivity.saveUserScore(currentUser, score, recordPrefs);
        }

        // 显示游戏结束对话框
        new AlertDialog.Builder(context)
                .setTitle("Game Over")
                .setMessage("No more moves available!")
                .setPositiveButton("New Game", (dialog, which) -> startNewGame())
                .setNegativeButton("Exit", (dialog, which) -> {
                    // 返回主界面
                    if (context instanceof AppCompatActivity) {
                        ((AppCompatActivity) context).finish();
                    }
                })
                .setCancelable(false)
                .show();
    }

}
