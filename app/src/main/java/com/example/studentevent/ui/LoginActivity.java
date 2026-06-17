package com.example.studentevent.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.studentevent.R;
import com.example.studentevent.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Purpose: Handles user login and registration using Firebase.
 * Input: User credentials (email, password, full name).
 * Output: Authenticated user session and navigation to MainActivity.
 */
public class LoginActivity extends AppCompatActivity {

    private EditText etFullName, etEmail, etPassword;
    private Button btnSubmit;
    private TextView tvTitle, tvToggleMode;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private boolean isLoginMode = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        checkIfLoggedIn();
        connectViews();
        setListeners();
    }

    /**
     * Purpose: Checks if the user is already logged in via SharedPreferences.
     */
    private void checkIfLoggedIn() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        if (prefs.contains("user_email") && mAuth.getCurrentUser() != null) {
            navigateToMain();
        }
    }

    private void connectViews() {
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnSubmit = findViewById(R.id.btnSubmit);
        tvTitle = findViewById(R.id.tvTitle);
        tvToggleMode = findViewById(R.id.tvToggleMode);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setListeners() {
        btnSubmit.setOnClickListener(v -> {
            if (isLoginMode) {
                loginUser();
            } else {
                registerUser();
            }
        });

        tvToggleMode.setOnClickListener(v -> toggleMode());
    }

    private void toggleMode() {
        isLoginMode = !isLoginMode;
        if (isLoginMode) {
            tvTitle.setText(R.string.login_title);
            etFullName.setVisibility(View.GONE);
            btnSubmit.setText(R.string.btn_login);
            tvToggleMode.setText(R.string.switch_to_register);
        } else {
            tvTitle.setText(R.string.register_title);
            etFullName.setVisibility(View.VISIBLE);
            btnSubmit.setText(R.string.btn_register);
            tvToggleMode.setText(R.string.switch_to_login);
        }
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, R.string.fields_empty, Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        fetchUserDetailsAndSave(user.getUid());
                    } else {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(LoginActivity.this, getString(R.string.login_failed, task.getException().getMessage()), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void registerUser() {
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (fullName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, R.string.fields_empty, Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        String uid = mAuth.getCurrentUser().getUid();
                        // Default role is Student
                        User newUser = new User(uid, fullName, email, getString(R.string.role_student));
                        saveUserToFirestore(newUser);
                    } else {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(LoginActivity.this, getString(R.string.registration_failed, task.getException().getMessage()), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void saveUserToFirestore(User user) {
        db.collection("users").document(user.getUid())
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(LoginActivity.this, R.string.registration_success, Toast.LENGTH_SHORT).show();
                    finishLogin(user);
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(LoginActivity.this, getString(R.string.error_firestore, e.getMessage()), Toast.LENGTH_LONG).show();
                });
    }

    private void fetchUserDetailsAndSave(String uid) {
        db.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(LoginActivity.this, R.string.error_user_profile_missing, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    User user = documentSnapshot.toObject(User.class);
                    if (user == null) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(LoginActivity.this, R.string.error_fetch_user_data, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (user.getUid() == null) {
                        user.setUid(uid);
                    }
                    finishLogin(user);
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(LoginActivity.this, R.string.error_fetch_user_data, Toast.LENGTH_SHORT).show();
                });
    }

    private void finishLogin(User user) {
        if (saveUserToPrefs(user)) {
            progressBar.setVisibility(View.GONE);
            navigateToMain();
        }
    }

    private boolean saveUserToPrefs(User user) {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("user_uid", user.getUid());
        editor.putString("user_name", user.getFullName());
        editor.putString("user_email", user.getEmail());
        editor.putString("user_role", user.getRole());
        return editor.commit();
    }

    private void navigateToMain() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}