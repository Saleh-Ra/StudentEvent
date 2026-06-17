package com.example.studentevent.ui;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studentevent.data.DatabaseHelper;
import com.example.studentevent.R;
import com.example.studentevent.model.Event;

import java.util.ArrayList;

public class BrowseEventsActivity extends AppCompatActivity {

    private RecyclerView recyclerEvents;
    private Button btnFilterEvents, btnResetFilter;
    private ArrayList<Event> eventList;
    private EventAdapter eventAdapter;

    private DatabaseHelper databaseHelper;

    private Button btnBack;

    /**
     * Purpose: Starts the browse events screen.
     * Input: savedInstanceState contains previous activity state if it exists.
     * Output: Displays all events in RecyclerView.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_events);

        databaseHelper = new DatabaseHelper(this);

        connectViews();
        createEventsList();
        setupRecyclerView();
        setButtonListeners();
    }

    /**
     * Purpose: Connects XML components to Java variables.
     * Input: None.
     * Output: Buttons and RecyclerView are ready to use.
     */
    private void connectViews() {
        recyclerEvents = findViewById(R.id.recyclerEvents);
        btnFilterEvents = findViewById(R.id.btnFilterEvents);
        btnResetFilter = findViewById(R.id.btnResetFilter);
        btnBack = findViewById(R.id.btnBack);
    }

    /**
     * Purpose: Creates temporary event data until SQLite is connected.
     * Input: None.
     * Output: ArrayList with example events.
     */
    /**
     * Purpose: Loads all events from SQLite.
     * Input: None.
     * Output: ArrayList with events from database.
     */
    private void createEventsList() {
        eventList = databaseHelper.getAllEvents();
    }

    /**
     * Purpose: Sets RecyclerView layout and adapter.
     * Input: None.
     * Output: Events are displayed as CardViews.
     */
    private void setupRecyclerView() {
        eventAdapter = new EventAdapter(this, eventList);
        recyclerEvents.setLayoutManager(new LinearLayoutManager(this));
        recyclerEvents.setAdapter(eventAdapter);
    }

    /**
     * Purpose: Adds button click actions.
     * Input: None.
     * Output: Filter button opens dialog, reset button reloads all events.
     */
    private void setButtonListeners() {
        btnFilterEvents.setOnClickListener(v -> {
            FilterEventsFragment filterFragment = new FilterEventsFragment();
            filterFragment.show(getSupportFragmentManager(), "FilterEventsFragment");
        });

        btnResetFilter.setOnClickListener(v -> {
            createEventsList();
            eventAdapter = new EventAdapter(this, eventList);
            recyclerEvents.setAdapter(eventAdapter);
        });

        btnBack.setOnClickListener(v -> finish());
    }

    public void filterEventsByCategory(String selectedCategory) {
        ArrayList<Event> filteredList = new ArrayList<>();

        for (Event event : eventList) {
            if (event.getCategory().equals(selectedCategory)) {
                filteredList.add(event);
            }
        }

        eventAdapter = new EventAdapter(this, filteredList);
        recyclerEvents.setAdapter(eventAdapter);
    }
}