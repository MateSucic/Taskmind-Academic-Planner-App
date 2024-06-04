package com.matesucic.taskmind;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class TasksFragment extends Fragment {
    LinearLayout layoutSubjects;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tasks, container, false);
        FloatingActionButton floatingActionButton = view.findViewById(R.id.floatingActionButton);
        layoutSubjects = view.findViewById(R.id.layoutSubjects);


        floatingActionButton.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), AddTaskActivity.class);
            startActivity(intent);
        });

        List<String> classesWithTasks = new ArrayList<>();
        List<Task> taskList = TaskManager.getTasks(requireContext());
        if (taskList != null) {
            try {
                final Calendar CALENDAR = Calendar.getInstance();

                // Use iterator to safely remove elements
                Iterator<Task> iterator = taskList.iterator();

                while (iterator.hasNext()) {
                    Task task = iterator.next();
                    // Check if the task is completed and 24 hours have passed
                    if (task.timeCompleted != null) {
                        Calendar timeToDelete = (Calendar) task.timeCompleted.clone();
                        timeToDelete.add(Calendar.HOUR, 24);
                        if (!CALENDAR.before(timeToDelete)) {
                            iterator.remove();  // Safe removal using the iterator
                            if (!hasTaskForSameDay(taskList, task)) {
                                int notificationId = Integer.parseInt(String.format("%s%s%s%s", task.isExam ? 1 : 0, task.dueDate.get(Calendar.YEAR), task.dueDate.get(Calendar.MONTH), task.dueDate.get(Calendar.DAY_OF_MONTH)));

                                NotificationScheduler.cancelNotification(requireContext(), notificationId);
                            }
                            continue;
                        }
                    }

                    // Check if the className is in classesWithTasks
                    String temp = task.className;
                    for (String className : classesWithTasks) {
                        if (className.equals(temp)) {
                            temp = "";
                            break;
                        }
                    }

                    // Add the className if not present
                    if (!temp.equals("")) {
                        classesWithTasks.add(temp);
                    }
                }

                // Save the modified taskList
                TaskManager.saveTasks(requireContext(), taskList);
            } catch (Exception e) {
                e.printStackTrace();
            }
            // Sort classesWithTasks by the due date of the earliest task
            classesWithTasks.sort((className1, className2) -> {
                // Get the earliest due dates for tasks associated with each class
                Calendar dueDate1 = getEarliestDueDate(taskList, className1);
                Calendar dueDate2 = getEarliestDueDate(taskList, className2);

                // Compare the due dates
                if (dueDate1 == null && dueDate2 == null) {
                    return 0; // Both classes have no tasks
                } else if (dueDate1 == null) {
                    return 1; // Class 2 has tasks, class 1 doesn't
                } else if (dueDate2 == null) {
                    return -1; // Class 1 has tasks, class 2 doesn't
                } else {
                    return dueDate1.compareTo(dueDate2); // Compare due dates
                }
            });

            for (int i = 0; i < classesWithTasks.size(); i++) {
                LinearLayout newSubjectItem = (LinearLayout) LayoutInflater.from(requireContext()).inflate(R.layout.subject_layout, layoutSubjects, false);
                RecyclerView recyclerView = newSubjectItem.findViewById(R.id.recyclerViewTasks);
                TextView className = newSubjectItem.findViewById(R.id.tvSubject);
                className.setText(classesWithTasks.get(i));
                List<Task> tasksOfClass = new ArrayList<>();
                for (int j = 0; j < taskList.size(); j++) {
                    if (taskList.get(j).className.equals(classesWithTasks.get(i))) {
                        tasksOfClass.add(taskList.get(j));
                    }
                }
                TaskListAdapter adapter = new TaskListAdapter(tasksOfClass);
                recyclerView.setHasFixedSize(true);
                recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.spacing);
                recyclerView.addItemDecoration(new ItemSpacingDecoration(spacingInPixels));
                recyclerView.setAdapter(adapter);
                layoutSubjects.addView(newSubjectItem);
            }
        }
        return view;
    }

    // Helper method to get the earliest due date for tasks associated with a class
    private Calendar getEarliestDueDate(List<Task> taskList, String className) {
        Calendar earliestDueDate = null;
        for (Task task : taskList) {
            if (task.className.equals(className)) {
                if (earliestDueDate == null || task.dueDate.before(earliestDueDate)) {
                    earliestDueDate = task.dueDate;
                }
            }
        }
        return earliestDueDate;
    }

    private static boolean hasTaskForSameDay(Collection<Task> tasks, Task currentTask) {
        for (Task task : tasks) {
            // Check if the task is for the same day and not the current task and is of same type
            if (isSameDay(task.dueDate, currentTask.dueDate) && !task.equals(currentTask) && task.isExam == currentTask.isExam) {
                return true;
            }
        }
        return false;
    }

    public static boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }
}