package com.example.fintrack_pr;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginForm extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView btnRegister, btnForgotPassword;
    private CheckBox cbRememberMe;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_form);



        initializeFirebase();
        initializeViews();
        checkForSavedCredentials();
        setUpListeners();
    }

    private void initializeFirebase() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    private void initializeViews() {
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        btnRegister = findViewById(R.id.tv_register);
        btnForgotPassword = findViewById(R.id.tv_forgot_password);
        cbRememberMe = findViewById(R.id.cb_remember_me);
    }

    private void checkForSavedCredentials() {
        SharedPreferences sharedPreferences = getSharedPreferences("FinTrackPrefs", MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean("is_logged_in", false);

        if (isLoggedIn) {
            String email = sharedPreferences.getString("user_email", null);
            String password = sharedPreferences.getString("user_password", null);
            if (email != null && password != null) {
                loginUserWithCredentials(email, password);
            }
        }
    }

    private void setUpListeners() {
        btnLogin.setOnClickListener(v -> attemptLogin());
        btnRegister.setOnClickListener(v -> navigateToRegister());
        btnForgotPassword.setOnClickListener(v -> navigateToForgotPassword());
    }

    private void attemptLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (!validateInputs(email, password)) return;

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null && user.isEmailVerified()) {
                    handleSuccessfulLogin(email, password, user);
                } else {
                    showToast("Please verify your email first");
                }
            } else {
                showToast("Login failed: " + getErrorMessage(task));
            }
        });
    }

    private void handleSuccessfulLogin(String email, String password, FirebaseUser user) {
        if (cbRememberMe.isChecked()) {
            saveUserCredentials(email, password);
        }

        db.collection("users").document(user.getUid()).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document != null && document.exists()) {
                    navigateToHome(document.getString("username"));
                } else {
                    showToast("User data not found");
                }
            } else {
                showToast("Error fetching user data: " + getErrorMessage(task));
            }
        });
    }

    private void loginUserWithCredentials(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    fetchUserDataAndNavigate(user);
                }
            } else {
                showToast("Login failed: " + getErrorMessage(task));
            }
        });
    }

    private void fetchUserDataAndNavigate(FirebaseUser user) {
        db.collection("users").document(user.getUid()).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document != null && document.exists()) {
                    navigateToHome(document.getString("username"));
                } else {
                    showToast("User data not found");
                }
            } else {
                showToast("Error fetching user data: " + getErrorMessage(task));
            }
        });
    }

    private void saveUserCredentials(String email, String password) {
        SharedPreferences sharedPreferences = getSharedPreferences("FinTrackPrefs", MODE_PRIVATE);
        sharedPreferences.edit()
                .putString("user_email", email)
                .putString("user_password", password)
                .putBoolean("is_logged_in", true)
                .apply();
    }

    private boolean validateInputs(String email, String password) {
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            showError(etEmail, "Email is required");
            showError(etPassword, "Password is required");
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError(etEmail, "Invalid email format");
            return false;
        }
        return true;
    }

    private void showError(EditText editText, String message) {
        editText.setError(message);
        editText.setBackgroundResource(R.drawable.edittext_background);
        showToast(message);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private String getErrorMessage(@NonNull Task<?> task) {
        return task.getException() != null ? task.getException().getMessage() : "Unknown error";
    }

    private void navigateToHome(String username) {
        Intent intent = new Intent(LoginForm.this, HomeActivity.class);
        intent.putExtra("username", username);
        startActivity(intent);
        finish();
    }

    private void navigateToRegister() {
        startActivity(new Intent(LoginForm.this, RegisterForm.class));
    }

    private void navigateToForgotPassword() {
        startActivity(new Intent(LoginForm.this, ForgotPasswordActivity.class));
    }
}
