package com.mytasklistapp.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Task entity representing a single to-do item stored in Room Database.
 * Annotated with @Entity so Room knows to create a table for it.
 */
@Entity(tableName = "tasks")
public class Task {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String title;        // Task title (required)
    private String description;  // Optional description
    private String dueDate;      // Due date as formatted string (e.g. "2024-12-31")
    private boolean completed;   // Whether the task is marked complete
    private long createdAt;      // Timestamp for sorting

    // --- Constructor ---
    public Task(String title, String description, String dueDate, boolean completed, long createdAt) {
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.completed = completed;
        this.createdAt = createdAt;
    }

    // --- Getters & Setters (required by Room) ---

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDueDate() { return dueDate; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
