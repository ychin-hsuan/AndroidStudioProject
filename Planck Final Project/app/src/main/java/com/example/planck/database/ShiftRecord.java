package com.example.planck.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "shift_records")
public class ShiftRecord {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public int jobId;            // 對應哪個 Job
    public String jobName;       // 冗餘儲存方便顯示
    public int hourlyRate;       // 冗餘儲存，避免改時薪影響舊紀錄

    public long scheduledStart;  // 預計上班時間
    public long scheduledEnd;    // 預計下班時間

    public long actualStart;     // 實際上班（0 = 未打卡）
    public long actualEnd;       // 實際下班（0 = 未打卡）

    public float actualHours;    // 實際時數（系統計算）
    public int actualPay;        // 實際薪資（系統計算）

    public boolean isHoliday;    // 是否假日
    public boolean isConfirmed;  // 是否已確認
}