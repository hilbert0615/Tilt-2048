package com.cs407.tilt_2048;

import android.content.Intent;
import android.os.Bundle;
import android.widget.GridLayout;
import android.widget.ImageButton;
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
    private static final long SWIPE_COOLDOWN = 500; // 滑动冷却时间，单位毫秒
    private long lastSwipeTime = 0; // 上一次滑动的时间
    private static final float TILT_THRESHOLD = 5.0f; // 初始倾斜触发阈值

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        // 初始化返回按钮
        ImageButton backButton = findViewById(R.id.button_back_to_main);
        backButton.setOnClickListener(v -> {
            // 返回主界面
            Intent intent = new Intent(GameActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        // 初始化传感器
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        // 初始化 UI
        gridLayout = findViewById(R.id.gridLayout);
        initGrid();
        mainGame = new MainGame(this, gridTextViews);

        TextView tvBestScore = findViewById(R.id.tvBestScore);
        TextView tvScore = findViewById(R.id.tvScore);
        mainGame.updateBestScore(tvBestScore);
        mainGame.updateScore(tvScore);

        // 检查是否从 MainActivity 传递了“startNewGame”标记
        boolean startNewGame = getIntent().getBooleanExtra("startNewGame", true);
        if (startNewGame) {
            mainGame.startNewGame();
        } else {
            mainGame.restorePreviousState(); // 恢复游戏状态
        }

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

        findViewById(R.id.btnNewGame).setOnClickListener(v -> mainGame.startNewGame());
        findViewById(R.id.btnUndo).setOnClickListener(v -> mainGame.restorePreviousState());

        gestureDetector = new GestureDetector(this, new GestureListener());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isGyroscopeMode) {
            return gestureDetector.onTouchEvent(event);
        }
        return super.onTouchEvent(event);
    }

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

    // 传感器监听器
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
                            mainGame.onSwipeLeft();
                        } else if (x < -TILT_THRESHOLD) {
                            mainGame.onSwipeRight();
                        }
                    } else {
                        if (y > TILT_THRESHOLD) {
                            mainGame.onSwipeUp();
                        } else if (y < -TILT_THRESHOLD) {
                            mainGame.onSwipeDown();
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
