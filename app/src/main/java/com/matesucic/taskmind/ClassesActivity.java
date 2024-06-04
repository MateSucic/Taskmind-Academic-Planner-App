package com.matesucic.taskmind;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class ClassesActivity extends AppCompatActivity {
    ImageButton btnConfirm;
    List<EditText> classList = new ArrayList<>();
    LinearLayout container;
    EditText etClass0;
    boolean classMatching = false;
    private int emptyClasses = 0;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_classes);
        this.btnConfirm = findViewById(R.id.btnConfirm);
        this.container = findViewById(R.id.containerClasses);
        this.etClass0 = findViewById(R.id.etTaskTitle);

        SharedPreferences preferences = getSharedPreferences("classesData", 0);
        if (this.classList.size() <= 1) {
            this.btnConfirm.setVisibility(View.GONE);
        } else {
            this.btnConfirm.setVisibility(View.VISIBLE);
        }
        this.classList.add(this.etClass0);
        try {
            this.classList.get(this.classList.size() - 1).setOnEditorActionListener(new OnEntered());
        } catch (Exception ignored) {
        }
        this.btnConfirm.setOnClickListener(v -> {
            classMatching = false;
            emptyClasses = 0;
            for (EditText item : classList) {
                for (EditText item2 : classList) {
                    if (item.getText().toString().trim().equalsIgnoreCase(item2.getText().toString().trim()) && !item.equals(item2)) {
                        if (!TextUtils.isEmpty(item.getText().toString().trim()))
                            Toast.makeText(getApplicationContext(), "Duplicate classes!", Toast.LENGTH_SHORT).show();
                        classMatching = true;
                    }
                }
                if (TextUtils.isEmpty(item.getText().toString().trim())) {
                    emptyClasses++;
                }
                if (!classMatching) {
                    if (emptyClasses >= classList.size()) {
                        Toast.makeText(getApplicationContext(), "No classes added!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Set<String> classSet = new HashSet<>();
                    for (int i = 0; i < this.classList.size(); i++) {
                        try {
                            if (!TextUtils.isEmpty(this.classList.get(i).getText().toString()))
                                classSet.add(classList.get(i).getText().toString().trim().toUpperCase());
                        } catch (Exception ignored) {
                        }
                    }
                    if (!classSet.isEmpty()) {
                        preferences.edit().putStringSet("classSet", classSet).apply();
                        startActivity(new Intent(this, MainActivity.class));
                        finish();
                    }
                }
            }
        });
    }


    public void createNewEditText() {
        EditText et = new EditText(this);
        et.setLayoutParams(this.etClass0.getLayoutParams());
        et.setHint(this.etClass0.getHint());
        et.setTextSize(0, this.etClass0.getTextSize());
        et.setTextColor(this.etClass0.getCurrentTextColor());
        et.setBackground(this.etClass0.getBackground());
        et.setSingleLine();
        et.setImeOptions(this.etClass0.getImeOptions());
        et.setPadding(this.etClass0.getPaddingLeft(), this.etClass0.getPaddingTop(), this.etClass0.getPaddingRight(), this.etClass0.getPaddingBottom());
        View spacer1 = new View(this);
        spacer1.setLayoutParams(new LinearLayout.LayoutParams(-1, 38));
        this.classList.add(et);
        this.container.addView(spacer1);
        this.container.addView(et);
        et.setOnEditorActionListener(new OnEntered());
        if (this.classList.size() <= 1) {
            this.btnConfirm.setVisibility(View.GONE);
        } else {
            this.btnConfirm.setVisibility(View.VISIBLE);
        }
    }

    private class OnEntered implements TextView.OnEditorActionListener {

        public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
            //Provjera postoji li predmet istog imena
            for (EditText item : classList) {
                if (item.getText().toString().trim().equalsIgnoreCase(classList.get(ClassesActivity.this.classList.size() - 1).getText().toString().trim()) && item != classList.get(ClassesActivity.this.classList.size() - 1)) {
                    Toast.makeText(getApplicationContext(), "Class already exists!", Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
            //Provjera je li editText prazan
            if (TextUtils.isEmpty(ClassesActivity.this.classList.get(ClassesActivity.this.classList.size() - 1).getText().toString()) || actionId != 6) {
                return false;
            }
            ClassesActivity.this.classList.get(ClassesActivity.this.classList.size() - 1).setOnEditorActionListener(null);
            ClassesActivity.this.createNewEditText();
            ClassesActivity.this.classList.get(ClassesActivity.this.classList.size() - 1).requestFocus();
            return true;
        }
    }
}