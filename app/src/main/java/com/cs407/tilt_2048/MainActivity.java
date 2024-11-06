package com.cs407.tilt_2048;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.GridLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.Random;
import androidx.core.content.ContextCompat;
import android.graphics.Color;
import android.animation.Animator;



public class MainActivity extends AppCompatActivity {

    private int[][] grid = new int[4][4];
    private TextView[][] gridTextViews = new TextView[4][4];
    private GridLayout gridLayout;
    private Random random = new Random();
    private GestureDetector gestureDetector;
    private int score = 0;

    private static final String PREFS_NAME = "GamePrefs";
    private static final String KEY_BEST_SCORE = "BestScore";
    private int bestScore = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 加载最高分
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        bestScore = prefs.getInt(KEY_BEST_SCORE, 0);

        // 初始化 UI
        gridLayout = findViewById(R.id.gridLayout);
        initGrid();
        updateBestScore();

        startNewGame();

        findViewById(R.id.btnNewGame).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startNewGame();
            }
        });

        gestureDetector = new GestureDetector(this, new GestureListener());
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    // 手势监听器
    private class GestureListener extends GestureDetector.SimpleOnGestureListener {

        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            float diffX = e2.getX() - e1.getX();
            float diffY = e2.getY() - e1.getY();

            try {
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            onSwipeRight();
                        } else {
                            onSwipeLeft();
                        }
                        return true;
                    }
                } else {
                    if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffY > 0) {
                            onSwipeDown();
                        } else {
                            onSwipeUp();
                        }
                        return true;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }
    }

    // 初始化 TextViews 并绑定到 gridTextViews 数组
    private void initGrid() {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                TextView cell = (TextView) gridLayout.getChildAt(i * 4 + j);
                gridTextViews[i][j] = cell;
            }
        }
    }

    // 开始新游戏
    private void startNewGame() {
        if (score > bestScore) {
            bestScore = score;
            saveBestScore(); // 保存新的最高分
            updateBestScore();
        }
        score = 0;
        updateScore();
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                grid[i][j] = 0;
            }
        }
        addRandomNumber();
        addRandomNumber();
        updateGridUI();
    }

    private void saveBestScore() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_BEST_SCORE, bestScore);
        editor.apply();
    }

    private void updateBestScore() {
        TextView tvBestScore = findViewById(R.id.tvBestScore);
        tvBestScore.setText("Best: " + bestScore);
    }

    // 添加一个随机数字（2 或 4）
    private void addRandomNumber() {
        int row, col;
        do {
            row = random.nextInt(4);
            col = random.nextInt(4);
        } while (grid[row][col] != 0);
        grid[row][col] = random.nextInt(10) < 9 ? 2 : 4;
    }

    // 更新 UI 显示
    private void updateGridUI() {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                int value = grid[i][j];
                TextView cell = gridTextViews[i][j];
                if (value == 0) {
                    cell.setText("");
                    cell.setBackground(getBackgroundDrawableForValue(value)); // 使用空格子背景
                } else {
                    cell.setText(String.valueOf(value));
                    cell.setBackground(getBackgroundDrawableForValue(value)); // 使用对应数值的背景
                    cell.setTextSize(34); // 设置字体大小
                    cell.setTypeface(null, android.graphics.Typeface.BOLD); // 设置字体加粗
                    cell.setTextColor(value >= 8 ? Color.WHITE : Color.parseColor("#776E65")); // 高亮字体颜色
                }
            }
        }
        updateScore();
    }


    // 更新分数显示
    private void updateScore() {
        TextView tvScore = findViewById(R.id.tvScore);
        tvScore.setText("Score: " + score);
    }

    // 根据数值返回不同的背景 drawable
    private Drawable getBackgroundDrawableForValue(int value) {
        switch (value) {
            case 2:
                return ContextCompat.getDrawable(this, R.drawable.cell_background_2);
            case 4:
                return ContextCompat.getDrawable(this, R.drawable.cell_background_4);
            case 8:
                return ContextCompat.getDrawable(this, R.drawable.cell_background_8);
            case 16:
                return ContextCompat.getDrawable(this, R.drawable.cell_background_16);
            case 32:
                return ContextCompat.getDrawable(this, R.drawable.cell_background_32);
            case 64:
                return ContextCompat.getDrawable(this, R.drawable.cell_background_64);
            case 128:
                return ContextCompat.getDrawable(this, R.drawable.cell_background_128);
            case 256:
                return ContextCompat.getDrawable(this, R.drawable.cell_background_256);
            case 512:
                return ContextCompat.getDrawable(this, R.drawable.cell_background_512);
            case 1024:
                return ContextCompat.getDrawable(this, R.drawable.cell_background_1024);
            case 2048:
                return ContextCompat.getDrawable(this, R.drawable.cell_background_2048);
            default:
                return ContextCompat.getDrawable(this, R.drawable.cell_background_empty); // 空格子的背景
        }
    }

    private void animateGridChange(String direction, ArrayList<int[]> changes) {
        ArrayList<Animator> animations = new ArrayList<>();
        float translation = 50f;

        for (int[] change : changes) {
            int row = change[0];
            int col = change[1];
            String action = change[2] == 1 ? "move" : "merge";

            TextView cell = gridTextViews[row][col];

            // 滑动动画
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
                        animator = ObjectAnimator.ofFloat(cell, "translationX", 0f, 0f); // 默认不动
                }
                animator.setDuration(200);
                animations.add(animator);
            }

            // 合并动画
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

    private void onSwipeLeft() {
        ArrayList<int[]> changes = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            slideAndMergeRow(grid[i], changes, i);
        }
        addRandomNumber();
        updateGridUI();
        animateGridChange("LEFT", changes);

        if (isGameOver()) {
            showGameOverDialog();
        }
    }

    private void onSwipeRight() {
        ArrayList<int[]> changes = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            reverseArray(grid[i]);
            slideAndMergeRow(grid[i], changes, i);
            reverseArray(grid[i]);
        }
        addRandomNumber();
        updateGridUI();
        animateGridChange("RIGHT", changes);

        if (isGameOver()) {
            showGameOverDialog();
        }
    }

    private void onSwipeUp() {
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

        if (isGameOver()) {
            showGameOverDialog();
        }
    }

    private void onSwipeDown() {
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
                    changes.add(new int[]{rowIndex, i, 1}); // 移动
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

    // 判断游戏是否结束
    private boolean isGameOver() {
        // 检查是否有空格子
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (grid[i][j] == 0) {
                    return false; // 有空格子，游戏未结束
                }
            }
        }

        // 检查是否有相邻的相同格子
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if ((i > 0 && grid[i][j] == grid[i - 1][j]) || // 上方
                        (i < 3 && grid[i][j] == grid[i + 1][j]) || // 下方
                        (j > 0 && grid[i][j] == grid[i][j - 1]) || // 左方
                        (j < 3 && grid[i][j] == grid[i][j + 1])) { // 右方
                    return false; // 有可合并的相邻格子，游戏未结束
                }
            }
        }

        return true; // 无法移动或合并，游戏结束
    }


    private void showGameOverDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Game Over")
                .setMessage("No more moves available!")
                .setPositiveButton("New Game", (dialog, which) -> startNewGame())
                .setNegativeButton("Exit", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }
}
