package com.example.todoapp.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.todoapp.dao.TaskDao;
import com.example.todoapp.database.TaskDatabase;
import com.example.todoapp.model.Task;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Repository acts as the single source of truth between the ViewModel and data sources.
 * It abstracts away the Room database operations and runs them on a background thread.
 *
 * Why a repository?
 * - ViewModels should NOT directly reference Android-specific classes like Context.
 * - Repository handles threading so ViewModels stay clean.
 */
public class TaskRepository {

    private final TaskDao taskDao;

    // Single-thread executor ensures DB writes happen sequentially (no race conditions)
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public TaskRepository(Application application) {
        TaskDatabase db = TaskDatabase.getInstance(application);
        taskDao = db.taskDao();
    }

    // --- Read operations (Room returns LiveData on main thread automatically) ---

    public LiveData<List<Task>> getAllTasks() {
        return taskDao.getAllTasks();
    }

    public LiveData<List<Task>> getCompletedTasks() {
        return taskDao.getCompletedTasks();
    }

    public LiveData<List<Task>> getPendingTasks() {
        return taskDao.getPendingTasks();
    }

    public LiveData<List<Task>> searchTasks(String query) {
        return taskDao.searchTasks(query);
    }

    // --- Write operations (must run off the main thread) ---

    public void insert(Task task) {
        executorService.execute(() -> taskDao.insert(task));
    }

    public void update(Task task) {
        executorService.execute(() -> taskDao.update(task));
    }

    public void delete(Task task) {
        executorService.execute(() -> taskDao.delete(task));
    }
}
