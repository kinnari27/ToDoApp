package com.mytasklistapp.model;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/**
 * Task entity representing a single to-do item stored in Room Database.
 * Annotated with @Entity so Room knows to create a table for it.
 */
@Entity(tableName = "tasks")
public class Task {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String firebaseId;   // Unique ID for Firebase sync
    private String title;        // Task title (required)
    private String description;  // Optional description
    private String dueDate;      // Due date as formatted string (e.g. "2024-12-31")
    private String reminderTime; // Reminder time as formatted string (e.g. "14:30")
    private boolean completed;   // Whether the task is marked complete
    private boolean reminderEnabled = true; // Whether the reminder is active
    private long createdAt;      // Timestamp for sorting

    // --- Constructor ---
    public Task() {
        // Required for Firebase
    }

    @Ignore
    public Task(String title, String description, String dueDate, String reminderTime, boolean completed, long createdAt) {
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.reminderTime = reminderTime;
        this.completed = completed;
        this.createdAt = createdAt;
    }

    // --- Getters & Setters (required by Room) ---

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFirebaseId() {
        return firebaseId;
    }

    public void setFirebaseId(String firebaseId) {
        this.firebaseId = firebaseId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public String getReminderTime() {
        return reminderTime;
    }

    public void setReminderTime(String reminderTime) {
        this.reminderTime = reminderTime;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public boolean isReminderEnabled() {
        return reminderEnabled;
    }

    public void setReminderEnabled(boolean reminderEnabled) {
        this.reminderEnabled = reminderEnabled;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Creates a deep copy of the task.
     */
    public Task copy() {
        Task copy = new Task(title, description, dueDate, reminderTime, completed, createdAt);
        copy.setId(id);
        copy.setFirebaseId(firebaseId);
        copy.setReminderEnabled(reminderEnabled);
        return copy;
    }
}
