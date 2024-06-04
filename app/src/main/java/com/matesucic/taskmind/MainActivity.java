package com.matesucic.taskmind;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;
    ClassesFragment classesFragment = new ClassesFragment();
    TasksFragment tasksFragment = new TasksFragment();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSharedPreferences("classesData", 0).getAll().isEmpty()) {
            startActivity(new Intent(this, ClassesActivity.class));
            finish();
            return;
        }
        setContentView(R.layout.activity_main);
        this.bottomNavigationView = findViewById(R.id.bottom_navigation);
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.containerClasses, this.tasksFragment).commit();
        this.bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.tasks) {
                fragmentManager.beginTransaction().replace(R.id.containerClasses, this.tasksFragment).commit();
                return true;
            } else if (item.getItemId() != R.id.settings) {
                return true;
            } else {
                fragmentManager.beginTransaction().replace(R.id.containerClasses, this.classesFragment).commit();
                return true;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(AddTaskActivity.needsUpdate) {
            AddTaskActivity.needsUpdate = false;
            getSupportFragmentManager().beginTransaction().remove(tasksFragment).commit();
            TasksFragment newTasksFragment = new TasksFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.containerClasses, newTasksFragment).commit();
        }
    }
}