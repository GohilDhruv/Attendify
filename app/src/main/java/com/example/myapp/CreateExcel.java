package com.example.myapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;

public class CreateExcel extends AppCompatActivity {
    private ArrayList<String> facultyList = new ArrayList<>();
    private final ArrayList<String> divisionList = new ArrayList<>();
    private final ArrayList<String> attendanceList = new ArrayList<>();
    private LinkedHashMap<String, List<String>> attendanceMap = new LinkedHashMap<>();

    Spinner divisionSpinner, facultySpinner;
    DatabaseReference divRef;
    public static String selectedFaculty, selectedDivision, division, folderName, fileName;

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(CreateExcel.this, StartActivity.class);
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
            new Thread(() -> {
                getAttendance();
            }).start();
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
                // Store the selected division in a variable
                selectedDivision = (String) parent.getItemAtPosition(position);
                attendanceList.clear();

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

    public void getAttendance() {
        DatabaseReference attendanceRef = divRef.child(selectedDivision);

        // Initialize attendanceMap to be sure it's empty at the start
        attendanceMap.clear();

        // Counter for how many data retrievals are pending
        final CountDownLatch latch = new CountDownLatch(attendanceList.size());

        Collections.sort(attendanceList, new Comparator<String>() {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");

            @Override
            public int compare(String date1, String date2) {
                try {
                    Date d1 = sdf.parse(date1);
                    Date d2 = sdf.parse(date2);
                    return d1.compareTo(d2);
                } catch (ParseException e) {
                    throw new RuntimeException(e); // Handle parsing exceptions as needed
                }
            }
        });
        // Fetch attendance data
        for (String nodeName : attendanceList) {
            DatabaseReference nodeRef = attendanceRef.child(nodeName);
            nodeRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    List<String> attendanceData = new ArrayList<>();
                    for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                        Boolean isPresent = childSnapshot.getValue(Boolean.class);
                        attendanceData.add(isPresent != null ? (isPresent ? "P" : "A") : ""); // "P" or "A" based on presence
                    }
                    attendanceMap.put(nodeName, attendanceData); // store the attendance data

                    // Decrement the latch when data retrieval is complete
                    latch.countDown();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(CreateExcel.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    latch.countDown(); // Decrement the latch to avoid deadlock
                }
            });
        }

        // Wait until all Firebase data retrieval operations are finished
        try {
            latch.await(); // This blocks until the count of the latch reaches zero
            // All data is loaded, proceed to create and upload the Excel file
            createAndUploadExcelFile();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void createAndUploadExcelFile() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            // Fetch the existing workbook from the drawable resource
            InputStream inputStream = getResources().openRawResource(R.raw.b);  // assuming B.xlsx is placed in res/raw folder as b.xlsx
            XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
            XSSFSheet sheet = workbook.getSheetAt(0); // Get the first sheet of the workbook

            int rowno = 0; // Start from the first row
            int columnIndex;

            // Create or update the header row with all the keys starting from the 4th column
            XSSFRow headerRow = sheet.getRow(rowno); // Assuming headers are in the first row
            if (headerRow == null) {
                headerRow = sheet.createRow(rowno);
            }
            headerRow.setHeight((short) 800); // row height 40 (40 * 20)

            // Populate the header row (starting from the 4th column)
            columnIndex = 3; // Start from the 4th column (index 3)
            // Assuming attendanceMap is a LinkedHashMap<String, List<String>>
            List<String> sortedNodeNames = new ArrayList<>(attendanceMap.keySet());

// Sort the node names, assuming they are in the format "dd-MM-yyyy HH:mm:ss"
            Collections.sort(sortedNodeNames, (a, b) -> {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                try {
                    Date dateA = dateFormat.parse(a);
                    Date dateB = dateFormat.parse(b);
                    return dateA.compareTo(dateB);
                } catch (ParseException e) {
                    e.printStackTrace();
                    return 0; // If parsing fails, consider them equal
                }
            });

// Now populate the Excel sheet with sorted entries
            for (String nodeName : sortedNodeNames) {
                CellStyle style = workbook.createCellStyle();
                style.setRotation((short) 180);
                style.setVerticalAlignment(VerticalAlignment.CENTER);
                style.setBorderTop(BorderStyle.THIN);
                style.setBorderBottom(BorderStyle.THIN);
                style.setBorderLeft(BorderStyle.THIN);
                style.setBorderRight(BorderStyle.THIN);

                // Get the node name for the header (first 5 characters)
                String headerNodeName = nodeName.substring(0, 5); // Assuming you want the first 5 characters
                XSSFCell headerCell = headerRow.createCell(columnIndex);
                headerCell.setCellValue(headerNodeName);
                headerCell.setCellStyle(style);
                sheet.setColumnWidth(columnIndex, 3 * 256); // Set column width to 3 characters wide
                columnIndex++; // Move to the next column
            }



            // Start filling attendance data into rows (starting from rowno = 1)
            rowno++; // Skip the header row (row 0)
            int maxAttendanceSize = attendanceMap.values().stream()
                    .mapToInt(List::size)
                    .max()
                    .orElse(0);

            // Iterate over attendance values and write them in the sheet
            for (int i = 0; i < maxAttendanceSize; i++) {
                XSSFRow row = sheet.getRow(rowno);
                if (row == null) {
                    row = sheet.createRow(rowno); // Create a new row if not existing
                }

                columnIndex = 3; // Reset to the 4th column for each row
                for (List<String> attendanceList : attendanceMap.values()) {
                    XSSFCell cell = row.getCell(columnIndex);
                    if (cell == null) {
                        cell = row.createCell(columnIndex); // Create a cell if it doesn't exist
                    }

                    // Fill attendance data
                    if (i < attendanceList.size()) {
                        String attendance = attendanceList.get(i);
                        cell.setCellValue(attendance != null ? attendance : "");
                    } else {
                        cell.setCellValue(""); // Empty cell if no data
                    }

                    // Set cell style
                    CellStyle style = workbook.createCellStyle();
                    style.setAlignment(HorizontalAlignment.CENTER);
                    style.setBorderTop(BorderStyle.THIN);
                    style.setBorderBottom(BorderStyle.THIN);
                    style.setBorderLeft(BorderStyle.THIN);
                    style.setBorderRight(BorderStyle.THIN);
                    cell.setCellStyle(style);

                    columnIndex++; // Move to the next column
                }

                rowno++; // Move to the next row
            }

            // Write the workbook into the ByteArrayOutputStream
            workbook.write(baos);
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(CreateExcel.this, "Failed to create Excel file", Toast.LENGTH_SHORT).show();
            return;
        }

        // Upload the Excel file to Firebase Storage
        byte[] fileBytes = baos.toByteArray();
        String fileName = selectedDivision + ".xlsx";
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("attendance").child(fileName);
        UploadTask uploadTask = storageRef.putBytes(fileBytes);

        uploadTask.addOnSuccessListener(taskSnapshot -> {
            Toast.makeText(CreateExcel.this, "Excel file uploaded successfully", Toast.LENGTH_SHORT).show();
            attendanceMap.clear();
        }).addOnFailureListener(exception -> {
            Toast.makeText(CreateExcel.this, "Failed to upload file: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }




    public void showLoadingScreen() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false); // Prevents dismissal
        progressDialog.show();

        // Optional: Dismiss the progress dialog after a certain timeout to avoid deadlocks
        new Handler().postDelayed(() -> {
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        }, 10000); // 10 seconds timeout
    }
}
