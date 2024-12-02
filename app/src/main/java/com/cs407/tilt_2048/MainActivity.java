package com.cs407.tilt_2048;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        // User Icon
        ImageView userIcon = findViewById(R.id.icon_user);

        // Handle User Icon Click
        userIcon.setOnClickListener(v -> handleUserIconClick());
    }

    private void handleUserIconClick() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean("isLoggedIn", false);

        if (isLoggedIn) {
            // If logged in, go to user profile
            Intent intent = new Intent(MainActivity.this, UserProfileActivity.class);
            startActivity(intent);
        } else {
            // If not logged in, show login/register dialog
            showUserDialog();
        }
    }

    private void showUserDialog() {
        // Inflate custom dialog layout
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_user, null);

        // Initialize dialog views
        EditText usernameInput = dialogView.findViewById(R.id.et_username);
        EditText passwordInput = dialogView.findViewById(R.id.et_password);
        View loginButton = dialogView.findViewById(R.id.btn_login);
        View registerButton = dialogView.findViewById(R.id.btn_register);
        View closeButton = dialogView.findViewById(R.id.btn_close);

        // Create AlertDialog
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create();

        // Handle login
        loginButton.setOnClickListener(v -> {
            String username = usernameInput.getText().toString();
            String password = passwordInput.getText().toString();

            // Retrieve credentials from SharedPreferences
            SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            String savedPassword = prefs.getString(username + "_password", null); // Fix key

            if (savedPassword != null && savedPassword.equals(password)) {
                Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();
                prefs.edit()
                        .putBoolean("isLoggedIn", true)
                        .putString("current_user", username) // Save logged-in user
                        .apply();

                dialog.dismiss();

                // Go to user profile
                Intent intent = new Intent(MainActivity.this, UserProfileActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show();
            }
        });

        // Handle registration
        registerButton.setOnClickListener(v -> {
            String username = usernameInput.getText().toString();
            String password = passwordInput.getText().toString();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter a username and password", Toast.LENGTH_SHORT).show();
                return;
            }

            SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);

            if (prefs.contains(username + "_password")) {
                Toast.makeText(this, "Username already exists", Toast.LENGTH_SHORT).show();
                return;
            }

            // Save username and password
            prefs.edit()
                    .putString(username + "_password", password)
                    .putBoolean("isLoggedIn", true)
                    .putString("current_user", username) // Save logged-in user
                    .apply();

            Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show();
            dialog.dismiss();

            // Go to user profile
            Intent intent = new Intent(MainActivity.this, UserProfileActivity.class);
            startActivity(intent);
        });

        // Close dialog
        closeButton.setOnClickListener(v -> dialog.dismiss());

        // Show dialog
        dialog.show();
    }
}
