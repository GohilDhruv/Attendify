package com.example.myapp;


import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class register_activity extends AppCompatActivity {
    private EditText emailEditText,passwordEditText,nameEditText,subjectEditText;

    private Button registerButton;
    private TextView LoginTextView;
    private FirebaseAuth mAuth;
    static String email, faculty,name,password;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        registerButton = findViewById(R.id.registerButton);
        LoginTextView = findViewById(R.id.loginTextView);
        nameEditText=findViewById(R.id.nameEditText);
        subjectEditText=findViewById(R.id.subjectEditText);

        mAuth = FirebaseAuth.getInstance();
        registerButton.setOnClickListener(view -> {

            email = emailEditText.getText().toString();
            password = passwordEditText.getText().toString();
            name = nameEditText.getText().toString();
            faculty = subjectEditText.getText().toString();

            if (TextUtils.isEmpty(email) && TextUtils.isEmpty(password) && TextUtils.isEmpty(name) && TextUtils.isEmpty(faculty)) {
                Toast.makeText(register_activity.this, "All fields are required", Toast.LENGTH_SHORT).show();
                emailEditText.setError("Email is required");
                emailEditText.requestFocus();
                passwordEditText.setError("Password is required");
                passwordEditText.requestFocus();
                nameEditText.setError("Name is required");
                nameEditText.requestFocus();
                subjectEditText.setError("Faculty code is required");
                subjectEditText.requestFocus();
                return;
            }

            if (TextUtils.isEmpty(email)) {
                emailEditText.setError("Email is required");
                emailEditText.requestFocus();
                return;
            }

            if (TextUtils.isEmpty(password)) {
                passwordEditText.setError("Password is required");
                passwordEditText.requestFocus();
                return;
            }

            if (password.length() < 8) {
                passwordEditText.setError("Password must be at least 8 characters long");
                passwordEditText.requestFocus();
                return;
            }

            if (TextUtils.isEmpty(name)) {
                nameEditText.setError("Name is required");
                nameEditText.requestFocus();
                return;
            }

            if (TextUtils.isEmpty(faculty)) {
                subjectEditText.setError("Faculty code is required");
                subjectEditText.requestFocus();
                return;
            }

            showLoadingScreen();

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(register_activity.this, task -> {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            emailEditText.setText("");
                            passwordEditText.setText("");
                            nameEditText.setText("");
                            subjectEditText.setText("");

                            // Get the user ID
                            FirebaseUser user = mAuth.getCurrentUser();
                            String userId = user.getUid();

                            // Store the user's name and subject in the Firebase Realtime Database
                            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users").child(userId);
                            userRef.child("name").setValue(name);
                            userRef.child("faculty").setValue(faculty);
                            userRef.child("email").setValue(email);
                            Toast.makeText(register_activity.this, "Registration successful", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(register_activity.this, LoginActivity.class);
                            startActivity(intent);
                        } else {
                            // If sign in fails, display a message to the user.

                            Exception e = task.getException();
                            if (e != null) {
                                Log.e("Registration Error", e.getMessage());
                                Toast.makeText(register_activity.this, "Registration failed" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                System.out.println(e.getMessage());
                            }
                        }
                    });
        });



        LoginTextView.setOnClickListener(view -> {
            Intent intent = new Intent(register_activity.this, LoginActivity.class);
            startActivity(intent);
        });
    }
    public void showLoadingScreen() {
        View loadingView = LayoutInflater.from(this).inflate(R.layout.activity_loading, null);
        loadingView.setBackgroundColor(Color.TRANSPARENT); // Use a transparent background for the loading screen
        setContentView(loadingView);
    }
}