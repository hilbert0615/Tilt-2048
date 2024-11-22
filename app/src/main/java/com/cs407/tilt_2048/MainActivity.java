package com.cs407.tilt_2048;

import android.widget.Button;
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
import android.app.AlertDialog;

public class MainActivity extends AppCompatActivity {

    private TextView welcomeText;
    private Button logoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        welcomeText = findViewById(R.id.welcomeText);
        logoutButton = findViewById(R.id.logoutButton);

        // Retrieve the username from the Intent
        String username = getIntent().getStringExtra("username");
        if (username != null) {
            // Set the welcome message with the username
            welcomeText.setText("Welcome, " + username + "!");
        }

        logoutButton.setOnClickListener(v -> {
            // Clear any session data if necessary (optional)

            // Navigate back to LoginActivity
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear back stack
            startActivity(intent);
            finish(); // Close MainActivity
        });

        // New Game
        findViewById(R.id.button_new_game).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, GameActivity.class);
            intent.putExtra("startNewGame", true);
            startActivity(intent);
        });

        // Resume
        findViewById(R.id.button_resume).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, GameActivity.class);
            intent.putExtra("startNewGame", false);
            startActivity(intent);
        });

        // About
        findViewById(R.id.button_about).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AboutActivity.class);
            startActivity(intent);
        });

        // Quit
        findViewById(R.id.button_quit).setOnClickListener(v -> {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Quit")
                    .setMessage("Are you sure you want to quit?")
                    .setPositiveButton("Yes", (dialog, which) -> finishAffinity())
                    .setNegativeButton("No", null)
                    .show();
        });
    }

}
