package com.cs407.tilt_2048;

import android.widget.Switch;
import android.content.Context;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.GridLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.content.Intent;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // “New Game” 按钮点击事件，启动 GameActivity 并开始新游戏
        findViewById(R.id.button_new_game).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, GameActivity.class);
            intent.putExtra("startNewGame", true); // 传递一个标记，指示开始新游戏
            startActivity(intent);
        });

        // “Resume” 按钮点击事件，启动 GameActivity 并恢复游戏
        findViewById(R.id.button_resume).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, GameActivity.class);
            intent.putExtra("startNewGame", false); // 传递一个标记，指示恢复游戏
            startActivity(intent);
        });
    }

}
