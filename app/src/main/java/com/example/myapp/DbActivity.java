package com.example.myapp;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DbActivity extends AppCompatActivity {
    public static String division = "",parentNode;
    public static String selectedFaculty,selectedSubject;
    static ArrayList<String> parentKeysList = new ArrayList<>();
    private ArrayList<String> facultyList = new ArrayList<>();
    Spinner subjectSpinner,facultySpinner;
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(DbActivity.this,StartActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.db_layout);
        subjectSpinner = findViewById(R.id.subjectSpinner);
        facultySpinner = findViewById(R.id.facultySpinner);
        facultyList = FireBaseActivity.facultyList;
        addFaculty();

        Button divisionA = findViewById(R.id.divisionAButton);
        Button divisionB = findViewById(R.id.divisionBButton);
        Button divisionC = findViewById(R.id.divisionCButton);


        divisionA.setOnClickListener(v -> {
            division = "A";
            fetchParentKeys();
        });
        divisionB.setOnClickListener(v -> {
            division = "B";
            fetchParentKeys();
        });
        divisionC.setOnClickListener(v -> {
            division = "C";
            fetchParentKeys();
        });
    }

    private void addFaculty() {
        facultySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Store the selected faculty in a variable
                selectedFaculty = StartActivity.selectedFaculty;
                updateSubjectList();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });


        // Add an empty item to the beginning of the list
        List<String> facultyListArray = new ArrayList<>();
        facultyListArray.add("Choose Faculty");
        facultyListArray.addAll(facultyList);

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, facultyListArray);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Set the spinner text color to white
        facultySpinner.setForegroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, android.R.color.white)));
        facultySpinner.setAdapter(dataAdapter);
        addSubject();
    }
    private void addSubject() {
        subjectSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Store the selected subject in a variable
                selectedSubject = (String) parent.getItemAtPosition(position);

                // Call the method to update the subject list
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

    }
    private void updateSubjectList() {
        // If selectedFaculty is null or "Choose Faculty", do nothing
        if (selectedFaculty == null || selectedFaculty.equals("Choose Faculty")) {
            subjectSpinner.setVisibility(View.INVISIBLE);
            return;
        }

        // Add the appropriate subjects based on the selected faculty
        List<String> subjectListArray = new ArrayList<>();
        subjectListArray.add("Choose Subject");

        switch (selectedFaculty) {
            case "ASD":
            case "VHP":
                selectedSubject = "A-Java";
                subjectListArray.set(0, selectedSubject);
                break;
            case "RPJ":
            case "NDS":
                selectedSubject = "PHP";
                subjectListArray.set(0, selectedSubject);
                break;
            case "ANM":
            case "MAJ":
                selectedSubject = "AAP";
                subjectListArray.set(0, selectedSubject);
                break;
            default:
                selectedSubject = "WNS";
                subjectListArray.set(0, selectedSubject);
                break;
        }
        parentNode = DbActivity.division + "-" + DbActivity.selectedSubject + "-" + DbActivity.selectedFaculty;

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, subjectListArray);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        subjectSpinner.setAdapter(dataAdapter);
    }
    private void fetchParentKeys() {

        DatabaseReference parentRef = FirebaseDatabase.getInstance().getReference().child("Students").child("Division").child(division);

        parentRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                parentKeysList = new ArrayList<>();
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    String key = childSnapshot.getKey();
                    parentKeysList.add(key);
                }
                if (selectedFaculty.equals("Choose Faculty")) {
                    Toast.makeText(DbActivity.this, "Please select a faculty", Toast.LENGTH_SHORT).show();
                }

                else {
                    Intent intent = new Intent(DbActivity.this, MainActivity.class);
                    intent.putStringArrayListExtra("parentKeysList", parentKeysList);
                    startActivity(intent);
                    finish();
                    showLoadingScreen();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors here
                Toast.makeText(DbActivity.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void showLoadingScreen() {
        View loadingView = LayoutInflater.from(this).inflate(R.layout.activity_loading, null);
        loadingView.setBackgroundColor(Color.TRANSPARENT); // Use a transparent background for the loading screen
        setContentView(loadingView);
    }

}
