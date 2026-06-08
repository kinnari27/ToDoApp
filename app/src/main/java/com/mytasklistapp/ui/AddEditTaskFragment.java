package com.mytasklistapp.ui;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.android.material.textfield.TextInputEditText;
import com.mytasklistapp.R;
import com.mytasklistapp.model.Task;
import com.mytasklistapp.viewmodel.TaskViewModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddEditTaskFragment extends Fragment {

    private static final String EXTRA_TASK_ID = "extra_task_id";
    private static final String EXTRA_TASK_TITLE = "extra_task_title";
    private static final String EXTRA_TASK_DESCRIPTION = "extra_task_description";
    private static final String EXTRA_TASK_DUE_DATE = "extra_task_due_date";
    private static final String EXTRA_TASK_REMINDER_TIME = "extra_task_reminder_time";
    private static final String EXTRA_TASK_COMPLETED = "extra_task_completed";
    private static final String EXTRA_TASK_CREATED_AT = "extra_task_created_at";

    private TextInputEditText etTitle, etDescription, etDueDate, etReminderTime;
    private TaskViewModel viewModel;
    private Task existingTask = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_edit_task, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // --- Toolbar ---
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
        if (((AppCompatActivity) requireActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity) requireActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> Navigation.findNavController(view).navigateUp());

        viewModel = new ViewModelProvider(this).get(TaskViewModel.class);

        etTitle = view.findViewById(R.id.etTitle);
        etDescription = view.findViewById(R.id.etDescription);
        etDueDate = view.findViewById(R.id.etDueDate);
        etReminderTime = view.findViewById(R.id.etReminderTime);
        Button btnSave = view.findViewById(R.id.btnSave);

        // --- Determine mode from arguments ---
        Bundle args = getArguments();
        if (args != null && args.containsKey(EXTRA_TASK_TITLE)) {
            requireActivity().setTitle("Edit Task");
            existingTask = new Task(
                    args.getString(EXTRA_TASK_TITLE),
                    args.getString(EXTRA_TASK_DESCRIPTION),
                    args.getString(EXTRA_TASK_DUE_DATE),
                    args.getString(EXTRA_TASK_REMINDER_TIME),
                    args.getBoolean(EXTRA_TASK_COMPLETED, false),
                    args.getLong(EXTRA_TASK_CREATED_AT, 0)
            );
            existingTask.setId(args.getInt(EXTRA_TASK_ID, 0));

            // Pre-fill fields
            etTitle.setText(existingTask.getTitle());
            etDescription.setText(existingTask.getDescription());
            etDueDate.setText(existingTask.getDueDate());
            etReminderTime.setText(existingTask.getReminderTime());
        } else {
            requireActivity().setTitle("Add Task");
        }

        // --- Date picker ---
        etDueDate.setFocusable(false);
        etDueDate.setOnClickListener(v -> showDatePicker());

        // --- Time picker ---
        etReminderTime.setFocusable(false);
        etReminderTime.setOnClickListener(v -> showTimePicker());

        // --- Save button ---
        btnSave.setOnClickListener(v -> saveTask());
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        String currentText = etDueDate.getText() != null ? etDueDate.getText().toString() : "";

        // If a date is already selected, initialize the picker with that date
        if (!currentText.isEmpty()) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            try {
                Date date = sdf.parse(currentText);
                if (date != null) {
                    calendar.setTime(date);
                }
            } catch (ParseException ignored) {
            }
        }

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(requireContext(), (view, y, m, d) -> {
            String date = String.format(Locale.getDefault(), "%04d-%02d-%02d", y, m + 1, d);
            etDueDate.setText(date);
        }, year, month, day);

        // Disable selection of past dates (allow from today onwards)
        dialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        
        dialog.show();
    }

    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();
        String currentText = etReminderTime.getText() != null ? etReminderTime.getText().toString() : "";

        if (!currentText.isEmpty()) {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            try {
                Date time = sdf.parse(currentText);
                if (time != null) {
                    calendar.setTime(time);
                }
            } catch (ParseException ignored) {
            }
        }

        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog dialog = new TimePickerDialog(requireContext(), (view, h, m) -> {
            String time = String.format(Locale.getDefault(), "%02d:%02d", h, m);
            etReminderTime.setText(time);
        }, hour, minute, true);

        dialog.show();
    }

    private void saveTask() {
        String title = etTitle.getText() != null ? etTitle.getText().toString().trim() : "";
        String desc = etDescription.getText() != null ? etDescription.getText().toString().trim() : "";
        String date = etDueDate.getText() != null ? etDueDate.getText().toString().trim() : "";
        String time = etReminderTime.getText() != null ? etReminderTime.getText().toString().trim() : "";

        if (TextUtils.isEmpty(title)) {
            etTitle.setError("Title is required");
            return;
        }

        if (existingTask == null) {
            Task newTask = new Task(title, desc, date, time, false, System.currentTimeMillis());
            viewModel.insert(newTask);
            Toast.makeText(requireContext(), "Task added!", Toast.LENGTH_SHORT).show();
        } else {
            existingTask.setTitle(title);
            existingTask.setDescription(desc);
            existingTask.setDueDate(date);
            existingTask.setReminderTime(time);
            viewModel.update(existingTask);
            Toast.makeText(requireContext(), "Task updated!", Toast.LENGTH_SHORT).show();
        }

        Navigation.findNavController(requireView()).navigateUp();
    }
}
