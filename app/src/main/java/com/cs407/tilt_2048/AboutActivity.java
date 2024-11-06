package com.cs407.tilt_2048;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        // 返回按钮的点击事件
        ImageView backButton = findViewById(R.id.ic_arrow_back);
        backButton.setOnClickListener(v -> {
            // 返回主界面
            Intent intent = new Intent(AboutActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
