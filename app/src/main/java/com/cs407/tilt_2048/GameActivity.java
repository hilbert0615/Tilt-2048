package com.cs407.tilt_2048;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Switch;
import androidx.appcompat.app.AppCompatActivity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.GestureDetector;
import android.view.MotionEvent;

public class GameActivity extends AppCompatActivity {

    private MainGame mainGame;
    private TextView[][] gridTextViews = new TextView[4][4];
    private GridLayout gridLayout;
    private GestureDetector gestureDetector;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private boolean isGyroscopeMode = false;

    private long lastSwipeTime = 0; // 上一次滑动的时间
    private long lastTimestamp = 0L; // 上一次事件的时间戳

    private static final float TILT_THRESHOLD = 6.0f; // 倾斜触发阈值，适当提高
    private static final long SWIPE_COOLDOWN = 500;  // 滑动冷却时间，单位毫秒

//    private static final String PREFS_NAME = "GamePrefs";
    private String currentUserPrefsName;

    private TextView tvScore;
    private TextView tvBestScore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        // 初始化 UI
        gridLayout = findViewById(R.id.gridLayout);
        initGrid();

        tvScore = findViewById(R.id.tvScore);
        tvBestScore = findViewById(R.id.tvBestScore);

        currentUserPrefsName = getCurrentUserName();

        mainGame = new MainGame(this, gridTextViews, tvScore, currentUserPrefsName);

        boolean startNewGame = getIntent().getBooleanExtra("startNewGame", true);
        if (startNewGame) {
            mainGame.startNewGame();
        } else {
            restoreGameState();
        }

        // 设置分数显示
        TextView tvBestScore = findViewById(R.id.tvBestScore);
//        TextView tvScore = findViewById(R.id.tvScore);
        mainGame.updateBestScore(tvBestScore);
        mainGame.updateScore(tvScore);

        // 初始化返回按钮
        ImageView backButton = findViewById(R.id.ic_arrow_back);
        backButton.setOnClickListener(v -> {
            // 返回主界面
            Intent intent = new Intent(GameActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        // 初始化传感器
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
//        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);


        // 开关控件切换陀螺仪模式
        Switch switchControlMode = findViewById(R.id.switchControlMode);
        switchControlMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isGyroscopeMode = isChecked;
            switchControlMode.setChecked(isGyroscopeMode);
            switchControlMode.setText(isGyroscopeMode ? "Gyroscope Enabled" : "Enable Gyroscope");

            if (isGyroscopeMode) {
                startGyroscopeControl();
            } else {
                stopGyroscopeControl();
            }
        });

        // 新游戏和撤销按钮的点击事件
        findViewById(R.id.btnNewGame).setOnClickListener(v -> mainGame.startNewGame());
        findViewById(R.id.btnUndo).setOnClickListener(v -> mainGame.restorePreviousState());

        // 初始化手势检测
        gestureDetector = new GestureDetector(this, new GestureListener());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isGyroscopeMode) {
            return gestureDetector.onTouchEvent(event);
        }
        return super.onTouchEvent(event);
    }

    // 初始化游戏网格
    private void initGrid() {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                TextView cell = (TextView) gridLayout.getChildAt(i * 4 + j);
                gridTextViews[i][j] = cell;
            }
        }
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
                            mainGame.onSwipeRight();
                        } else {
                            mainGame.onSwipeLeft();
                        }
                        return true;
                    }
                } else {
                    if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffY > 0) {
                            mainGame.onSwipeDown();
                        } else {
                            mainGame.onSwipeUp();
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

    private SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (isGyroscopeMode) {
                // 检查传感器是否初始化，防止 null 或未注册的异常
                if (event.sensor.getType() != Sensor.TYPE_GRAVITY) return;

                float gravityX = event.values[0];
                float gravityY = event.values[1];

                // 时间差计算，防止频繁触发
                long currentTime = System.currentTimeMillis();
                if (lastTimestamp != 0L) {
                    if (currentTime - lastTimestamp < SWIPE_COOLDOWN) {
                        return; // 时间间隔小于冷却时间，跳过
                    }
                }
                lastTimestamp = currentTime;

                // 根据重力方向和阈值触发滑动操作
                if (Math.abs(gravityX) > TILT_THRESHOLD || Math.abs(gravityY) > TILT_THRESHOLD) {
                    if (Math.abs(gravityX) > Math.abs(gravityY)) {
                        // 水平方向
                        if (gravityX > TILT_THRESHOLD) {
                            mainGame.onSwipeLeft();
                        } else if (gravityX < -TILT_THRESHOLD) {
                            mainGame.onSwipeRight();
                        }
                    } else {
                        // 垂直方向
                        if (gravityY > TILT_THRESHOLD) {
                            mainGame.onSwipeUp();
                        } else if (gravityY < -TILT_THRESHOLD) {
                            mainGame.onSwipeDown();
                        }
                    }
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // 不需要处理
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
        lastTimestamp = 0L; // 重置时间戳
        if (isGyroscopeMode) {
            startGyroscopeControl();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        lastTimestamp = 0L; // 重置时间戳
        saveGameState();
        stopGyroscopeControl();
    }


//    // 保存游戏状态
//    private void saveGameState() {
//        SharedPreferences prefs = getSharedPreferences(currentUserPrefsName, MODE_PRIVATE);
//        SharedPreferences.Editor editor = prefs.edit();
//
//        editor.putInt("score", mainGame.getScore());
//        editor.putInt("best_score", mainGame.getBestScore());
//
//        for (int i = 0; i < 4; i++) {
//            for (int j = 0; j < 4; j++) {
//                editor.putInt("grid_" + i + "_" + j, mainGame.getGridValue(i, j));
//            }
//        }
//        editor.apply();
//    }

    private void saveGameState() {
        SharedPreferences prefs = getSharedPreferences("UserRecords", MODE_PRIVATE);

        // 保存当前游戏状态
        SharedPreferences gamePrefs = getSharedPreferences(currentUserPrefsName, MODE_PRIVATE);
        SharedPreferences.Editor editor = gamePrefs.edit();
        editor.putInt("score", mainGame.getScore());
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                editor.putInt("grid_" + i + "_" + j, mainGame.getGridValue(i, j));
            }
        }
        editor.apply();

        // 更新最高分和分数记录
        int score = mainGame.getScore();
        RankActivity.saveUserScore(getCurrentUserName(), score, prefs);
    }

    private String getCurrentUserName() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        return prefs.getString("current_user", "default_user");
    }

    // 恢复游戏状态
    private void restoreGameState() {
        SharedPreferences prefs = getSharedPreferences(currentUserPrefsName, MODE_PRIVATE);

        int score = prefs.getInt("score", 0);
        int bestScore = prefs.getInt("best_score", 0);

        // 从 Record 中获取最高分
        SharedPreferences recordsPrefs = getSharedPreferences("UserRecords", MODE_PRIVATE);
        String username = getCurrentUserName();
        int newBestScore = getHighestScoreFromRecords(username, recordsPrefs);
        mainGame.setBestScore(newBestScore);

        int[][] grid = new int[4][4];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                grid[i][j] = prefs.getInt("grid_" + i + "_" + j, 0);
            }
        }

        mainGame.setGrid(grid);
        mainGame.updateGridUI();

        mainGame.updateScore(tvScore);
        mainGame.updateBestScore(tvBestScore);
    }

    private int getHighestScoreFromRecords(String username, SharedPreferences prefs) {
        int highestScore = 0;
        for (int i = 1; i <= 20; i++) {
            int score = prefs.getInt(username + "_score_" + i, 0);
            if (score > highestScore) {
                highestScore = score;
            }
        }
        return highestScore;
    }

    private boolean isGravitySensorEvent(SensorEvent event) {
        return event.sensor != null && event.sensor.getType() == Sensor.TYPE_GRAVITY;
    }

}
