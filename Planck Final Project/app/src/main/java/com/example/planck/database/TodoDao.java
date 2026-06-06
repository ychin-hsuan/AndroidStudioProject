package com.example.planck.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface TodoDao {

    @Insert
    void insert(TodoItem todo);

    @Update
    void update(TodoItem todo);

    @Delete
    void delete(TodoItem todo);

    @Query("SELECT * FROM todo_items WHERE status != 'DONE' ORDER BY priority DESC, deadlineTime ASC")
    LiveData<List<TodoItem>> getActiveTodos();

    @Query("SELECT * FROM todo_items WHERE deadlineTime >= :dayStart AND deadlineTime < :dayEnd")
    LiveData<List<TodoItem>> getTodayTodos(long dayStart, long dayEnd);

    @Query("SELECT * FROM todo_items WHERE status = 'DONE' ORDER BY completedAt DESC")
    LiveData<List<TodoItem>> getCompletedTodos();
}