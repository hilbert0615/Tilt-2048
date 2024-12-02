package com.cs407.tilt_2048;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class RecordAdapter extends RecyclerView.Adapter<RecordAdapter.RecordViewHolder> {

    private Context context;
    private List<Record> records;

    public RecordAdapter(Context context, List<Record> records) {
        this.context = context;
        this.records = records;
    }

    @NonNull
    @Override
    public RecordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_record_row, parent, false);
        return new RecordViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecordViewHolder holder, int position) {
        Record record = records.get(position);

        // 设置排名
        holder.tvRank.setText(String.valueOf(record.getRank()));

        // 设置分数
        if (record.getScore() > 0) {
            holder.tvScore.setText(String.valueOf(record.getScore()));
        } else {
            holder.tvScore.setText("--");
        }

        // 设置星标
        if (record.getRank() == 1) {
            holder.ivMedal.setImageResource(R.drawable.ic_star_gold);
        } else if (record.getRank() == 2) {
            holder.ivMedal.setImageResource(R.drawable.ic_star_silver);
        } else if (record.getRank() == 3) {
            holder.ivMedal.setImageResource(R.drawable.ic_star_bronze);
        } else {
            holder.ivMedal.setVisibility(View.GONE); // 非前三名隐藏图标
        }
    }

    @Override
    public int getItemCount() {
        return records.size();
    }

    static class RecordViewHolder extends RecyclerView.ViewHolder {
        TextView tvRank, tvScore;
        ImageView ivMedal;

        public RecordViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRank = itemView.findViewById(R.id.tvRank);
            tvScore = itemView.findViewById(R.id.tvScore);
            ivMedal = itemView.findViewById(R.id.ivMedal);
        }
    }
}
