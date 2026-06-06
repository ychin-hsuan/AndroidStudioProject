package com.example.planck.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "events")
public class Event {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String title;
    public String type;        // "TIME" / "REMIND" / "SHIFT"
    public int priority;       // 0=一般 / 1=重要 / 2=緊急
    public long startTime;
    public long endTime;
    public int bufferMinutes;
    public long deadlineTime;  // 0 = 沒有截止
    public String note;
    public int jobId;          // -1 = 非打工事件
}