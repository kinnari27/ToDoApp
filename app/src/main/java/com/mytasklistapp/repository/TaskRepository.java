package com.mytasklistapp.repository;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.mytasklistapp.dao.TaskDao;
import com.mytasklistapp.database.TaskDatabase;
import com.mytasklistapp.model.Task;
import com.mytasklistapp.util.NotificationHelper;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Repository acts as the single source of truth between the ViewModel and data sources.
 * It abstracts away the Room database operations and runs them on a background thread.
 * It also synchronizes data with Firebase Realtime Database and manages local notifications.
 */
public class TaskRepository {

    private final TaskDao taskDao;
    private final DatabaseReference firebaseRef;
    private final FirebaseAuth mAuth;
    private final Application application;

    // Single-thread executor ensures DB writes happen sequentially
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public TaskRepository(Application application) {
        this.application = application;
        TaskDatabase db = TaskDatabase.getInstance(application);
        taskDao = db.taskDao();
        
        mAuth = FirebaseAuth.getInstance();
        // Initialize Firebase reference under "users"
        firebaseRef = FirebaseDatabase.getInstance().getReference("users");
    }

    private DatabaseReference getTaskRef() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            return firebaseRef.child(user.getUid()).child("tasks");
        }
        return null;
    }

    // --- Read operations ---

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

    // --- Write operations ---

    public void insert(Task task) {
        executorService.execute(() -> {
            // Generate Firebase ID if not present
            DatabaseReference ref = getTaskRef();
            if (ref != null && (task.getFirebaseId() == null || task.getFirebaseId().isEmpty())) {
                String key = ref.push().getKey();
                task.setFirebaseId(key);
            }
            
            // Insert locally and get the generated ID
            long id = taskDao.insert(task);
            task.setId((int) id);
            
            // Schedule notification
            if (!task.isCompleted() && task.isReminderEnabled()) {
                NotificationHelper.scheduleReminder(application, task);
            }
            
            // Sync to Firebase
            syncToFirebase(task);
        });
    }

    public void update(Task task) {
        executorService.execute(() -> {
            taskDao.update(task);
            
            // Re-schedule notification
            if (task.isCompleted() || !task.isReminderEnabled()) {
                NotificationHelper.cancelReminder(application, task.getId());
            } else {
                NotificationHelper.scheduleReminder(application, task);
            }
            
            syncToFirebase(task);
        });
    }

    public void delete(Task task) {
        executorService.execute(() -> {
            taskDao.delete(task);
            
            // Cancel notification
            NotificationHelper.cancelReminder(application, task.getId());
            
            DatabaseReference ref = getTaskRef();
            if (ref != null && task.getFirebaseId() != null) {
                ref.child(task.getFirebaseId()).removeValue()
                    .addOnFailureListener(e -> Log.e("Firebase", "Failed to delete task", e));
            }
        });
    }

    /**
     * Pushes all local tasks to Firebase. Useful for first-time sync.
     */
    public void syncAllLocalTasksToFirebase() {
        executorService.execute(() -> {
            List<Task> allTasks = taskDao.getAllTasksList();
            DatabaseReference ref = getTaskRef();
            if (ref != null && allTasks != null) {
                for (Task task : allTasks) {
                    if (task.getFirebaseId() == null || task.getFirebaseId().isEmpty()) {
                        String key = ref.push().getKey();
                        task.setFirebaseId(key);
                        taskDao.update(task);
                    }
                    ref.child(task.getFirebaseId()).setValue(task);
                }
            }
        });
    }

    private void syncToFirebase(Task task) {
        DatabaseReference ref = getTaskRef();
        if (ref != null && task.getFirebaseId() != null) {
            ref.child(task.getFirebaseId()).setValue(task)
                .addOnFailureListener(e -> Log.e("Firebase", "Failed to sync task", e));
        }
    }
}
