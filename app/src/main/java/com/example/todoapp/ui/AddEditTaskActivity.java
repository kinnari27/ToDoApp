package com.example.todoapp.ui;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.example.todoapp.R;
import com.example.todoapp.model.Task;
import com.example.todoapp.viewmodel.TaskViewModel;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;
import java.util.Locale;

/**
 * AddEditTaskActivity handles BOTH adding a new task and editing an existing one.
 *
 * Mode detection:
 * - If the intent carries a Task extra → EDIT mode (pre-fill fields).
 * - If no extra → ADD mode (blank form).
 */
public class AddEditTaskActivity extends AppCompatActivity {

    // Intent extra keys
    private static final String EXTRA_TASK_ID          = "extra_task_id";
    private static final String EXTRA_TASK_TITLE       = "extra_task_title";
    private static final String EXTRA_TASK_DESCRIPTION = "extra_task_description";
    private static final String EXTRA_TASK_DUE_DATE    = "extra_task_due_date";
    private static final String EXTRA_TASK_COMPLETED   = "extra_task_completed";
    private static final String EXTRA_TASK_CREATED_AT  = "extra_task_created_at";

    private TextInputEditText etTitle, etDescription, etDueDate;
    private TaskViewModel viewModel;

    // Holds the task being edited (null in add mode)
    private Task existingTask = null;

    /**
     * Convenience factory method — call this from MainActivity instead of building intents manually.
     * @param task pass null for add mode, or an existing Task for edit mode.
     */
    public static void start(Context context, Task task) {
        Intent intent = new Intent(context, AddEditTaskActivity.class);
        if (task != null) {
            intent.putExtra(EXTRA_TASK_ID,          task.getId());
            intent.putExtra(EXTRA_TASK_TITLE,       task.getTitle());
            intent.putExtra(EXTRA_TASK_DESCRIPTION, task.getDescription());
            intent.putExtra(EXTRA_TASK_DUE_DATE,    task.getDueDate());
            intent.putExtra(EXTRA_TASK_COMPLETED,   task.isCompleted());
            intent.putExtra(EXTRA_TASK_CREATED_AT,  task.getCreatedAt());
        }
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_task);

        // --- Toolbar ---
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        viewModel = new ViewModelProvider(this).get(TaskViewModel.class);

        etTitle       = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        etDueDate     = findViewById(R.id.etDueDate);
        Button btnSave = findViewById(R.id.btnSave);

        // --- Determine mode from intent ---
        Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_TASK_TITLE)) {
            // EDIT MODE: reconstruct the task from intent extras
            setTitle("Edit Task");
            existingTask = new Task(
                    intent.getStringExtra(EXTRA_TASK_TITLE),
                    intent.getStringExtra(EXTRA_TASK_DESCRIPTION),
                    intent.getStringExtra(EXTRA_TASK_DUE_DATE),
                    intent.getBooleanExtra(EXTRA_TASK_COMPLETED, false),
                    intent.getLongExtra(EXTRA_TASK_CREATED_AT, 0)
            );
            existingTask.setId(intent.getIntExtra(EXTRA_TASK_ID, 0));

            // Pre-fill fields
            etTitle.setText(existingTask.getTitle());
            etDescription.setText(existingTask.getDescription());
            etDueDate.setText(existingTask.getDueDate());
        } else {
            setTitle("Add Task");
        }

        // --- Date picker: tap field to open calendar ---
        etDueDate.setFocusable(false);
        etDueDate.setOnClickListener(v -> showDatePicker());

        // --- Save button ---
        btnSave.setOnClickListener(v -> saveTask());

        // Enable back arrow in toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    /** Open a Material DatePickerDialog and format the chosen date into the field. */
    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year  = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day   = calendar.get(Calendar.DAY_OF_MONTH);

        new DatePickerDialog(this, (view, y, m, d) -> {
            String date = String.format(Locale.getDefault(), "%04d-%02d-%02d", y, m + 1, d);
            etDueDate.setText(date);
        }, year, month, day).show();
    }

    /** Validate inputs and insert/update the task via ViewModel. */
    private void saveTask() {
        String title = etTitle.getText() != null ? etTitle.getText().toString().trim() : "";
        String desc  = etDescription.getText() != null ? etDescription.getText().toString().trim() : "";
        String date  = etDueDate.getText() != null ? etDueDate.getText().toString().trim() : "";

        if (TextUtils.isEmpty(title)) {
            etTitle.setError("Title is required");
            return;
        }

        if (existingTask == null) {
            // ADD MODE: create a fresh task
            Task newTask = new Task(title, desc, date, false, System.currentTimeMillis());
            viewModel.insert(newTask);
            Toast.makeText(this, "Task added!", Toast.LENGTH_SHORT).show();
        } else {
            // EDIT MODE: update existing task fields
            existingTask.setTitle(title);
            existingTask.setDescription(desc);
            existingTask.setDueDate(date);
            viewModel.update(existingTask);
            Toast.makeText(this, "Task updated!", Toast.LENGTH_SHORT).show();
        }

        finish(); // Return to MainActivity
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
