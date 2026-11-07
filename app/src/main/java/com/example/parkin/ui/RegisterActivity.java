package com.example.parkin.ui;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.parkin.R;
import com.example.parkin.database.DatabaseHelper;
import com.example.parkin.models.User;

public class RegisterActivity extends AppCompatActivity {

    private EditText etName, etUsername, etEmail, etPassword, etPhone, etPlate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etName = findViewById(R.id.etRegFullName);
        etUsername = findViewById(R.id.etRegUsername);
        etEmail = findViewById(R.id.etRegEmail);
        etPassword = findViewById(R.id.etRegPassword);
        etPhone = findViewById(R.id.etRegPhone);
        etPlate = findViewById(R.id.etRegPlate);

        findViewById(R.id.btnSubmitRegistration).setOnClickListener(v -> performRegistration());
        findViewById(R.id.btnBackToLogin).setOnClickListener(v -> finish());
    }

    private void performRegistration() {
        String name = etName.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String plate = etPlate.getText().toString().trim();

        if (name.isEmpty() || username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isValidPassword(password)) {
            Toast.makeText(this, "Password must be at least 8 characters long and contain uppercase, lowercase, a digit, and a special character.", Toast.LENGTH_LONG).show();
            return;
        }

        User newUser = new User(0, name, email, username, password, phone, plate, "CUSTOMER", "ACTIVE");
        boolean success = DatabaseHelper.getInstance(this).registerUser(newUser);

        if (success) {
            Toast.makeText(this, "Registration Successful! Please Sign In.", Toast.LENGTH_LONG).show();
            finish(); // Returns to Login screen
        } else {
            Toast.makeText(this, "Username or Email already exists!", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isValidPassword(String password) {
        if (password == null || password.length() < 8) return false;
        boolean hasUpper = false, hasLower = false, hasDigit = false, hasSpecial = false;
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isLowerCase(c)) hasLower = true;
            else if (Character.isDigit(c)) hasDigit = true;
            else hasSpecial = true;
        }
        return hasUpper && hasLower && hasDigit && hasSpecial;
    }
}