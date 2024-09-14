package com.example.myapp;

import static android.provider.Settings.*;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends AppCompatActivity {
    private LinearLayout mainLayout;
    public int arrayIndex;
    private String[] numberArray;
    private int currentIndex = 0;
    private final ArrayList<String> clickedNumbers = new ArrayList<>();
    private ArrayList<String> parentKeysList = new ArrayList<>(); // declare as class-level variable
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(MainActivity.this,DbActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        parentKeysList = intent.getStringArrayListExtra("parentKeysList");
        arrayIndex = parentKeysList.size();
        numberArray = new String[arrayIndex];

        // Calculate number of rows based on number array length
        int numRows = (int) Math.ceil((double) numberArray.length / 5);

        mainLayout = findViewById(R.id.main_layout);
        // Add rows of buttons
        for (int i = 0; i < numRows; i++) {
            mainLayout.addView(createRow());
        }
        Button submitButton = new Button(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(32, 16, 32, 16);
        submitButton.setLayoutParams(params);
        submitButton.setBackgroundResource(R.drawable.button_bg);
        submitButton.setText("Submit");
        submitButton.setOnClickListener(v -> goToClickedButtonsActivity());
        mainLayout.addView(submitButton);

    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private Button createButton() {
        Button button = new Button(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
        );
        params.setMargins(8, 0, 8, 0); // add margins
        button.setLayoutParams(params);
        button.setBackgroundResource(R.drawable.button_bg);
        String parentKey = parentKeysList.get(currentIndex);
        button.setText(parentKey.substring(parentKey.length() - 4)); // set button text to last 4 digits of parentKey
        currentIndex++;
        button.setOnClickListener(v -> {
            Button b = (Button) v;
            String buttonText = b.getText().toString();
            if (b.getBackground().getConstantState() == getResources().getDrawable(R.drawable.green_button).getConstantState()) {
                b.setBackgroundResource(R.drawable.red_button);
                clickedNumbers.remove(buttonText);
            } else {
                b.setBackgroundResource(R.drawable.green_button);
                clickedNumbers.add(buttonText);
            }
        });

        return button;
    }
    private LinearLayout createRow() {
        LinearLayout rowLayout = new LinearLayout(this);
        rowLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        rowLayout.setOrientation(LinearLayout.HORIZONTAL);
        rowLayout.setGravity(Gravity.CENTER_HORIZONTAL);
        rowLayout.setPadding(0, 10, 0, 10);

        // Add empty spaces to row
        int remainingButtons = numberArray.length - currentIndex;
        int numEmptySpaces = Math.max(5 - remainingButtons, 0);

        // Add buttons to row
        int numButtonsToAdd = Math.min(remainingButtons, 5);
        for (int i = 0; i < numButtonsToAdd; i++) {
            rowLayout.addView(createButton());
        }
        for (int i = 0; i < numEmptySpaces; i++) {
            View emptySpace = new View(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
            );
            params.setMargins(8, 0, 8, 0); // add margins
            emptySpace.setLayoutParams(params);
            rowLayout.addView(emptySpace);
        }

        return rowLayout;
    }
    public void goToClickedButtonsActivity() {
        showLoadingScreen();
        Collections.sort(clickedNumbers);
        Intent intent = new Intent(this, ClickedButtonsActivity.class);
        intent.putStringArrayListExtra("clickedNumbers", clickedNumbers);
        intent.putStringArrayListExtra("parentKeysList",parentKeysList);
        startActivity(intent);
        finish();

    }
    public void showLoadingScreen() {
        View loadingView = LayoutInflater.from(this).inflate(R.layout.activity_loading, null);
        loadingView.setBackgroundColor(Color.TRANSPARENT); // Use a transparent background for the loading screen
        setContentView(loadingView);
        finish();
    }

}
