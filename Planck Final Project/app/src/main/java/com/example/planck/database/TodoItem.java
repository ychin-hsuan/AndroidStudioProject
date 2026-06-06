package com.example.planck.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "todo_items")
public class TodoItem {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String title;
    public int priority;       // 0=一般 / 1=重要 / 2=緊急
    public String status;      // "TODO" / "DOING" / "DONE" / "POSTPONED"
    public long deadlineTime;  // 0 = 沒有
    public long reminderTime;  // 0 = 沒有
    public int linkedEventId;  // -1 = 沒有綁定
    public int postponeCount;
    public long createdAt;
    public long completedAt;   // 0 = 未完成
}