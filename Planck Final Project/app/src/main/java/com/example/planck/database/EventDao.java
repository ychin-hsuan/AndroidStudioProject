package com.example.planck.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface EventDao {

    @Insert
    void insert(Event event);

    @Update
    void update(Event event);

    @Delete
    void delete(Event event);

    @Query("SELECT * FROM events WHERE startTime >= :dayStart AND startTime < :dayEnd ORDER BY startTime ASC")
    LiveData<List<Event>> getEventsByDay(long dayStart, long dayEnd);

    @Query("SELECT * FROM events ORDER BY startTime ASC")
    LiveData<List<Event>> getAllEvents();

    @Query("SELECT * FROM events WHERE id = :id")
    Event getEventById(int id);
}