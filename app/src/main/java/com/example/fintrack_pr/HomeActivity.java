package com.example.fintrack_pr;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class HomeActivity extends AppCompatActivity {

    private TextView tvWelcome;
    private Button btn_notes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize the views
        Log.d("HomeActivity", "Before findViewById");
        btn_notes = findViewById(R.id.btn_notes);
        tvWelcome = findViewById(R.id.tv_welcome);
        Log.d("HomeActivity", "After findViewById");


        // Get current user from FirebaseAuth
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            // Try to get username from the Intent
            String username = getIntent().getStringExtra("username");

            if (username != null) {
                // If username is passed via Intent, display it
                tvWelcome.setText("Welcome, " + username + "!");
            } else {
                // If username is not passed, fetch it from Firestore
                retrieveUsernameFromFirestore(currentUser);
            }
        } else {
            // If no user is logged in, show the default message
            tvWelcome.setText("Welcome!");
        }

        // Set up the notes button click listener
        btn_notes.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, NotesActivity.class);
            startActivity(intent);
        });
    }

    private void retrieveUsernameFromFirestore(FirebaseUser currentUser) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // Fetch the user document from Firestore
        db.collection("users").document(currentUser.getUid()).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            // Get the username from the Firestore document
                            String username = document.getString("username");
                            if (username != null) {
                                tvWelcome.setText("Welcome, " + username + "!");
                            } else {
                                tvWelcome.setText("Welcome!");
                            }
                        } else {
                            tvWelcome.setText("Welcome!");
                        }
                    } else {
                        tvWelcome.setText("Welcome!");
                    }
                });
    }
}
