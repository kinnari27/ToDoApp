package com.mytasklistapp.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.mytasklistapp.model.Task;
import com.mytasklistapp.repository.TaskRepository;

import java.util.List;

/**
 * ViewModel exposes LiveData to the UI and survives configuration changes (e.g. screen rotation).
 * It holds a reference to the Repository and never directly touches the database or UI.
 * <p>
 * AndroidViewModel is used (instead of plain ViewModel) because we need the Application context
 * to create the repository / database.
 */
public class TaskViewModel extends AndroidViewModel {

    // The LiveData that the UI observes — it switches source based on filter/search state
    public final LiveData<List<Task>> tasks;
    private final TaskRepository repository;
    // Tracks the current filter mode: "all", "completed", "pending", or a search query
    private final MutableLiveData<String> filterMode = new MutableLiveData<>("all");
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");

    public TaskViewModel(@NonNull Application application) {
        super(application);
        repository = new TaskRepository(application);

        // Transformations.switchMap re-evaluates the lambda whenever filterMode or searchQuery changes
        tasks = Transformations.switchMap(filterMode, mode -> {
            String query = searchQuery.getValue();
            if (query != null && !query.isEmpty()) {
                // Search mode overrides filter
                return repository.searchTasks(query);
            }
            switch (mode) {
                case "completed":
                    return repository.getCompletedTasks();
                case "pending":
                    return repository.getPendingTasks();
                default:
                    return repository.getAllTasks();
            }
        });
    }

    // --- Public API for the UI ---

    public void insert(Task task) {
        repository.insert(task);
    }

    public void update(Task task) {
        repository.update(task);
    }

    public void delete(Task task) {
        repository.delete(task);
    }

    /**
     * Switch between "all", "completed", "pending" filter tabs.
     */
    public void setFilter(String mode) {
        searchQuery.setValue(""); // Clear search when filter changes
        filterMode.setValue(mode);
    }

    /**
     * Trigger a live search query. Pass empty string to clear.
     */
    public void setSearchQuery(String query) {
        searchQuery.setValue(query);
        // Force switchMap to re-evaluate by poking filterMode
        filterMode.setValue(filterMode.getValue());
    }

    public String getCurrentFilter() {
        return filterMode.getValue();
    }
}
