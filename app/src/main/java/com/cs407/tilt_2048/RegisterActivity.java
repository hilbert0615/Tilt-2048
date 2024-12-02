package com.cs407.tilt_2048;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.Executors;

public class RegisterActivity extends AppCompatActivity {
    private EditText usernameInput, passwordInput, confirmPasswordInput;
    private Button registerSubmitButton;
    private UserDao userDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize views
        usernameInput = findViewById(R.id.registerUsernameInput);
        passwordInput = findViewById(R.id.registerPasswordInput);
        confirmPasswordInput = findViewById(R.id.registerConfirmPasswordInput);
        registerSubmitButton = findViewById(R.id.registerSubmitButton);

        // Initialize database
        AppDatabase database = AppDatabase.getInstance(this);
        userDao = database.userDao();

        // Set register button click listener
        registerSubmitButton.setOnClickListener(v -> {
            String username = usernameInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();
            String confirmPassword = confirmPasswordInput.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            } else if (!password.equals(confirmPassword)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            } else {
                // Register user in a background thread
                Executors.newSingleThreadExecutor().execute(() -> {
                    User existingUser = userDao.getUserByUsername(username);
                    if (existingUser != null) {
                        // User already exists
                        runOnUiThread(() -> Toast.makeText(this, "Username already exists", Toast.LENGTH_SHORT).show());
                    } else {
                        // Create and save new user
                        User newUser = new User();
                        newUser.setUsername(username);
                        newUser.setPassword(password);
                        userDao.addUser(newUser);

                        runOnUiThread(() -> {
                            Toast.makeText(this, "Registration successful! Logging in...", Toast.LENGTH_SHORT).show();

                            // Automatically log in the user and navigate to MainActivity
                            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                            intent.putExtra("username", username); // Pass the username to MainActivity
                            startActivity(intent);
                            finish();
                        });
                    }
                });
            }
        });
    }
}