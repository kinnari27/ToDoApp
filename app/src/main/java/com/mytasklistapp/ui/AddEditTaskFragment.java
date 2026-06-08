package com.mytasklistapp.ui;

import android.app.DatePickerDialog;
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

import java.util.Calendar;
import java.util.Locale;

public class AddEditTaskFragment extends Fragment {

    private static final String EXTRA_TASK_ID = "extra_task_id";
    private static final String EXTRA_TASK_TITLE = "extra_task_title";
    private static final String EXTRA_TASK_DESCRIPTION = "extra_task_description";
    private static final String EXTRA_TASK_DUE_DATE = "extra_task_due_date";
    private static final String EXTRA_TASK_COMPLETED = "extra_task_completed";
    private static final String EXTRA_TASK_CREATED_AT = "extra_task_created_at";

    private TextInputEditText etTitle, etDescription, etDueDate;
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
        Button btnSave = view.findViewById(R.id.btnSave);

        // --- Determine mode from arguments ---
        Bundle args = getArguments();
        if (args != null && args.containsKey(EXTRA_TASK_TITLE)) {
            requireActivity().setTitle("Edit Task");
            existingTask = new Task(
                    args.getString(EXTRA_TASK_TITLE),
                    args.getString(EXTRA_TASK_DESCRIPTION),
                    args.getString(EXTRA_TASK_DUE_DATE),
                    args.getBoolean(EXTRA_TASK_COMPLETED, false),
                    args.getLong(EXTRA_TASK_CREATED_AT, 0)
            );
            existingTask.setId(args.getInt(EXTRA_TASK_ID, 0));

            // Pre-fill fields
            etTitle.setText(existingTask.getTitle());
            etDescription.setText(existingTask.getDescription());
            etDueDate.setText(existingTask.getDueDate());
        } else {
            requireActivity().setTitle("Add Task");
        }

        // --- Date picker ---
        etDueDate.setFocusable(false);
        etDueDate.setOnClickListener(v -> showDatePicker());

        // --- Save button ---
        btnSave.setOnClickListener(v -> saveTask());
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        new DatePickerDialog(requireContext(), (view, y, m, d) -> {
            String date = String.format(Locale.getDefault(), "%04d-%02d-%02d", y, m + 1, d);
            etDueDate.setText(date);
        }, year, month, day).show();
    }

    private void saveTask() {
        String title = etTitle.getText() != null ? etTitle.getText().toString().trim() : "";
        String desc = etDescription.getText() != null ? etDescription.getText().toString().trim() : "";
        String date = etDueDate.getText() != null ? etDueDate.getText().toString().trim() : "";

        if (TextUtils.isEmpty(title)) {
            etTitle.setError("Title is required");
            return;
        }

        if (existingTask == null) {
            Task newTask = new Task(title, desc, date, false, System.currentTimeMillis());
            viewModel.insert(newTask);
            Toast.makeText(requireContext(), "Task added!", Toast.LENGTH_SHORT).show();
        } else {
            existingTask.setTitle(title);
            existingTask.setDescription(desc);
            existingTask.setDueDate(date);
            viewModel.update(existingTask);
            Toast.makeText(requireContext(), "Task updated!", Toast.LENGTH_SHORT).show();
        }

        Navigation.findNavController(requireView()).navigateUp();
    }
}
