package com.example.planck.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "jobs")
public class Job {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;              // 工作名稱（例：超商）
    public int hourlyRate;           // 時薪
    public float holidayMultiplier;  // 假日倍率，預設 1.33
    public float overtimeThreshold;  // 加班起算時數，預設 8.0
    public float overtimeMultiplier; // 加班倍率，預設 1.33
    public String color;             // 顯示顏色 hex
}