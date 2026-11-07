package com.example.parkin.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.parkin.R;
import com.example.parkin.database.DatabaseHelper;
import com.example.parkin.models.User;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize SQLite database and seed default tables/records
        DatabaseHelper.getInstance(this);

        etUsername = findViewById(R.id.etLoginUsername);
        etPassword = findViewById(R.id.etLoginPassword);

        // Autofill Demo Buttons
        findViewById(R.id.btnDemoCustomer).setOnClickListener(v -> {
            etUsername.setText("alex");
            etPassword.setText("alex123");
        });

        findViewById(R.id.btnDemoAdmin).setOnClickListener(v -> {
            etUsername.setText("admin");
            etPassword.setText("admin123");
        });

        // Sign In Action
        findViewById(R.id.btnSignIn).setOnClickListener(v -> handleLogin());

        // Open Registration Screen
        findViewById(R.id.tvRegisterNow).setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void handleLogin() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter your username and password", Toast.LENGTH_SHORT).show();
            return;
        }

        // Authenticate directly against persistent SQLite DB
        User matchedUser = DatabaseHelper.getInstance(this).authenticateUser(username, password);

        if (matchedUser != null) {
            Intent intent;
            if ("ADMIN".equalsIgnoreCase(matchedUser.getRole())) {
                // Route Administrator to the Command Center
                intent = new Intent(this, AdminPortalActivity.class);
            } else {
                // Route Customer to the Customer Portal
                intent = new Intent(this, CustomerPortalActivity.class);
            }
            intent.putExtra("USER_ID", matchedUser.getUserId());
            intent.putExtra("USER_NAME", matchedUser.getFullName());
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Invalid credentials. Try demo 'alex' or register a new user.", Toast.LENGTH_SHORT).show();
        }
    }
}