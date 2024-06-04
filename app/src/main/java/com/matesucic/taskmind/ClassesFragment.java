package com.matesucic.taskmind;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class ClassesFragment extends Fragment {
    ImageButton btnConfirm;
    List<EditText> classList = new ArrayList<>();
    LinearLayout container;
    EditText etClass0;
    private long lastButtonClickTime = 0;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_classes, container, false);

        this.btnConfirm = view.findViewById(R.id.btnConfirm);
        this.container = view.findViewById(R.id.containerClasses);
        this.etClass0 = view.findViewById(R.id.etTaskTitle);

        SharedPreferences preferences = requireActivity().getSharedPreferences("classesData", 0);

        try {
            this.classList.get(this.classList.size() - 1).setOnEditorActionListener(new onEntered());
        } catch (Exception ignored) {
        }

        this.btnConfirm.setOnClickListener(v -> {
            long currentTime = System.currentTimeMillis();
            // Check if the button click is within the cooldown period
            if (currentTime - lastButtonClickTime < 2000) {
                return;
            }
            lastButtonClickTime = currentTime;

            for (EditText item : classList) {
                for (EditText item2 : classList) {
                    if (item.getText().toString().trim().equalsIgnoreCase(item2.getText().toString().trim()) && !item.equals(item2)) {
                        if (!TextUtils.isEmpty(item.getText().toString().trim()))
                            Toast.makeText(requireActivity().getApplicationContext(), "Duplicate classes!", Toast.LENGTH_SHORT).show();
                        else {
                            Toast.makeText(requireActivity().getApplicationContext(), "No classes added!", Toast.LENGTH_SHORT).show();
                        }
                        return;
                    }
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
                    if (classSet.equals(Objects.requireNonNull(preferences.getStringSet("classSet", null)))) {
                        Toast.makeText(requireContext(), "Classes already saved!", Toast.LENGTH_SHORT).show();
                    } else {
                        preferences.edit().putStringSet("classSet", classSet).apply();
                        Toast.makeText(requireContext(), "Classes successfully saved!", Toast.LENGTH_SHORT).show();
                    }
                    return;
                }
            }
        });
        return view;
    }

    public void createNewEditText() {
        EditText et = new EditText(requireContext());
        et.setLayoutParams(this.etClass0.getLayoutParams());
        et.setHint(this.etClass0.getHint());
        et.setTextSize(0, this.etClass0.getTextSize());
        et.setTextColor(this.etClass0.getCurrentTextColor());
        et.setBackground(this.etClass0.getBackground());
        et.setSingleLine();
        et.setImeOptions(this.etClass0.getImeOptions());
        et.setPadding(this.etClass0.getPaddingLeft(), this.etClass0.getPaddingTop(), this.etClass0.getPaddingRight(), this.etClass0.getPaddingBottom());
        View spacer1 = new View(requireContext());
        spacer1.setLayoutParams(new LinearLayout.LayoutParams(-1, 38));
        this.classList.add(et);

        int lastIndex = this.classList.size() - 1;

        List<String> classNames = new ArrayList<>(Objects.requireNonNull(requireActivity().getSharedPreferences("classesData", 0).getStringSet("classSet", null)));

        if (lastIndex < classNames.size()) {
            String storedText = classNames.get(lastIndex);
            et.setText(storedText);
        }

        this.container.addView(spacer1);
        this.container.addView(et);
        et.setOnEditorActionListener(new onEntered());
        if (this.classList.size() <= 1) {
            this.btnConfirm.setVisibility(View.GONE);
        } else {
            this.btnConfirm.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        SharedPreferences preferences = requireActivity().getSharedPreferences("classesData", 0);

        List<String> classNames = new ArrayList<>(Objects.requireNonNull(preferences.getStringSet("classSet", null)));

        // Clear existing classList before adding new EditText elements
        classList.clear();
        classList.add(etClass0);
        etClass0.setText(classNames.get(0));
        for (int i = 0; i < classNames.size(); i++) {
            createNewEditText();
        }
    }


    private class onEntered implements TextView.OnEditorActionListener {

        public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
            //Provjera postoji li predmet istog imena
            for (EditText item : classList) {
                if (item.getText().toString().trim().equalsIgnoreCase(classList.get(classList.size() - 1).getText().toString().trim()) && item != classList.get(classList.size() - 1)) {
                    Toast.makeText(requireActivity().getApplicationContext(), "Class already exists!", Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
            //Provjera je li editText prazan
            if (TextUtils.isEmpty(classList.get(classList.size() - 1).getText().toString()) || actionId != 6) {
                return false;
            }
            classList.get(classList.size() - 1).setOnEditorActionListener(null);
            createNewEditText();
            classList.get(classList.size() - 1).requestFocus();
            return true;
        }
    }
}