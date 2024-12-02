package com.cs407.tilt_2048;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class UserProfileActivity extends AppCompatActivity {

    private static final int REQUEST_LOCATION_PERMISSION = 1001;

    private ImageView userPhoto;
    private EditText descriptionInput;
    private TextView usernameView;
    private TextView locationView;

    private String username;

    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        // 初始化视图
        userPhoto = findViewById(R.id.user_photo);
        descriptionInput = findViewById(R.id.et_description);
        usernameView = findViewById(R.id.tv_username);
        locationView = findViewById(R.id.et_location);

        // 初始化位置服务客户端
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // 检查登录状态
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        username = prefs.getString("current_user", null);

        if (username == null || username.isEmpty()) {
            // 用户未登录，提示登录
            promptLogin();
            return; // 中断后续加载流程
        }

        // 用户已登录，加载数据
        loadUserData(username);
        loadUserPhoto();

        // 设置返回按钮点击事件
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // 设置头像点击事件
        userPhoto.setOnClickListener(v -> showPhotoOptions());

        // 设置注销按钮点击事件
        findViewById(R.id.btn_logout).setOnClickListener(v -> showLogoutConfirmation());

        // 检查并请求位置权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        } else {
            // 权限已授予，获取实时位置信息
            fetchRealTimeLocation();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveUserData(username);
    }

    // 实时获取用户位置信息
    private void fetchRealTimeLocation() {
        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10000) // 每 10 秒更新
                .setFastestInterval(5000); // 最快 5 秒更新一次

        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }

                // 获取最新位置
                if (!locationResult.getLocations().isEmpty()) {
                    double latitude = locationResult.getLastLocation().getLatitude();
                    double longitude = locationResult.getLastLocation().getLongitude();
                    updateLocationUI(latitude, longitude);
                }
            }
        };

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, getMainLooper());
    }

    private void updateLocationUI(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                String city = address.getLocality();
                String province = address.getAdminArea();
                String country = address.getCountryName();

                // 更新 TextView
                String location = String.format("%s, %s, %s", city, province, country);
                locationView.setText(location);

                // 保存位置信息到 SharedPreferences
                SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                prefs.edit()
                        .putString("user_location", location)
                        .apply();
            } else {
                locationView.setText("Unknown Location");
            }
        } catch (IOException e) {
            e.printStackTrace();
            locationView.setText("Error retrieving location");
        }
    }

    private void saveUserData(String username) {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String description = descriptionInput.getText().toString();

        prefs.edit()
                .putString(username + "_description", description)
                .apply();
    }

    private void loadUserData(String username) {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String description = prefs.getString(username + "_description", "");
        descriptionInput.setText(description);
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

                    Intent intent = new Intent(UserProfileActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void promptLogin() {
        new AlertDialog.Builder(this)
                .setTitle("Login Required")
                .setMessage("Please log in to access your profile.")
                .setPositiveButton("Log In", (dialog, which) -> {
                    Intent intent = new Intent(this, MainActivity.class);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", (dialog, which) -> finish())
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchRealTimeLocation();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
