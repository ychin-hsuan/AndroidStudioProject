package com.example.planck.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface ShiftDao {

    @Insert
    void insert(ShiftRecord shift);

    @Update
    void update(ShiftRecord shift);

    @Delete
    void delete(ShiftRecord shift);

    // 取得某月所有班次
    @Query("SELECT * FROM shift_records WHERE scheduledStart >= :monthStart AND scheduledStart < :monthEnd ORDER BY scheduledStart DESC")
    LiveData<List<ShiftRecord>> getShiftsByMonth(long monthStart, long monthEnd);

    // 取得某月實際薪資總和
    @Query("SELECT SUM(actualPay) FROM shift_records WHERE scheduledStart >= :monthStart AND scheduledStart < :monthEnd AND isConfirmed = 1")
    LiveData<Integer> getTotalPayByMonth(long monthStart, long monthEnd);

    // 取得某月預計薪資總和
    @Query("SELECT SUM(CAST((scheduledEnd - scheduledStart) / 3600000.0 * hourlyRate AS INTEGER)) FROM shift_records WHERE scheduledStart >= :monthStart AND scheduledStart < :monthEnd")
    LiveData<Integer> getScheduledPayByMonth(long monthStart, long monthEnd);

    @Query("SELECT * FROM shift_records WHERE scheduledStart >= :monthStart AND scheduledStart < :monthEnd ORDER BY scheduledStart DESC")

    List<ShiftRecord> getShiftsByMonthDirect(long monthStart, long monthEnd);
}