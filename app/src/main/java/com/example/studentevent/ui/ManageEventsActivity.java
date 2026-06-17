package com.example.studentevent.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studentevent.data.DatabaseHelper;
import com.example.studentevent.R;
import com.example.studentevent.model.Event;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class ManageEventsActivity extends AppCompatActivity {

    private EditText edtEventName, edtOrganizer, edtEventDate, edtDescription;
    private Spinner spinnerManageCategory;
    private Button btnAddEvent, btnUpdateEvent, btnDeleteEvent;
    private RecyclerView recyclerManageEvents;

    private DatabaseHelper databaseHelper;
    private ArrayList<Event> eventsList;
    private EventAdapter eventAdapter;

    private Button btnBack;

    private int selectedEventId = -1;

    /**
     * Purpose: Starts the manage events screen.
     * Input: savedInstanceState contains previous activity state if it exists.
     * Output: Displays form and events from SQLite.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        if (!prefs.contains("user_email") || FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        if (!getString(R.string.role_admin).equals(prefs.getString("user_role", getString(R.string.role_student)))) {
            Toast.makeText(this, R.string.access_denied_admin, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setContentView(R.layout.activity_manage_events);

        databaseHelper = new DatabaseHelper(this);

        connectViews();
        setupSpinner();
        loadEvents();
        setButtonListeners();
    }

    /**
     * Purpose: Connects XML components to Java variables.
     * Input: None.
     * Output: All views are ready to use.
     */
    private void connectViews() {
        edtEventName = findViewById(R.id.edtEventName);
        edtOrganizer = findViewById(R.id.edtOrganizer);
        edtEventDate = findViewById(R.id.edtEventDate);
        edtDescription = findViewById(R.id.edtDescription);
        spinnerManageCategory = findViewById(R.id.spinnerManageCategory);
        btnAddEvent = findViewById(R.id.btnAddEvent);
        btnUpdateEvent = findViewById(R.id.btnUpdateEvent);
        btnDeleteEvent = findViewById(R.id.btnDeleteEvent);
        recyclerManageEvents = findViewById(R.id.recyclerManageEvents);
        btnBack = findViewById(R.id.btnBack);
    }

    /**
     * Purpose: Loads categories from strings.xml into Spinner.
     * Input: None.
     * Output: Spinner shows event categories.
     */
    private void setupSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.event_categories,
                android.R.layout.simple_spinner_item
        );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerManageCategory.setAdapter(adapter);
    }

    /**
     * Purpose: Loads all events from SQLite and displays them.
     * Input: None.
     * Output: RecyclerView shows all stored events.
     */
    private void loadEvents() {
        eventsList = databaseHelper.getAllEvents();

        eventAdapter = new EventAdapter(this, eventsList);
        recyclerManageEvents.setLayoutManager(new LinearLayoutManager(this));
        recyclerManageEvents.setAdapter(eventAdapter);
    }

    /**
     * Purpose: Adds actions to buttons.
     * Input: None.
     * Output: User can add, update, and delete events.
     */
    private void setButtonListeners() {
        btnAddEvent.setOnClickListener(v -> addEvent());
        btnUpdateEvent.setOnClickListener(v -> updateEvent());
        btnDeleteEvent.setOnClickListener(v -> deleteEvent());
        btnBack.setOnClickListener(v -> finish());
    }

    /**
     * Purpose: Adds a new event to SQLite.
     * Input: Values from EditTexts and Spinner.
     * Output: New event is saved and list is refreshed.
     */
    private void addEvent() {
        String name = edtEventName.getText().toString();
        String organizer = edtOrganizer.getText().toString();
        String category = spinnerManageCategory.getSelectedItem().toString();
        String date = edtEventDate.getText().toString();
        String description = edtDescription.getText().toString();

        if (name.isEmpty() || organizer.isEmpty() || date.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "נא למלא את כל השדות", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean success = databaseHelper.insertEvent(
                name,
                organizer,
                category,
                date,
                description,
                R.mipmap.ic_launcher
        );

        if (success) {
            Toast.makeText(this, "האירוע נוסף בהצלחה", Toast.LENGTH_SHORT).show();
            clearFields();
            loadEvents();
        } else {
            Toast.makeText(this, "שגיאה בהוספת האירוע", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Purpose: Updates selected event in SQLite.
     * Input: Selected event id and new form values.
     * Output: Event is updated and list is refreshed.
     */
    /**
     * Purpose: Updates selected event in SQLite.
     * Input: Selected event id and new form values.
     * Output: Event is updated and list is refreshed.
     */
    private void updateEvent() {
        if (selectedEventId == -1) {
            Toast.makeText(this, "בחר אירוע מהרשימה קודם", Toast.LENGTH_SHORT).show();
            return;
        }

        String name = edtEventName.getText().toString();
        String organizer = edtOrganizer.getText().toString();
        String category = spinnerManageCategory.getSelectedItem().toString();
        String date = edtEventDate.getText().toString();
        String description = edtDescription.getText().toString();

        if (name.isEmpty() || organizer.isEmpty() || date.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "נא למלא את כל השדות", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean success = databaseHelper.updateEvent(
                selectedEventId,
                name,
                organizer,
                category,
                date,
                description,
                R.mipmap.ic_launcher
        );

        if (success) {
            Toast.makeText(this, "האירוע עודכן בהצלחה", Toast.LENGTH_SHORT).show();
            clearFields();
            loadEvents();
        } else {
            Toast.makeText(this, "שגיאה בעדכון האירוע", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Purpose: Deletes selected event from SQLite.
     * Input: Selected event id.
     * Output: Event is deleted and list is refreshed.
     */
    /**
     * Purpose: Deletes selected event from SQLite.
     * Input: Selected event id.
     * Output: Event is deleted and list is refreshed.
     */
    /**
     * Purpose: Deletes selected event from SQLite.
     * Input: Selected event id.
     * Output: Event is deleted and list is refreshed.
     */
    private void deleteEvent() {
        if (selectedEventId == -1) {
            Toast.makeText(this, "בחר אירוע מהרשימה קודם", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean success = databaseHelper.deleteEvent(selectedEventId);

        if (success) {
            Toast.makeText(this, "האירוע נמחק בהצלחה", Toast.LENGTH_SHORT).show();
            clearFields();
            loadEvents();
        } else {
            Toast.makeText(this, "שגיאה במחיקת האירוע", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Purpose: Fills the form with selected event details.
     * Input: selectedEvent is the event clicked by the user.
     * Output: Form fields contain selected event data for update/delete.
     */
    public void selectEventForEdit(Event selectedEvent) {
        selectedEventId = selectedEvent.getId();

        edtEventName.setText(selectedEvent.getName());
        edtOrganizer.setText(selectedEvent.getOrganizer());
        edtEventDate.setText(selectedEvent.getDate());
        edtDescription.setText(selectedEvent.getDescription());

        for (int i = 0; i < spinnerManageCategory.getCount(); i++) {
            if (spinnerManageCategory.getItemAtPosition(i).toString().equals(selectedEvent.getCategory())) {
                spinnerManageCategory.setSelection(i);
                break;
            }
        }

        Toast.makeText(this, "האירוע נבחר לעריכה", Toast.LENGTH_SHORT).show();
    }

    /**
     * Purpose: Clears input fields after add/update.
     * Input: None.
     * Output: Form becomes empty.
     */
    private void clearFields() {
        edtEventName.setText("");
        edtOrganizer.setText("");
        edtEventDate.setText("");
        edtDescription.setText("");
        spinnerManageCategory.setSelection(0);
        selectedEventId = -1;
    }
}