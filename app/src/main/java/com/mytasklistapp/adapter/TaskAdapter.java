package com.mytasklistapp.adapter;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.mytasklistapp.R;
import com.mytasklistapp.model.Task;

import java.util.Objects;

/**
 * RecyclerView Adapter using ListAdapter + DiffUtil for efficient, animated updates.
 * ListAdapter automatically calculates the diff between old and new lists and
 * animates only the changed items — no need to call notifyDataSetChanged().
 */
public class TaskAdapter extends ListAdapter<Task, TaskAdapter.TaskViewHolder> {

    /**
     * DiffUtil compares items to decide what changed.
     * areItemsTheSame: are these the same task (by ID)?
     * areContentsTheSame: did any field change?
     */
    private static final DiffUtil.ItemCallback<Task> DIFF_CALLBACK = new DiffUtil.ItemCallback<Task>() {
        @Override
        public boolean areItemsTheSame(@NonNull Task oldItem, @NonNull Task newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Task oldItem, @NonNull Task newItem) {
            // Check booleans first as they are fastest
            if (oldItem.isCompleted() != newItem.isCompleted()) return false;
            if (oldItem.isReminderEnabled() != newItem.isReminderEnabled()) return false;
            
            // Check strings with null safety
            if (!Objects.equals(oldItem.getTitle(), newItem.getTitle())) return false;
            if (!Objects.equals(oldItem.getDueDate(), newItem.getDueDate())) return false;
            return Objects.equals(oldItem.getReminderTime(), newItem.getReminderTime());
        }
    };
    private final OnTaskActionListener listener;

    public TaskAdapter(OnTaskActionListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        holder.bind(getItem(position), listener);
    }

    // Callbacks to the Activity/Fragment for item interactions
    public interface OnTaskActionListener {
        void onTaskClick(Task task);         // Edit task

        void onTaskDelete(Task task);        // Delete with confirmation

        void onTaskToggle(Task task, boolean completed); // Mark complete/incomplete

        void onNotificationToggle(Task task); // Toggle reminder enabled/disabled
    }

    // --- ViewHolder ---

    static class TaskViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvTitle;
        private final TextView tvDescription;
        private final TextView tvDueDate;
        private final CheckBox cbCompleted;
        private final ImageButton btnDelete;
        private final ImageButton btnNotificationToggle;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTaskTitle);
            tvDescription = itemView.findViewById(R.id.tvTaskDescription);
            tvDueDate = itemView.findViewById(R.id.tvDueDate);
            cbCompleted = itemView.findViewById(R.id.cbCompleted);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnNotificationToggle = itemView.findViewById(R.id.btnNotificationToggle);
        }

        void bind(Task task, OnTaskActionListener listener) {
            tvTitle.setText(task.getTitle());

            // Show description only if present
            if (task.getDescription() != null && !task.getDescription().isEmpty()) {
                tvDescription.setVisibility(View.VISIBLE);
                tvDescription.setText(task.getDescription());
            } else {
                tvDescription.setVisibility(View.GONE);
            }

            // Show due date and reminder time
            StringBuilder dateTimeInfo = new StringBuilder();
            if (task.getDueDate() != null && !task.getDueDate().isEmpty()) {
                dateTimeInfo.append("Due: ").append(task.getDueDate());
            }
            if (task.getReminderTime() != null && !task.getReminderTime().isEmpty()) {
                if (dateTimeInfo.length() > 0) dateTimeInfo.append(" at ");
                dateTimeInfo.append(task.getReminderTime());
            }

            if (dateTimeInfo.length() > 0) {
                tvDueDate.setVisibility(View.VISIBLE);
                tvDueDate.setText(dateTimeInfo.toString());
            } else {
                tvDueDate.setVisibility(View.GONE);
            }

            // Strike-through title when task is completed
            if (task.isCompleted()) {
                tvTitle.setPaintFlags(tvTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                tvTitle.setAlpha(0.5f);
            } else {
                tvTitle.setPaintFlags(tvTitle.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
                tvTitle.setAlpha(1.0f);
            }

            // Update notification icon based on state
            if (task.isReminderEnabled()) {
                btnNotificationToggle.setImageResource(R.drawable.ic_notifications);
                btnNotificationToggle.setImageAlpha(255); // 1.0f
            } else {
                btnNotificationToggle.setImageResource(R.drawable.ic_notifications_off);
                btnNotificationToggle.setImageAlpha(128); // 0.5f
            }

            // Temporarily remove listener before setting state
            cbCompleted.setOnCheckedChangeListener(null);
            cbCompleted.setChecked(task.isCompleted());
            cbCompleted.setOnCheckedChangeListener((btn, isChecked) ->
                    listener.onTaskToggle(task, isChecked));

            // Click card body to edit
            itemView.setOnClickListener(v -> listener.onTaskClick(task));

            // Click delete icon to trigger confirmation dialog
            btnDelete.setOnClickListener(v -> listener.onTaskDelete(task));

            // Click notification icon to toggle reminder
            btnNotificationToggle.setOnClickListener(v -> {
                listener.onNotificationToggle(task);
            });
        }
    }
}
