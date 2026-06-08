package com.mytasklistapp.util;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.mytasklistapp.model.Task;
import com.mytasklistapp.receiver.ReminderReceiver;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class NotificationHelper {

    private static final String TAG = "NotificationHelper";

    @SuppressLint("ScheduleExactAlarm")
    public static void scheduleReminder(Context context, Task task) {
        if (task.getDueDate() == null || task.getDueDate().isEmpty() || 
            task.getReminderTime() == null || task.getReminderTime().isEmpty()) {
            return;
        }

        String dateTimeStr = task.getDueDate() + " " + task.getReminderTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        
        try {
            Date date = sdf.parse(dateTimeStr);
            if (date != null && date.after(new Date())) {
                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                Intent intent = new Intent(context, ReminderReceiver.class);
                intent.putExtra("task_title", task.getTitle());
                intent.putExtra("task_description", task.getDescription());
                intent.putExtra("task_id", task.getId());

                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, task.getId(), intent, 
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

                if (alarmManager != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, date.getTime(), pendingIntent);
                    } else {
                        alarmManager.setExact(AlarmManager.RTC_WAKEUP, date.getTime(), pendingIntent);
                    }
                    Log.d(TAG, "Reminder scheduled for: " + dateTimeStr);
                }
            }
        } catch (ParseException e) {
            Log.e(TAG, "Error parsing date/time for reminder", e);
        }
    }

    public static void cancelReminder(Context context, int taskId) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, ReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, taskId, intent, 
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
            Log.d(TAG, "Reminder cancelled for task ID: " + taskId);
        }
    }
}
