package com.cs407.tilt_2048;

public class Record {
    private int rank; // 排名
    private int score; // 分数

    public Record(int rank, int score) {
        this.rank = rank;
        this.score = score;
    }

    public int getRank() {
        return rank;
    }

    public int getScore() {
        return score;
    }
}
