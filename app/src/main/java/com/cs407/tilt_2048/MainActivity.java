package com.cs407.tilt_2048;

import android.widget.Switch;
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
import android.content.SharedPreferences;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

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

    private int[][] previousGrid = new int[4][4];
    private int previousScore = 0;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private boolean isGyroscopeMode = false;

    private static final long SWIPE_COOLDOWN = 500; // 滑动冷却时间，单位毫秒
    private long lastSwipeTime = 0; // 上一次滑动的时间
    private static final float TILT_THRESHOLD = 5.0f; // 初始倾斜触发阈值


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 初始化传感器
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        // 加载最高分
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        bestScore = prefs.getInt(KEY_BEST_SCORE, 0);

        // 初始化 UI
        gridLayout = findViewById(R.id.gridLayout);
        initGrid();
        updateBestScore();

        startNewGame();

        Switch switchControlMode = findViewById(R.id.switchControlMode);
        switchControlMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isGyroscopeMode = isChecked;
            switchControlMode.setText(isGyroscopeMode ? "Gyroscope Enabled" : "Enable Gyroscope");

            if (isGyroscopeMode) {
                startGyroscopeControl();
            } else {
                stopGyroscopeControl();
            }
        });

        findViewById(R.id.btnNewGame).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startNewGame();
            }
        });

        findViewById(R.id.btnUndo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                restorePreviousState();
            }
        });

        gestureDetector = new GestureDetector(this, new GestureListener());
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isGyroscopeMode) {  // 仅在非陀螺仪模式下启用手势
            return gestureDetector.onTouchEvent(event);
        }
        return super.onTouchEvent(event);
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
            saveBestScore();
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

    // 添加一个随机数字
    private void addRandomNumber() {
        int row, col;
        do {
            row = random.nextInt(4);
            col = random.nextInt(4);
        } while (grid[row][col] != 0);
        grid[row][col] = random.nextInt(10) < 9 ? 2 : 4;
    }

    // 更新 UI
    private void updateGridUI() {
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
        updateScore();
    }

    // 更新分数
    private void updateScore() {
        TextView tvScore = findViewById(R.id.tvScore);
        tvScore.setText("Score: " + score);
    }

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
                return ContextCompat.getDrawable(this, R.drawable.cell_background_empty);
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

            // 滑动
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

            // 合并
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
        backupGameState();

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

        if (isGameOver()) {
            showGameOverDialog();
        }
    }

    private void onSwipeUp() {
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

        if (isGameOver()) {
            showGameOverDialog();
        }
    }

    private void onSwipeDown() {
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

    private void backupGameState() {
        for (int i = 0; i < 4; i++) {
            System.arraycopy(grid[i], 0, previousGrid[i], 0, 4);
        }
        previousScore = score;
    }

    private void restorePreviousState() {
        for (int i = 0; i < 4; i++) {
            System.arraycopy(previousGrid[i], 0, grid[i], 0, 4);
        }
        score = previousScore;
        updateGridUI();
        updateScore();
    }

    private SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (isGyroscopeMode) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastSwipeTime < SWIPE_COOLDOWN) {
                    return;
                }

                float x = event.values[0];
                float y = event.values[1];

                // 设定一个触发倾斜阈值
                if (Math.abs(x) > TILT_THRESHOLD || Math.abs(y) > TILT_THRESHOLD) {
                    if (Math.abs(x) > Math.abs(y)) {
                        if (x > TILT_THRESHOLD) {
                            onSwipeLeft();
                        } else if (x < -TILT_THRESHOLD) {
                            onSwipeRight();
                        }
                    } else {
                        if (y > TILT_THRESHOLD) {
                            onSwipeUp();
                        } else if (y < -TILT_THRESHOLD) {
                            onSwipeDown();
                        }
                    }
                    lastSwipeTime = currentTime;
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    private void startGyroscopeControl() {
        sensorManager.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_UI);
    }

    private void stopGyroscopeControl() {
        sensorManager.unregisterListener(sensorEventListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isGyroscopeMode) {
            startGyroscopeControl();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopGyroscopeControl();
    }

}
