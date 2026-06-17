package com.example.studentevent.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studentevent.data.DatabaseHelper;
import com.example.studentevent.R;
import com.example.studentevent.model.Event;
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
        setContentView(R.layout.activity_browse_events);

        dbHelper = new DatabaseHelper(this);
        db = FirebaseFirestore.getInstance();

        connectViews();
        setButtonListeners();
        
        // Synchronize then load
        syncFromFirestore();
    }

    private void connectViews() {
        recyclerEvents = findViewById(R.id.recyclerEvents);
        btnFilterEvents = findViewById(R.id.btnFilterEvents);
        btnResetFilter = findViewById(R.id.btnResetFilter);
        btnBack = findViewById(R.id.btnBack);
        
        // Adding a progress bar programmatically or checking if it exists in layout
        // For now, let's assume we might add it to layout or just use Toasts
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
                            // Default image if none provided in Firestore
                            if (event.getImageResource() == 0) {
                                event.setImageResource(R.mipmap.ic_launcher);
                            }
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
            // Open EventDetailsFragment (Section 6)
            // For now, just toast or keep it ready
            Toast.makeText(this, "Clicked: " + event.getName(), Toast.LENGTH_SHORT).show();
        });
        recyclerEvents.setLayoutManager(new LinearLayoutManager(this));
        recyclerEvents.setAdapter(eventAdapter);
    }
}