package com.example.studentevent.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studentevent.data.DatabaseHelper;
import com.example.studentevent.R;
import com.example.studentevent.model.Event;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class MyEventsActivity extends AppCompatActivity {

    private RecyclerView recyclerMyEvents;
    private ArrayList<Event> myEventsList;
    private MyEventsAdapter myEventsAdapter;

    private Button btnBack;

    private DatabaseHelper databaseHelper;

    /**
     * Purpose: Starts the user's events screen.
     * Input: savedInstanceState contains previous activity state if it exists.
     * Output: Displays user's registered events.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!isUserLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_my_events);
        databaseHelper = new DatabaseHelper(this);

        connectViews();
        createMyEventsList();
        setupRecyclerView();
        checkExpiredReminders();
        setButtonListeners();
    }

    private boolean isUserLoggedIn() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        return prefs.contains("user_email") && FirebaseAuth.getInstance().getCurrentUser() != null;
    }

    /**
     * Purpose: Connects XML components to Java variables.
     * Input: None.
     * Output: RecyclerView is ready to use.
     */
    private void connectViews() {
        recyclerMyEvents = findViewById(R.id.recyclerMyEvents);
        btnBack = findViewById(R.id.btnBack);
    }

    /**
     * Purpose: Creates temporary registered events until SQLite is connected.
     * Input: None.
     * Output: ArrayList with user's events.
     */
    private void createMyEventsList() {
        myEventsList = databaseHelper.getMyEvents();
    }

    /**
     * Purpose: Sets RecyclerView layout and adapter.
     * Input: None.
     * Output: User's events are displayed as CardViews.
     */
    private void setupRecyclerView() {
        myEventsAdapter = new MyEventsAdapter(this, myEventsList);
        recyclerMyEvents.setLayoutManager(new LinearLayoutManager(this));
        recyclerMyEvents.setAdapter(myEventsAdapter);
    }

    /**
     * Purpose: Adds click action to back button.
     * Input: None.
     * Output: Closes MyEventsActivity and returns to previous screen.
     */
    private void setButtonListeners() {
        btnBack.setOnClickListener(v -> finish());
    }

    private void checkExpiredReminders() {
        Toast.makeText(
                this,
                "יש תזכורות שעברו. בדוק את האירועים שלך.",
                Toast.LENGTH_LONG
        ).show();
    }
}