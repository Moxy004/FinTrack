package com.example.fintrack_pr;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.auth.User;

import java.util.HashMap;

public class RegisterForm extends AppCompatActivity {

    private EditText etUsername, etEmail, etPassword, etConfirmPassword;
    private Button btnRegister;
    private TextView tvLogin;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_form);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etUsername = findViewById(R.id.et_username);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        btnRegister = findViewById(R.id.btn_register);
        tvLogin = findViewById(R.id.tv_login);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });

        tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterForm.this, LoginForm.class));
                finish();
            }
        });
    }

    private void registerUser() {
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(username)) {
            showError(etUsername, "Username is required");
            return;
        }
        if (TextUtils.isEmpty(email)) {
            showError(etEmail, "Email is required");
            return;
        }
        if (!isValidEmail(email)) {
            showError(etEmail, "Invalid email format");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            showError(etPassword, "Password is required");
            return;
        }
        if (!password.equals(confirmPassword)) {
            showError(etConfirmPassword, "Passwords do not match");
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseAuth userAuth = FirebaseAuth.getInstance();
                        userAuth.getCurrentUser().sendEmailVerification()
                                .addOnCompleteListener(verificationTask -> {
                                    if (verificationTask.isSuccessful()) {
                                        String userId = mAuth.getCurrentUser().getUid();
                                        db.collection("users").document(userId)
                                                .set(new HashMap<String, Object>() {{
                                                    put("username", username);
                                                    put("email", email);
                                                }})
                                                .addOnSuccessListener(aVoid -> {
                                                    Toast.makeText(RegisterForm.this,
                                                            "Registration successful! Please check your email to verify.",
                                                            Toast.LENGTH_SHORT).show();
                                                    startActivity(new Intent(RegisterForm.this, LoginForm.class));
                                                    finish();
                                                })
                                                .addOnFailureListener(e -> {
                                                    Toast.makeText(RegisterForm.this,
                                                            "Registration failed: " + e.getMessage(),
                                                            Toast.LENGTH_SHORT).show();
                                                });
                                    } else {
                                        Toast.makeText(RegisterForm.this,
                                                "Failed to send verification email: " + verificationTask.getException().getMessage(),
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Toast.makeText(RegisterForm.this,
                                "Registration failed: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean isValidEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void showError(EditText editText, String message) {
        editText.setError(message);
        editText.setBackgroundResource(R.drawable.edittext_background);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
