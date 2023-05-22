package com.example.myapp;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class CreateExcel extends AppCompatActivity {
    private ArrayList<String> facultyList = new ArrayList<>();
    private final ArrayList<String> divisionList = new ArrayList<>();
    private final ArrayList<String> attendanceList = new ArrayList<>();
    TreeMap<String, List<String>> attendanceMap = new TreeMap<>();
    Spinner divisionSpinner, facultySpinner;
    DatabaseReference divRef;
    public static String selectedFaculty, selectedDivision, division,folderName,fileName;
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(CreateExcel.this,StartActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_excel);
        divisionSpinner = findViewById(R.id.divisionSpinner);
        facultySpinner = findViewById(R.id.facultySpinner);
        facultyList = FireBaseActivity.facultyList;
        Button download = findViewById(R.id.downloadButton);
        addFaculty();
        download.setOnClickListener(view -> {
            showLoadingScreen();
            button();
        });
    }

    private void addFaculty() {
        facultySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Store the selected faculty in a variable
                selectedFaculty = StartActivity.selectedFaculty;
                divisionList.clear();
                addDiv();
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
                    ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(CreateExcel.this, android.R.layout.simple_spinner_item, divisionList);
                    dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    divisionSpinner.setVisibility(View.VISIBLE);
                    divisionSpinner.setAdapter(dataAdapter);
                } else {
                    List<String> divisionListArray = new ArrayList<>();
                    divisionListArray.add("No Attendance Available");

                    ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(CreateExcel.this, android.R.layout.simple_spinner_item, divisionListArray);
                    dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    divisionSpinner.setAdapter(dataAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors here
                Toast.makeText(CreateExcel.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        divisionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Store the selected subject in a variable
                selectedDivision = (String) parent.getItemAtPosition(position);
                attendanceList.clear();
                //List of taken Attendance

                divRef.child(selectedDivision).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                            String key = childSnapshot.getKey();
                            attendanceList.add(key);
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle errors here
                        Toast.makeText(CreateExcel.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });


            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

    }

    private void button() {
        if (selectedFaculty.equals("Choose Faculty")) {
            Toast.makeText(this, "Please select a Faculty", Toast.LENGTH_SHORT).show();
        } else if (selectedDivision.equals("No Attendance Available")) {
            Toast.makeText(this, "No Attendance Available!! Please take attendance first", Toast.LENGTH_SHORT).show();
        } else {
            getAttendance();
        }
    }

    private void getAttendance() {
        DatabaseReference attendanceRef = divRef.child(selectedDivision);
        for (String nodeName : attendanceList) {
            DatabaseReference nodeRef = attendanceRef.child(nodeName);
            nodeRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    List<String> attendanceList = new ArrayList<String>();
                    for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                        Object value = childSnapshot.getValue();
                        String attendance = "";
                        if (value instanceof Boolean) {
                            Boolean isPresent = (Boolean) value;
                            attendance = isPresent ? "P" : "A"; // set attendance as 'P' if true, else 'A'
                        }
                        attendanceList.add(attendance); // add the attendance value for the node to the list
                    }
                    attendanceMap.put(nodeName, attendanceList); // store the list of attendance values for the node in the HashMap
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(CreateExcel.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
        Collections.sort(attendanceList);
        division = selectedDivision.substring(0,1);
        StorageReference storage = FirebaseStorage.getInstance().getReference().child(selectedFaculty).child(division+".xlsx");

        // Code to execute on failure
        storage.getBytes(Long.MAX_VALUE).addOnSuccessListener(bytes -> {
            try {
                InputStream inputStream = new ByteArrayInputStream(bytes);
                XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
                XSSFSheet sheet = workbook.getSheetAt(0);
                int rowno = 0;

                // create the header row with all the keys starting from the 4th column
                XSSFRow headerRow = sheet.getRow(rowno++);
                if (headerRow == null) {
                    headerRow = sheet.createRow(rowno++);
                }
                headerRow.setHeight((short) 800); //row height 40 (40 * 20)
                int columnIndex = 3; // start from the 4th column
                for (Map.Entry<String, List<String>> entry : attendanceMap.entrySet()) {
                    CellStyle style = workbook.createCellStyle();
                    style.setRotation((short) 180);
                    style.setVerticalAlignment(VerticalAlignment.CENTER);
                    style.setBorderTop(BorderStyle.THIN);
                    style.setBorderBottom(BorderStyle.THIN);
                    style.setBorderLeft(BorderStyle.THIN);
                    style.setBorderRight(BorderStyle.THIN);
                    String nodeName = entry.getKey().substring(0, 5);
                    XSSFCell headerCell = headerRow.createCell(columnIndex++);
                    headerCell.setCellValue(nodeName);
                    headerCell.setCellStyle(style);
                    sheet.setColumnWidth(columnIndex, 3 * 256); // 3 characters wide
                }

                // iterate over the values of each key and write them in columns starting from the 4th column
                for (int i = 0; i < attendanceMap.values().stream().mapToInt(List::size).max().orElse(0); i++) {
                    XSSFRow row = sheet.getRow(rowno++);
                    if (row == null) {
                        row = sheet.createRow(rowno++);
                    }
                    columnIndex = 3; // start from the 4th column
                    sheet.setColumnWidth(columnIndex, 3 * 256); // 3 characters wide
                    for (List<String> attendanceList : attendanceMap.values()) {
                        if (i < attendanceList.size()) {
                            String attendance = attendanceList.get(i);
                            XSSFCell cell = row.createCell(columnIndex++);
                            if (attendance != null) {
                                cell.setCellValue(attendance);
                            } else {
                                cell.setCellValue("");
                            }
                            CellStyle style = workbook.createCellStyle();
                            style.setAlignment(HorizontalAlignment.CENTER);
                            style.setBorderTop(BorderStyle.THIN);
                            style.setBorderBottom(BorderStyle.THIN);
                            style.setBorderLeft(BorderStyle.THIN);
                            style.setBorderRight(BorderStyle.THIN);
                            cell.setCellStyle(style);
                        } else {
                            row.createCell(columnIndex++).setCellValue("");
                        }
                    }
                }

                folderName = "Attendance";
                fileName = selectedDivision + ".xlsx";

                // create the folder if it doesn't exist
                File folder = new File(Environment.getExternalStorageDirectory(), folderName);
                if (!folder.exists()) {
                    folder.mkdir();
                }

                // create the Excel file
                File file = new File(folder, fileName);
                FileOutputStream fileOutputStream = new FileOutputStream(file);

                workbook.write(fileOutputStream);
                fileOutputStream.close();

                Toast.makeText(CreateExcel.this, "Excel file created successfully", Toast.LENGTH_SHORT).show();
                attendanceMap.clear();

            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }).addOnFailureListener(Throwable::printStackTrace);

    }
    public void showLoadingScreen() {
        View loadingView = LayoutInflater.from(this).inflate(R.layout.activity_loading, null);
        loadingView.setBackgroundColor(Color.TRANSPARENT); // Use a transparent background for the loading screen
        setContentView(loadingView);
        finish();
    }
}