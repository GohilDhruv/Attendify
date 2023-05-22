package com.example.myapp;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class ClickedButtonsActivity extends AppCompatActivity {
    ArrayList<String> clickedNumbers = new ArrayList<>();
   static ArrayList<String> getEnrollment = new ArrayList<>();
   static ArrayList<String> getAttendance = new ArrayList<>();
    ArrayList<String> parentKeysList = new ArrayList<>();
    DbActivity dbActivity;
    int numberOfNodes;
    static String childNodeName,parentNodeName,date;
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this,DbActivity.class);
        startActivity(intent);
        finish();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clicked);
        dbActivity = new DbActivity();
        Intent intent = getIntent();
        // declare as class-level variable
        parentKeysList = intent.getStringArrayListExtra("parentKeysList");

        clickedNumbers = getIntent().getStringArrayListExtra("clickedNumbers");
        Collections.sort(clickedNumbers); // Sort the list in ascending order

        // Find full number for each four-digit number in clickedNumbers
        for (int i = 0; i < clickedNumbers.size(); i++) {
            String fourDigitNumber = clickedNumbers.get(i);
            if (fourDigitNumber.length() == 4) {
                // Look for full number in parentKeysList that ends with the four-digit number
                for (String fullNumber : parentKeysList) {
                    if (fullNumber.endsWith(fourDigitNumber)) {
                        clickedNumbers.set(i, fullNumber); // Replace four-digit number with full number
                        break;
                    }
                }
            }
        }
        // Update the ListView adapter with the new data
        ListView clickedNumbersListView = findViewById(R.id.clicked_numbers_listview);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(ClickedButtonsActivity.this,
                android.R.layout.simple_list_item_1, android.R.id.text1, clickedNumbers);
        clickedNumbersListView.setAdapter(adapter);
        recordAttendance();
    }
    private void recordAttendance() {
        String division = DbActivity.division;

        // Get the database reference
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();
        // Declare the child node name and its key-value pair
        childNodeName = new SimpleDateFormat("dd-MM-yyyy (hh:mm:ss a)", Locale.getDefault()).format(new Date());
        date = childNodeName.substring(0,5);
        parentNodeName = DbActivity.division + "-" + DbActivity.selectedSubject + "-" + DbActivity.selectedFaculty;
        // Create a new child node in the "Attendance" node with the child node name
        DatabaseReference attendanceRef = databaseRef.child("Faculty").child(DbActivity.selectedFaculty).child("Attendance").child(parentNodeName).child(childNodeName);
        DatabaseReference attendanceNode = databaseRef.child("Faculty").child(DbActivity.selectedFaculty).child("Attendance").child(parentNodeName);

        // Add "true" to the clicked numbers
        for (String number : clickedNumbers) {
            attendanceRef.child(number).setValue(true);
        }

        // Set all remaining numbers to "false"
        databaseRef.child("Students").child("Division").child(division).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    String number = childSnapshot.getKey();
                    if (!clickedNumbers.contains(number)) {
                        attendanceRef.child(number).setValue(false);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ClickedButtonsActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        // Count the number of nodes inside attendanceRef
        attendanceNode.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                numberOfNodes = (int) snapshot.getChildrenCount();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ClickedButtonsActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


}
