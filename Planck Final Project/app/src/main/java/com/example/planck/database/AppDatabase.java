package com.example.planck.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(
        entities = {Event.class, TodoItem.class, Job.class, ShiftRecord.class},
        version = 2,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    public abstract EventDao eventDao();
    public abstract TodoDao todoDao();
    public abstract JobDao jobDao();
    public abstract ShiftDao shiftDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "planck_database"
                            )
                            .fallbackToDestructiveMigration() // version 升級時重建資料庫
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}