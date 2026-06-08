package com.mytasklistapp.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.mytasklistapp.dao.TaskDao;
import com.mytasklistapp.model.Task;

/**
 * Room Database singleton.
 * - version: increment when schema changes (triggers migration).
 * - exportSchema: false keeps things simple for this project.
 *
 * Using a singleton prevents multiple database instances from opening simultaneously,
 * which could cause race conditions or data corruption.
 */
@Database(entities = {Task.class}, version = 1, exportSchema = false)
public abstract class TaskDatabase extends RoomDatabase {

    // Volatile ensures visibility across threads
    private static volatile TaskDatabase INSTANCE;

    /** Returns the DAO so callers can run queries. */
    public abstract TaskDao taskDao();

    /**
     * Returns the singleton database instance, creating it if necessary.
     * Uses double-checked locking to avoid redundant synchronization overhead.
     */
    public static TaskDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (TaskDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    TaskDatabase.class,
                                    "task_database"
                            )
                            // Allow Room to destructively recreate tables on version conflict
                            // (Replace with proper migrations in production)
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
