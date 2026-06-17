package com.example.studentevent.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.studentevent.R;
import com.google.firebase.auth.FirebaseAuth;

/**
 * Purpose: Home screen for logged-in users. Displays navigation options based on role.
 * Input: User session data from SharedPreferences.
 * Output: Navigation to various event screens or logout.
 */
public class MainActivity extends AppCompatActivity {

    private Button btnViewEvents, btnMyEvents, btnManageEvents, btnLogout;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        
        if (!isUserLoggedIn()) {
            navigateToLogin();
            return;
        }

        connectViews();
        setupRoleBasedUI();
        setButtonListeners();
    }

    /**
     * Purpose: Checks if user data exists in SharedPreferences.
     * @return true if logged in.
     */
    private boolean isUserLoggedIn() {
        return prefs.contains("user_email") && FirebaseAuth.getInstance().getCurrentUser() != null;
    }

    private void connectViews() {
        btnViewEvents = findViewById(R.id.btnViewEvents);
        btnMyEvents = findViewById(R.id.btnMyEvents);
        btnManageEvents = findViewById(R.id.btnManageEvents);
        btnLogout = findViewById(R.id.btnLogout);
    }

    /**
     * Purpose: Displays management button only for Admin users.
     */
    private void setupRoleBasedUI() {
        if (isAdmin()) {
            btnManageEvents.setVisibility(View.VISIBLE);
        } else {
            btnManageEvents.setVisibility(View.GONE);
        }
    }

    private boolean isAdmin() {
        return getString(R.string.role_admin).equals(prefs.getString("user_role", getString(R.string.role_student)));
    }

    private void setButtonListeners() {
        btnViewEvents.setOnClickListener(v -> {
            startActivity(new Intent(this, BrowseEventsActivity.class));
        });

        btnMyEvents.setOnClickListener(v -> {
            startActivity(new Intent(this, MyEventsActivity.class));
        });

        btnManageEvents.setOnClickListener(v -> {
            if (isAdmin()) {
                startActivity(new Intent(this, ManageEventsActivity.class));
            } else {
                Toast.makeText(this, R.string.access_denied_admin, Toast.LENGTH_SHORT).show();
            }
        });

        btnLogout.setOnClickListener(v -> logout());
    }

    /**
     * Purpose: Signs out from Firebase and clears local preferences.
     */
    private void logout() {
        FirebaseAuth.getInstance().signOut();
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.commit();
        navigateToLogin();
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}