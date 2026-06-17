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
import com.example.studentevent.data.MyEventsSync;
import com.example.studentevent.R;
import com.example.studentevent.model.Event;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

/**
 * Purpose: Shows the logged-in user's personal event list.
 * Input: User session and synced data from SQLite/Firestore.
 * Output: Personal events with reminders and actions.
 */
public class MyEventsActivity extends AppCompatActivity {

    private RecyclerView recyclerMyEvents;
    private ArrayList<Event> myEventsList;
    private MyEventsAdapter myEventsAdapter;
    private Button btnBack;
    private DatabaseHelper databaseHelper;

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
        MyEventsSync.syncFromFirestore(this, this::loadMyEvents);
        setButtonListeners();
    }

    private boolean isUserLoggedIn() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        return prefs.contains("user_email") && FirebaseAuth.getInstance().getCurrentUser() != null;
    }

    private void connectViews() {
        recyclerMyEvents = findViewById(R.id.recyclerMyEvents);
        btnBack = findViewById(R.id.btnBack);
    }

    private void loadMyEvents() {
        myEventsList = databaseHelper.getMyEvents();
        setupRecyclerView();
        checkExpiredReminders();
    }

    private void setupRecyclerView() {
        myEventsAdapter = new MyEventsAdapter(this, myEventsList);
        recyclerMyEvents.setLayoutManager(new LinearLayoutManager(this));
        recyclerMyEvents.setAdapter(myEventsAdapter);
    }

    private void setButtonListeners() {
        btnBack.setOnClickListener(v -> finish());
    }

    private void checkExpiredReminders() {
        if (databaseHelper.hasExpiredReminders()) {
            Toast.makeText(this, R.string.warning_expired_reminders, Toast.LENGTH_LONG).show();
        }
    }
}
