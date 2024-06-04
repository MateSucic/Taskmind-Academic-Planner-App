package com.matesucic.taskmind;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import java.util.Calendar;

public class Task implements Comparable<Task> {
    private final long id;
    String className;
    String title;
    Calendar dueDate;
    boolean isExam;

    public boolean isChecked() {
        return isChecked;
    }

    boolean isChecked = false;
    int daysToStudy;
    String description;
    Calendar timeCompleted;
    public Task(String className, String title, Calendar dueDate, boolean isExam, int daysToStudy, String description, Context context) {
        if (!TextUtils.isEmpty(className) && !TextUtils.isEmpty(title) && dueDate != null && !(isExam && daysToStudy < 1)) {
            SharedPreferences sharedPreferences = context.getSharedPreferences("app", Context.MODE_PRIVATE);
            this.id = sharedPreferences.getLong("id_counter", 0);
            if (Long.MAX_VALUE == id)
                sharedPreferences.edit().putLong("id_counter", 0).apply();
            else
                sharedPreferences.edit().putLong("id_counter", id+1).apply();

            this.className = className;
            this.title = title;
            this.dueDate = dueDate;
            this.isExam = isExam;
            this.daysToStudy = daysToStudy;
            this.description = description;
        } else {
            throw new IllegalArgumentException("Invalid Task: Some required fields are empty or null");
        }
    }

    @Override
    public int compareTo(Task otherTask) {
        return this.dueDate.compareTo(otherTask.dueDate);
    }

    public long getId() {
        return id;
    }
}