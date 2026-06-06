package com.example.planck.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface JobDao {

    @Insert
    long insert(Job job);

    @Update
    void update(Job job);

    @Delete
    void delete(Job job);

    @Query("SELECT * FROM jobs ORDER BY name ASC")
    LiveData<List<Job>> getAllJobs();

    @Query("SELECT * FROM jobs WHERE id = :id")
    Job getJobById(int id);

    // 加在 JobDao interface 裡
    @Query("SELECT * FROM jobs ORDER BY name ASC")
    List<Job> getAllJobsDirect();
}
