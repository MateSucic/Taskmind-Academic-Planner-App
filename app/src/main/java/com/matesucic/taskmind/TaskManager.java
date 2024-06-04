package com.matesucic.taskmind;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class TaskManager {
    private static final String TASKS_KEY = "tasks";

    public static void addTask(Context context, Task task) {
        List<Task> tasks = getTasks(context);

        if (tasks == null) {
            tasks = new ArrayList<>();
        }

        tasks.add(task);
        saveTasks(context, tasks);
    }


    public static List<Task> getTasks(Context context) {
        SharedPreferences sp = context.getSharedPreferences("classesData", Context.MODE_PRIVATE);
        String jsonTasks = sp.getString(TASKS_KEY, null);

        if (jsonTasks != null) {
            Gson gson = new Gson();
            Type taskListType = new TypeToken<List<Task>>() {
            }.getType();
            try {
                // Try to parse as a list of tasks
                List<Task> tasks = gson.fromJson(jsonTasks, taskListType);
                if (tasks != null && !tasks.isEmpty()) {
                    return tasks;
                }
            } catch (JsonSyntaxException e) {
                e.printStackTrace();
            }
        } else {
            return new ArrayList<>();
        }
        return null;
    }

    public static void saveTasks(Context context, List<Task> tasks) {
        Gson gson = new Gson();
        String jsonTasks = gson.toJson(tasks);

        SharedPreferences sp = context.getSharedPreferences("classesData", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(TASKS_KEY, jsonTasks);
        editor.apply();
    }
}
