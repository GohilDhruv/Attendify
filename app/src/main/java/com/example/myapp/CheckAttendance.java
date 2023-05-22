package com.example.myapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CheckAttendance extends AppCompatActivity {
    String studentId,selectedFaculty,selectedDivision;
    Spinner divisionSpinner;
    static ArrayList<String> parentKeysList = new ArrayList<>();
    DatabaseReference divRef;
    AutoCompleteTextView studentList;
    private final ArrayList<String> divisionList = new ArrayList<>();

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(CheckAttendance.this,StartActivity.class);
        startActivity(intent);
        finish();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_attendance);
        divisionSpinner = findViewById(R.id.lectureSpinner);
        studentList = findViewById(R.id.studentSpinner);
        selectedFaculty = StartActivity.selectedFaculty;
        addDiv();
    }
    private void addDiv() {
        divRef = FirebaseDatabase.getInstance().getReference().child("Faculty").child(selectedFaculty).child("Attendance");
        divRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    String key = childSnapshot.getKey();
                    divisionList.add(key);
                }
                if (!divisionList.isEmpty()) {
                    ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(CheckAttendance.this, android.R.layout.simple_spinner_item, divisionList);
                    dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    divisionSpinner.setVisibility(View.VISIBLE);
                    divisionSpinner.setAdapter(dataAdapter);
                } else {
                    List<String> divisionListArray = new ArrayList<>();
                    divisionListArray.add("No Attendance Available");

                    ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(CheckAttendance.this, android.R.layout.simple_spinner_item, divisionListArray);
                    dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    divisionSpinner.setAdapter(dataAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors here
                Toast.makeText(CheckAttendance.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        divisionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Store the selected subject in a variable
                selectedDivision = (String) parent.getItemAtPosition(position);
                addStudent();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

    }

    private void addStudent() {
        DatabaseReference parentRef = FirebaseDatabase.getInstance().getReference().child("Students").child("Division").child(selectedDivision.substring(0,1));

        parentRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                parentKeysList = new ArrayList<>();
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    String key = childSnapshot.getKey();
                    parentKeysList.add(key);
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(CheckAttendance.this, android.R.layout.simple_dropdown_item_1line, parentKeysList);
                studentList.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CheckAttendance.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkAttendance() {
        studentId = studentList.getText().toString();
        if (!studentId.isEmpty()) {
            DatabaseReference database = FirebaseDatabase.getInstance().getReference().child("Faculty/" + selectedFaculty + "/Attendance");
            DatabaseReference ref = database.child(selectedDivision);
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    int totalLectures = (int) dataSnapshot.getChildrenCount();
                    int attendedLectures = 0;

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Boolean attended = snapshot.child(studentId).getValue(Boolean.class);
                        if (attended != null && attended) {
                            attendedLectures++;
                        }
                    }

                    // show the attendance details in the dialog box
                    showAttendanceDialog(studentList.getText().toString(), attendedLectures, totalLectures);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle the error
                }
            });
        } else {
            // show error message
            Toast.makeText(CheckAttendance.this, "Please select a student", Toast.LENGTH_SHORT).show();
        }
    }

    private void showAttendanceDialog(String studentName, int attendedLectures, int totalLectures) {
        // create the dialog box
        Dialog dialog = new Dialog(CheckAttendance.this);
        dialog.setContentView(R.layout.attendance_dialog);

        // set the student name
        TextView studentNameTextView = dialog.findViewById(R.id.studentNameTextView);
        studentNameTextView.setText(studentName);

        // set the attendance details
        TextView attendanceTextView = dialog.findViewById(R.id.attendanceTextView);
        String attendanceText = "Attended " + attendedLectures + " out of " + totalLectures + " lectures.";
        attendanceTextView.setText(attendanceText);

        // set the close button
        Button closeButton = dialog.findViewById(R.id.closeButton);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        // show the dialog box
        dialog.show();
    }


    public void attendance(View view) {
        checkAttendance();
    }

}