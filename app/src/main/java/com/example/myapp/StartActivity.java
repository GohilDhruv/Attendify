package com.example.myapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class StartActivity extends AppCompatActivity {
    static String selectedFaculty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String userId = mAuth.getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(userId);
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String faculty = dataSnapshot.child("faculty").getValue(String.class);
                    selectedFaculty = faculty;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w("Faculty Error", "Failed to read value.", databaseError.toException());
            }
        });
    }

    public void takeAttendance(View view) {
        showLoadingScreen();
        Intent intent = new Intent(StartActivity.this, DbActivity.class);
        startActivity(intent);
    }
    public void downloadAttendance(View view) {
        showLoadingScreen();
        Intent intent = new Intent(StartActivity.this, CreateExcel.class);
        startActivity(intent);
    }

    public void logoout(View view) {
        showLoadingScreen();
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    public void checkAttendance(View view) {
        showLoadingScreen();
        Intent intent = new Intent(StartActivity.this, CheckAttendance.class);
        startActivity(intent);
    }
    public void showLoadingScreen() {
        View loadingView = LayoutInflater.from(this).inflate(R.layout.activity_loading, null);
        loadingView.setBackgroundColor(Color.TRANSPARENT); // Use a transparent background for the loading screen
        setContentView(loadingView);
        finish();
    }
}