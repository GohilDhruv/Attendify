package com.example.myapp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
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
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity {
    private EditText emailEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private TextView registerTextView;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        registerTextView = findViewById(R.id.registerTextView);

        mAuth = FirebaseAuth.getInstance();

        mAuth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() != null) {
                    // user is already logged in
                    Intent intent = new Intent(LoginActivity.this, FireBaseActivity.class);
                    startActivity(intent);
                    finish(); // finish the login activity so that user cannot go back to it using the back button
                }
            }
        });

        loginButton.setOnClickListener(view -> {

            String email = emailEditText.getText().toString();
            String password = passwordEditText.getText().toString();

            if (TextUtils.isEmpty(email) && TextUtils.isEmpty(password)) {

                emailEditText.setError("Email is required");
                emailEditText.requestFocus();
                passwordEditText.setError("Password is required");
                passwordEditText.requestFocus();
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




            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                showLoadingScreen();
                                // Sign in success, update UI with the signed-in user's information
                                Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(LoginActivity.this, FireBaseActivity.class);
                                startActivity(intent);
                                finish(); // finish the login activity so that user cannot go back to it using the back button
                            } else {
                                // If sign in fails, display a message to the user.
                                Toast.makeText(LoginActivity.this, "Authentication failed", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        });


        registerTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, register_activity.class);
                startActivity(intent);
            }
        });
    }
    public void showLoadingScreen() {
        View loadingView = LayoutInflater.from(this).inflate(R.layout.activity_loading, null);
        loadingView.setBackgroundColor(Color.TRANSPARENT); // Use a transparent background for the loading screen
        setContentView(loadingView);
    }

}