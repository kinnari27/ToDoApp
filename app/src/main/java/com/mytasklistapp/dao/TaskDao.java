package com.mytasklistapp.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.mytasklistapp.model.Task;

import java.util.List;

/**
 * Data Access Object (DAO) for the Task entity.
 * Defines all SQL operations Room will generate implementations for.
 */
@Dao
public interface TaskDao {

    /**
     * Insert a new task. Room auto-generates the SQL.
     */
    @Insert
    long insert(Task task);

    /**
     * Update an existing task (matched by primary key).
     */
    @Update
    void update(Task task);

    /**
     * Delete a task by object reference.
     */
    @Delete
    void delete(Task task);

    /**
     * Get ALL tasks ordered by creation time descending.
     * Returns LiveData so the UI auto-updates on changes.
     */
    @Query("SELECT * FROM tasks ORDER BY createdAt DESC")
    LiveData<List<Task>> getAllTasks();

    @Query("SELECT * FROM tasks")
    List<Task> getAllTasksList();

    /**
     * Get only COMPLETED tasks.
     */
    @Query("SELECT * FROM tasks WHERE completed = 1 ORDER BY createdAt DESC")
    LiveData<List<Task>> getCompletedTasks();

    /**
     * Get only PENDING (not completed) tasks.
     */
    @Query("SELECT * FROM tasks WHERE completed = 0 ORDER BY createdAt DESC")
    LiveData<List<Task>> getPendingTasks();

    /**
     * Search tasks by title or description (case-insensitive LIKE).
     * Used by the SearchView in the toolbar.
     */
    @Query("SELECT * FROM tasks WHERE title LIKE '%' || :query || '%' " +
            "OR description LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    LiveData<List<Task>> searchTasks(String query);
}
