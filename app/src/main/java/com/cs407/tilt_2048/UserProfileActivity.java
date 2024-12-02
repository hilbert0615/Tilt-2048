package com.cs407.tilt_2048;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class UserProfileActivity extends AppCompatActivity {

    private ImageView userPhoto;
    private EditText descriptionInput;
    private EditText birthdayInput;
    private TextView usernameView;

    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        // 初始化视图
        userPhoto = findViewById(R.id.user_photo);
        descriptionInput = findViewById(R.id.et_description);
        birthdayInput = findViewById(R.id.et_birthday);
        usernameView = findViewById(R.id.tv_username);

        // 检查登录状态
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        username = prefs.getString("current_user", null);

        if (username == null || username.isEmpty()) {
            showLoginDialog();
        } else {
            // 加载用户数据
            loadUserData(username);
            loadUserPhoto();

            // 返回按钮
            findViewById(R.id.btn_back).setOnClickListener(v -> finish());

            // 头像点击事件
            userPhoto.setOnClickListener(v -> showPhotoOptions());

            // Logout 按钮
            findViewById(R.id.btn_logout).setOnClickListener(v -> showLogoutConfirmation());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveUserData(username);
    }

    // 显示登录/注册弹窗
    private void showLoginDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_user, null);

        EditText usernameInput = dialogView.findViewById(R.id.et_username);
        EditText passwordInput = dialogView.findViewById(R.id.et_password);
        View loginButton = dialogView.findViewById(R.id.btn_login);
        View registerButton = dialogView.findViewById(R.id.btn_register);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create();

        // 登录逻辑
        loginButton.setOnClickListener(v -> {
            String inputUsername = usernameInput.getText().toString();
            String inputPassword = passwordInput.getText().toString();

            SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            String savedPassword = prefs.getString(inputUsername + "_password", null);

            if (savedPassword != null && savedPassword.equals(inputPassword)) {
                Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();
                prefs.edit()
                        .putString("current_user", inputUsername)
                        .apply();
                username = inputUsername;
                dialog.dismiss();
                recreate();
            } else {
                Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show();
            }
        });

        // 注册逻辑
        registerButton.setOnClickListener(v -> {
            String inputUsername = usernameInput.getText().toString();
            String inputPassword = passwordInput.getText().toString();

            if (inputUsername.isEmpty() || inputPassword.isEmpty()) {
                Toast.makeText(this, "Username and password cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            if (prefs.contains(inputUsername + "_password")) {
                Toast.makeText(this, "Username already exists", Toast.LENGTH_SHORT).show();
                return;
            }

            prefs.edit()
                    .putString(inputUsername + "_password", inputPassword)
                    .apply();

            Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show();
        });

        dialog.show();
    }

    private void saveUserData(String username) {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String description = descriptionInput.getText().toString();
        String birthday = birthdayInput.getText().toString();

        prefs.edit()
                .putString(username + "_description", description)
                .putString(username + "_birthday", birthday)
                .apply();

        Toast.makeText(this, "Data saved!", Toast.LENGTH_SHORT).show();
    }

    private void loadUserData(String username) {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);

        String description = prefs.getString(username + "_description", "");
        String birthday = prefs.getString(username + "_birthday", "");

        descriptionInput.setText(description);
        birthdayInput.setText(birthday);
        usernameView.setText(username);
    }

    private void loadUserPhoto() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String photoPath = prefs.getString(username + "_photo", null);

        if (photoPath != null) {
            File file = new File(photoPath);
            if (file.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(photoPath);
                userPhoto.setImageBitmap(bitmap);
            } else {
                userPhoto.setImageResource(R.drawable.ic_user);
            }
        } else {
            userPhoto.setImageResource(R.drawable.ic_user);
        }
    }

    private void showPhotoOptions() {
        String[] options = {"Take a Photo", "Choose from Gallery"};
        new AlertDialog.Builder(this)
                .setTitle("Update Profile Photo")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        openCamera();
                    } else if (which == 1) {
                        openGallery();
                    }
                })
                .show();
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, 100);
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, 101);
    }

    private void showLogoutConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                    prefs.edit()
                            .putBoolean("isLoggedIn", false)
                            .putString("current_user", null)
                            .apply();

                    Toast.makeText(this, "Logout successful!", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(UserProfileActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("No", null)
                .show();
    }
}
