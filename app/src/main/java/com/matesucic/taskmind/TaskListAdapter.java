package com.matesucic.taskmind;

import static com.matesucic.taskmind.TasksFragment.isSameDay;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TaskListAdapter extends RecyclerView.Adapter<TaskListAdapter.TaskViewHolder> {
    private final Task[] tasks;
    int completedTasks = 0;

    public TaskListAdapter(List<Task> taskList) {
        Map<Boolean, List<Task>> groupedTasks = taskList.stream()
                .collect(Collectors.groupingBy(Task::isChecked));
        groupedTasks.values().forEach(tasks -> tasks.sort(Comparator.comparing(task -> task.dueDate.getTime())));
        List<Task> sortedTaskList = new ArrayList<>();
        groupedTasks.values().forEach(sortedTaskList::addAll);
        this.tasks = sortedTaskList.toArray(new Task[0]);
        for (Task task : tasks) {
            if (task.isChecked)
                completedTasks++;
        }
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem = layoutInflater.inflate(R.layout.recycler_view_row_task, parent, false);
        return new TaskViewHolder(listItem);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        final Calendar CALENDAR = Calendar.getInstance();
        final Task task = tasks[position];

        holder.isTaskDone.setText(task.title);

        if (!clearTimeFields(task.dueDate).after(clearTimeFields(CALENDAR))) {
            holder.tvDueDate.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.red));
        } else {
            holder.tvDueDate.setTextColor(Color.parseColor("#616161"));
        }

        holder.isTaskDone.setChecked(task.isChecked);
        if (task.isChecked) {
            int grayColor = Color.argb(179, 169, 169, 169);
            GradientDrawable drawable = new GradientDrawable();
            drawable.setColor(grayColor);
            drawable.setCornerRadius(holder.itemView.getContext().getResources().getDimension(R.dimen.spacing));
            holder.itemView.setForeground(drawable);
        }

        if (!TextUtils.isEmpty(task.description)) {
            holder.btnDescription.setOnClickListener(v -> AlertDialogDescription.showAlertDialog(v.getContext(), task.description));
        } else {
            holder.btnDescription.setVisibility(View.GONE);
        }

        // Check if the task is due tomorrow
        Calendar tomorrow = (Calendar) CALENDAR.clone();
        tomorrow.add(Calendar.DAY_OF_MONTH, 1);

        if (!task.isExam) {
            if (isSameDay(task.dueDate, tomorrow)) {
                holder.tvDueDate.setText("DUE: TOMORROW");
            }
            else
                holder.tvDueDate.setText(String.format("DUE: %s/%s/%s", task.dueDate.get(Calendar.DAY_OF_MONTH), task.dueDate.get(Calendar.MONTH) + 1, task.dueDate.get(Calendar.YEAR)));
        }
        else {
            if (isSameDay(task.dueDate, tomorrow)) {
                holder.tvDueDate.setText("EXAM: TOMORROW");
            }
            else
                holder.tvDueDate.setText(String.format("EXAM DATE: %s/%s/%s", task.dueDate.get(Calendar.DAY_OF_MONTH), task.dueDate.get(Calendar.MONTH) + 1, task.dueDate.get(Calendar.YEAR)));
        }

        holder.tvIsExam.setVisibility(task.isExam ? View.VISIBLE : View.GONE);

        holder.isTaskDone.setOnCheckedChangeListener((buttonView, isChecked) -> {
            List<Task> taskList = TaskManager.getTasks(buttonView.getContext());
            final int currentPosition = holder.getAdapterPosition();

            if (isChecked) {
                int grayColor = Color.argb(179, 169, 169, 169);
                GradientDrawable drawable = new GradientDrawable();
                drawable.setColor(grayColor);
                drawable.setCornerRadius(holder.itemView.getContext().getResources().getDimension(R.dimen.spacing));
                holder.itemView.setForeground(drawable);
                // Shift the tasks left, excluding the task to be moved
                Task temp = tasks[currentPosition];
                for (int i = currentPosition; i < tasks.length - 1; i++) {
                    tasks[i] = tasks[i + 1];
                }
                tasks[tasks.length - 1] = temp;

                // Notify the RecyclerView about the move
                new Handler(Looper.getMainLooper()).post(() -> notifyItemMoved(currentPosition, tasks.length - 1));
                holder.isTaskDone.setChecked(true);
                completedTasks++;
                assert taskList != null;
                for (Task value : taskList) {
                    if (value.getId() == task.getId()) {
                        value.isChecked = true;
                        value.timeCompleted = Calendar.getInstance();
                        break;
                    }
                }
                TaskManager.saveTasks(buttonView.getContext(), taskList);
            } else {
                holder.itemView.setForeground(new ColorDrawable(Color.TRANSPARENT));
                holder.isTaskDone.setChecked(false);
                if (tasks.length > 1) {
                    Task[] subArray = Arrays.copyOfRange(tasks, 0, tasks.length - completedTasks);
                    int insertIndex = findInsertPosition(subArray, task);
                    Task temp = tasks[currentPosition];
                    // Shift the tasks right to make space for the task
                    for (int i = currentPosition; i > insertIndex; i--) {
                        tasks[i] = tasks[i - 1];
                    }
                    // Insert the unchecked task at position
                    tasks[insertIndex] = temp;
                    new Handler(Looper.getMainLooper()).post(() -> notifyItemMoved(currentPosition, insertIndex));
                }
                completedTasks--;
                assert taskList != null;
                for (Task value : taskList) {
                    if (value.getId() == task.getId()) {
                        value.isChecked = false;
                        value.timeCompleted = null;
                        break;
                    }
                }
                TaskManager.saveTasks(buttonView.getContext(), taskList);
            }

        });
    }

    private int findInsertPosition(Task[] sortedTaskList, Task taskToInsert) {
        int insertionIndex = Arrays.binarySearch(sortedTaskList, taskToInsert, Comparator.naturalOrder());

        if (insertionIndex < 0) {
            insertionIndex = -(insertionIndex + 1);
        }

        return insertionIndex;
    }


    @Override
    public int getItemCount() {
        return tasks.length;
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView tvDueDate;
        CheckBox isTaskDone;
        Button btnDescription;
        TextView tvIsExam;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            isTaskDone = itemView.findViewById(R.id.isTaskDone);
            tvDueDate = itemView.findViewById(R.id.tvDueDate);
            btnDescription = itemView.findViewById(R.id.btnTaskDescription);
            tvIsExam = itemView.findViewById(R.id.tvIsExam);
        }
    }

    private static Calendar clearTimeFields(Calendar calendar) {
        Calendar clearedCalendar = (Calendar) calendar.clone();

        clearedCalendar.set(Calendar.HOUR_OF_DAY, 0);
        clearedCalendar.set(Calendar.MINUTE, 0);
        clearedCalendar.set(Calendar.SECOND, 0);
        clearedCalendar.set(Calendar.MILLISECOND, 0);

        return clearedCalendar;
    }
}
