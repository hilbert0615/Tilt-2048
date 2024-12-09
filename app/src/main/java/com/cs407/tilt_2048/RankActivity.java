package com.cs407.tilt_2048;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RankActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecordAdapter adapter;
    private List<Record> records;
    private String currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rank);

        // 检查用户是否已登录
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        currentUser = prefs.getString("current_user", null);
        if (currentUser == null) {
            promptLogin();
            return;
        }

        // 初始化返回按钮
        ImageView backButton = findViewById(R.id.ic_arrow_back);
        backButton.setOnClickListener(v -> {
            // 返回主界面
            Intent intent = new Intent(RankActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        // 初始化 RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this)); // 垂直布局
        recyclerView.setHasFixedSize(true);

        // 加载用户分数记录
        records = loadUserRecords(currentUser);

        // 设置适配器
        adapter = new RecordAdapter(this, records);
        recyclerView.setAdapter(adapter);
    }

    // 提示用户登录
    private void promptLogin() {
        Toast.makeText(this, "Please log in to view records.", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, UserProfileActivity.class);
        startActivity(intent);
        finish(); // 关闭当前 Activity
    }

    // 加载用户的分数记录
    private List<Record> loadUserRecords(String username) {
        SharedPreferences prefs = getSharedPreferences("UserRecords", MODE_PRIVATE);
        List<Record> records = new ArrayList<>();

        // 从 SharedPreferences 中加载分数记录
        for (int i = 1; i <= 20; i++) {
            int score = prefs.getInt(username + "_score_" + i, 0);
            records.add(new Record(i, score));
        }

        // 按分数从高到低排序
        Collections.sort(records, (r1, r2) -> Integer.compare(r2.getScore(), r1.getScore()));

        return records;
    }

    // 保存用户分数
    public static void saveUserScore(String username, int newScore, SharedPreferences prefs) {
        SharedPreferences.Editor editor = prefs.edit();

        // 加载已有记录
        List<Integer> scores = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            int score = prefs.getInt(username + "_score_" + i, 0);
            if (score > 0) {
                scores.add(score);
            }
        }

        // 如果分数已经存在，则不重复保存
        if (scores.contains(newScore)) {
            return;
        }

        // 添加新分数
        scores.add(newScore);

        // 按分数从高到低排序，并保留前 20 条
        Collections.sort(scores, Collections.reverseOrder());
        while (scores.size() > 20) {
            scores.remove(scores.size() - 1);
        }

        // 保存回 SharedPreferences
        for (int i = 0; i < scores.size(); i++) {
            editor.putInt(username + "_score_" + (i + 1), scores.get(i));
        }
        editor.apply();
    }
}
