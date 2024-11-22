package com.cs407.tilt_2048;

import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import android.widget.Toast;

import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private TextView welcomeText;
    private Button logoutButton;
    private ImageView iconUser;
    private String username;
    private UserDao userDao;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        welcomeText = findViewById(R.id.welcomeText);
        logoutButton = findViewById(R.id.logoutButton);
        iconUser = findViewById(R.id.icon_user);
        iconUser.setOnClickListener(v -> showChangeUsernameDialog());
        AppDatabase database = AppDatabase.getInstance(this);
        userDao = database.userDao();

        // Retrieve the username from the Intent
        username = getIntent().getStringExtra("username");
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
    private void showChangeUsernameDialog() {
        // Create an AlertDialog with an EditText for the new username
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Change Username");

        // Use the custom layout
        final View customView = getLayoutInflater().inflate(R.layout.dialog_change_username, null);
        builder.setView(customView);

        // Access the EditText inside the custom layout
        final EditText input = customView.findViewById(R.id.newUsernameInput);

        builder.setPositiveButton("Change", (dialog, which) -> {
            String newUsername = input.getText().toString().trim();

            if (TextUtils.isEmpty(newUsername)) {
                Toast.makeText(this, "Username cannot be empty", Toast.LENGTH_SHORT).show();
            } else if (newUsername.equals(username)) {
                Toast.makeText(this, "Username is already set to " + username, Toast.LENGTH_SHORT).show();
            } else {
                // Update username in the database
                Executors.newSingleThreadExecutor().execute(() -> {
                    User user = userDao.getUserByUsername(username);
                    if (user != null) {
                        user.setUsername(newUsername);
                        userDao.updateUser(user);

                        runOnUiThread(() -> {
                            username = newUsername;
                            welcomeText.setText("Welcome, " + username + "!");
                            Toast.makeText(this, "Username updated successfully", Toast.LENGTH_SHORT).show();
                        });
                    } else {
                        runOnUiThread(() -> Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show());
                    }
                });
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

}
