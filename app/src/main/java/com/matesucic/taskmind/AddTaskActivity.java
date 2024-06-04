package com.matesucic.taskmind;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

public class AddTaskActivity extends AppCompatActivity {
    ImageButton back, calendarButton, btnConfirm;
    Spinner spinnerClass;
    Spinner spinnerTimes;
    CheckBox checkBox;
    LinearLayout lLayout;
    TextView tvDate;
    EditText etDescription, etTaskTitle;
    Calendar examDate = null;
    static boolean needsUpdate = false;
    int daysToStudy;

    @SuppressLint("NewApi")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);
        this.spinnerClass = findViewById(R.id.spinnerSelectClass);
        this.back = findViewById(R.id.btnBack);
        calendarButton = findViewById(R.id.btnCalendar);
        spinnerTimes = findViewById(R.id.spinnerTimes);
        checkBox = findViewById(R.id.examCheckbox);
        tvDate = findViewById(R.id.tvCalendar);
        lLayout = findViewById(R.id.extraExamLayout);
        btnConfirm = findViewById(R.id.btnConfirm2);
        etDescription = findViewById(R.id.etDescription);
        etTaskTitle = findViewById(R.id.etTaskTitle);
        lLayout.setVisibility(View.INVISIBLE);

        final Calendar CALENDAR = Calendar.getInstance();
        final String TEXT_VIEW_DEFAULT = tvDate.getText().toString();

        //Dodavanje svih predmeta iz memorije
        List<String> classList = new ArrayList<>(Objects.requireNonNull(getSharedPreferences("classesData", 0).getStringSet("classSet", null)));
        this.spinnerClass.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, classList));
        //Back prekida activity
        this.back.setOnClickListener(v -> finish());

        int yy = CALENDAR.get(Calendar.YEAR);
        int mm = CALENDAR.get(Calendar.MONTH);
        int dd = CALENDAR.get(Calendar.DAY_OF_MONTH);

        calendarButton.setOnClickListener(v -> {

            //Otvaranje kalendara
            DatePickerDialog mDatePicker = new DatePickerDialog(AddTaskActivity.this, (datepicker, selectedYear, selectedMonth, selectedDay) -> {
                //Postavljanje objekta na odabrani datum
                examDate = Calendar.getInstance();
                examDate.set(selectedYear, selectedMonth, selectedDay);
                tvDate.setText(String.format("%s %s/%s/%s", TEXT_VIEW_DEFAULT, selectedDay, selectedMonth + 1, selectedYear));
                //Broj slobodnih dana za ucenje
                long differenceInMilliseconds = examDate.getTimeInMillis() - CALENDAR.getTimeInMillis();
                daysToStudy = (int) (differenceInMilliseconds / (24 * 60 * 60 * 1000));
                ArrayList<String> frequencyList = new ArrayList<>();
                frequencyList.add("1 day");
                //Dodaju se dani za
                for (int i = 2; i < daysToStudy; i++) {
                    frequencyList.add(i + " days");
                }
                //Ako je taj broj veći od 1 dodaje se opcija svaki dan (jer ako je 1 onda postoji samo jedna opcija)
                if (daysToStudy > 1)
                    frequencyList.add("Every day");
                spinnerTimes.setAdapter(new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_list_item_1,
                        frequencyList
                ));
            }, yy, mm, dd);
            Calendar calTemp = Calendar.getInstance();
            //Postavljanje minimalnog mogućeg datuma na sutrašnji
            calTemp.set(Calendar.MONTH, mm);
            calTemp.set(Calendar.DAY_OF_MONTH, dd + 1);
            calTemp.set(Calendar.YEAR, yy);
            mDatePicker.getDatePicker().setMinDate(calTemp.getTimeInMillis());
            mDatePicker.setTitle("Select date");
            mDatePicker.show();
        });

        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked)
                lLayout.setVisibility(View.VISIBLE);
            else
                lLayout.setVisibility(View.INVISIBLE);
        });

        btnConfirm.setOnClickListener(v -> {
            try {
                if (checkBox.isChecked()) {
                    if (!spinnerTimes.getSelectedItem().toString().equals("Every day"))
                        daysToStudy = Integer.parseInt(spinnerTimes.getSelectedItem().toString().replaceAll("\\D", ""));
                }
                Task task = new Task(spinnerClass.getSelectedItem().toString(), etTaskTitle.getText().toString().trim().toUpperCase(), examDate, checkBox.isChecked(), daysToStudy, etDescription.getText().toString().trim(), getApplicationContext());
                TaskManager.addTask(getApplicationContext(), task);
                needsUpdate = true;

                AlarmManager manager = ContextCompat.getSystemService(this, AlarmManager.class);
                if ((manager != null && !manager.canScheduleExactAlarms() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S || ActivityCompat.checkSelfPermission(this, Manifest.permission.SCHEDULE_EXACT_ALARM) != PackageManager.PERMISSION_GRANTED) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Intent permissionIntent = new Intent(this, PermissionRequestActivity.class);
                    permissionIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(permissionIntent);
                }
                Calendar notifyTime = task.dueDate;
                notifyTime.set(Calendar.HOUR_OF_DAY, 6);
                notifyTime.set(Calendar.MINUTE, 0);
                if (!task.isExam) {
                    int notificationId = Integer.parseInt(String.format("%s%s%s%s", 0, notifyTime.get(Calendar.YEAR), notifyTime.get(Calendar.MONTH), notifyTime.get(Calendar.DAY_OF_MONTH)));
                    notifyTime.add(Calendar.DAY_OF_MONTH, -1);
                    if (!NotificationScheduler.isNotificationScheduled(this, notificationId))
                        NotificationScheduler.scheduleNotification(this, notificationId, notifyTime, task.isExam);
                } else {
                    for (int i = 1; i <= daysToStudy; i++) {
                        notifyTime.add(Calendar.DAY_OF_MONTH, -1);
                        int notificationId = Integer.parseInt(String.format("%s%s%s%s", 1, notifyTime.get(Calendar.YEAR), notifyTime.get(Calendar.MONTH), notifyTime.get(Calendar.DAY_OF_MONTH)));
                        if (!NotificationScheduler.isNotificationScheduled(this, notificationId))
                            NotificationScheduler.scheduleNotification(this, notificationId, notifyTime, task.isExam);
                    }
                }
                finish();
            } catch (Exception e) {
                Log.e("Exception", Objects.requireNonNull(e.getMessage()));
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


}