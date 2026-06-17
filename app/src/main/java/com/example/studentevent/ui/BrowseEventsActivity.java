package com.example.studentevent.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studentevent.data.DatabaseHelper;
import com.example.studentevent.R;
import com.example.studentevent.model.Event;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

/**
 * Purpose: Screen for browsing all events. Synchronizes data with Firestore and uses SQLite for local display.
 * Input: Events from Firestore/SQLite.
 * Output: Displayed list of events with filtering capabilities.
 */
public class BrowseEventsActivity extends AppCompatActivity {

    private RecyclerView recyclerEvents;
    private Button btnFilterEvents, btnResetFilter, btnBack;
    
    private EventAdapter eventAdapter;
    private DatabaseHelper dbHelper;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!isUserLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_browse_events);

        dbHelper = new DatabaseHelper(this);
        db = FirebaseFirestore.getInstance();

        connectViews();
        setButtonListeners();
        
        // Synchronize then load
        syncFromFirestore();
    }

    private boolean isUserLoggedIn() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        return prefs.contains("user_email") && FirebaseAuth.getInstance().getCurrentUser() != null;
    }

    private void connectViews() {
        recyclerEvents = findViewById(R.id.recyclerEvents);
        btnFilterEvents = findViewById(R.id.btnFilterEvents);
        btnResetFilter = findViewById(R.id.btnResetFilter);
        btnBack = findViewById(R.id.btnBack);
    }

    private void setButtonListeners() {
        btnFilterEvents.setOnClickListener(v -> {
            FilterEventsFragment filterFragment = new FilterEventsFragment();
            filterFragment.show(getSupportFragmentManager(), "FilterEventsFragment");
        });

        btnResetFilter.setOnClickListener(v -> loadEventsFromSQLite());

        btnBack.setOnClickListener(v -> finish());
    }

    /**
     * Purpose: Synchronizes Firestore events to the local SQLite database.
     */
    private void syncFromFirestore() {
        db.collection("events").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Event event = document.toObject(Event.class);
                            event.setFirestoreId(document.getId());
                            event.setImageResource(getImageForCategory(event.getCategory()));
                            dbHelper.syncEvent(event);
                        }
                        loadEventsFromSQLite();
                    } else {
                        Toast.makeText(this, R.string.error_sync_failed, Toast.LENGTH_SHORT).show();
                        loadEventsFromSQLite(); // Load local data anyway
                    }
                });
    }

    /**
     * Purpose: Loads events from SQLite and updates the RecyclerView.
     */
    private void loadEventsFromSQLite() {
        ArrayList<Event> eventList = dbHelper.getAllEvents();
        setupRecyclerView(eventList);
    }

    /**
     * Purpose: Applies filters to the event list using an SQLite query.
     */
    public void applyFilter(String category, int minParticipants) {
        ArrayList<Event> filteredList = dbHelper.getEventsByCategoryAndParticipants(category, minParticipants);
        setupRecyclerView(filteredList);
    }

    private void setupRecyclerView(ArrayList<Event> list) {
        eventAdapter = new EventAdapter(this, list);
        eventAdapter.setOnEventClickListener(event -> {
            EventDetailsFragment detailsFragment = EventDetailsFragment.newInstance(event.getId());
            detailsFragment.show(getSupportFragmentManager(), "EventDetailsFragment");
        });
        recyclerEvents.setLayoutManager(new LinearLayoutManager(this));
        recyclerEvents.setAdapter(eventAdapter);
    }

    /**
     * Purpose: Returns a local drawable image based on event category.
     * Input: Category name.
     * Output: Drawable resource id.
     */
    private int getImageForCategory(String category) {
        if ("סדנה".equals(category)) {
            return R.drawable.ic_launcher_background;
        }
        if ("כנס".equals(category)) {
            return R.drawable.ic_launcher_background;
        }
        return R.drawable.ic_launcher_foreground;
    }
}