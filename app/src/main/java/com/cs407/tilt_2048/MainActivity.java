package com.cs407.tilt_2048;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.widget.TextView;
import android.provider.Settings;
import android.net.Uri;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_LOCATION_PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 设置 GPS 图标点击事件
        ImageView locationIcon = findViewById(R.id.icon_location);
        locationIcon.setOnClickListener(v -> handleLocationIconClick());

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

        // Record
        findViewById(R.id.button_rank).setOnClickListener(v -> {
            if (isUserLoggedIn()) {
                // 用户已登录，进入 RankActivity
                Intent intent = new Intent(MainActivity.this, RankActivity.class);
                startActivity(intent);
            } else {
                // 用户未登录，显示提示
                showLoginRequiredAlert();
            }
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

    private void handleLocationIconClick() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // 如果未授予权限，请求权限
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        } else {
            // 如果权限已授予，提示用户是否跳转到设置页面修改权限
            showSettingsDialog();
        }
    }

    private void showSettingsDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Modify Location Permission")
                .setMessage("Location permission is already granted. Do you want to open settings to modify permissions?")
                .setPositiveButton("Open Settings", (dialog, which) -> openAppSettings())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void openAppSettings() {
        // 跳转到应用的设置页面
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 用户授予权限
                Toast.makeText(this, "Location permission granted", Toast.LENGTH_SHORT).show();
            } else {
                // 用户拒绝权限
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean isUserLoggedIn() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String currentUser = prefs.getString("current_user", null);
        return currentUser != null;
    }

    private void showLoginRequiredAlert() {
        new AlertDialog.Builder(this)
                .setTitle("Login Required")
                .setMessage("You must log in to access your record.")
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }

}
