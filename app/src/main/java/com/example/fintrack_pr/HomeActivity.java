package com.example.fintrack_pr;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class HomeActivity extends AppCompatActivity {

    private TextView tvWelcome;
    private Button btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        tvWelcome = findViewById(R.id.tv_welcome);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {

            String username = getIntent().getStringExtra("username");
            if (username != null) {
                tvWelcome.setText("Welcome, " + username + "!");
            } else {
                retrieveUsernameFromFirestore(currentUser);
            }
        } else {
            tvWelcome.setText("Welcome!");
        }
    }


    private void retrieveUsernameFromFirestore(FirebaseUser currentUser) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(currentUser.getUid()).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            String username = document.getString("username");
                            tvWelcome.setText("Welcome, " + username + "!");
                        } else {
                            tvWelcome.setText("Welcome!");
                        }
                    } else {
                        tvWelcome.setText("Welcome!");
                    }
                });
    }
}
