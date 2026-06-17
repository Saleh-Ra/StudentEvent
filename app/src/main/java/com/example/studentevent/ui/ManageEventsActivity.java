package com.example.studentevent.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studentevent.data.DatabaseHelper;
import com.example.studentevent.data.EventFirestoreSync;
import com.example.studentevent.R;
import com.example.studentevent.model.Event;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

/**
 * Purpose: Admin screen for managing events in SQLite and Firestore.
 * Input: Event form data and selected event.
 * Output: CRUD operations on events with confirmation dialogs.
 */
public class ManageEventsActivity extends AppCompatActivity {

    private EditText edtEventName, edtOrganizer, edtEventDate, edtDescription;
    private Spinner spinnerManageCategory;
    private Button btnAddEvent, btnUpdateEvent, btnDeleteEvent, btnBack;
    private RecyclerView recyclerManageEvents;

    private DatabaseHelper databaseHelper;
    private ArrayList<Event> eventsList;
    private EventAdapter eventAdapter;
    private Event selectedEvent;

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

    private void setupSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.manage_event_categories, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerManageCategory.setAdapter(adapter);
    }

    private void loadEvents() {
        eventsList = databaseHelper.getAllEvents();
        eventAdapter = new EventAdapter(this, eventsList);
        recyclerManageEvents.setLayoutManager(new LinearLayoutManager(this));
        recyclerManageEvents.setAdapter(eventAdapter);
    }

    private void setButtonListeners() {
        btnAddEvent.setOnClickListener(v -> addEvent());
        btnUpdateEvent.setOnClickListener(v -> confirmUpdateEvent());
        btnDeleteEvent.setOnClickListener(v -> confirmDeleteEvent());
        btnBack.setOnClickListener(v -> finish());
    }

    private boolean validateForm() {
        String name = edtEventName.getText().toString().trim();
        String organizer = edtOrganizer.getText().toString().trim();
        String date = edtEventDate.getText().toString().trim();
        String description = edtDescription.getText().toString().trim();

        if (name.isEmpty() || organizer.isEmpty() || date.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, R.string.fields_empty, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private String getSelectedCategory() {
        return spinnerManageCategory.getSelectedItem().toString();
    }

    private int getImageForCategory(String category) {
        if ("סדנה".equals(category) || "כנס".equals(category)) {
            return R.drawable.ic_launcher_background;
        }
        return R.drawable.ic_launcher_foreground;
    }

    private void addEvent() {
        if (!validateForm()) {
            return;
        }

        String name = edtEventName.getText().toString().trim();
        String organizer = edtOrganizer.getText().toString().trim();
        String category = getSelectedCategory();
        String date = edtEventDate.getText().toString().trim();
        String description = edtDescription.getText().toString().trim();
        int image = getImageForCategory(category);

        EventFirestoreSync.addEvent(name, organizer, category, date, description, image,
                firestoreId -> {
                    long sqliteId = databaseHelper.insertEventReturningId(
                            name, organizer, category, date, description, image);
                    if (sqliteId != -1 && databaseHelper.setFirestoreId((int) sqliteId, firestoreId)) {
                        Toast.makeText(this, R.string.success_event_added, Toast.LENGTH_SHORT).show();
                        clearFields();
                        loadEvents();
                    } else {
                        Toast.makeText(this, R.string.error_event_added, Toast.LENGTH_SHORT).show();
                    }
                },
                e -> Toast.makeText(this, getString(R.string.error_firestore, e.getMessage()), Toast.LENGTH_SHORT).show());
    }

    private void confirmUpdateEvent() {
        if (selectedEvent == null) {
            Toast.makeText(this, R.string.error_select_event_first, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!validateForm()) {
            return;
        }

        new AlertDialog.Builder(this)
                .setMessage(R.string.confirm_update_event)
                .setPositiveButton(R.string.btn_yes, (dialog, which) -> updateEvent())
                .setNegativeButton(R.string.btn_no, null)
                .show();
    }

    private void updateEvent() {
        String name = edtEventName.getText().toString().trim();
        String organizer = edtOrganizer.getText().toString().trim();
        String category = getSelectedCategory();
        String date = edtEventDate.getText().toString().trim();
        String description = edtDescription.getText().toString().trim();
        int image = getImageForCategory(category);
        int registeredCount = selectedEvent.getRegisteredCount();

        Runnable updateSqlite = () -> {
            boolean success = databaseHelper.updateEvent(
                    selectedEvent.getId(), name, organizer, category, date, description, image);
            if (success) {
                Toast.makeText(this, R.string.success_event_updated, Toast.LENGTH_SHORT).show();
                clearFields();
                loadEvents();
            } else {
                Toast.makeText(this, R.string.error_event_updated, Toast.LENGTH_SHORT).show();
            }
        };

        String firestoreId = selectedEvent.getFirestoreId();
        if (firestoreId == null || firestoreId.isEmpty()) {
            updateSqlite.run();
            return;
        }

        EventFirestoreSync.updateEvent(firestoreId, name, organizer, category, date, description,
                image, registeredCount,
                aVoid -> updateSqlite.run(),
                e -> Toast.makeText(this, getString(R.string.error_firestore, e.getMessage()), Toast.LENGTH_SHORT).show());
    }

    private void confirmDeleteEvent() {
        if (selectedEvent == null) {
            Toast.makeText(this, R.string.error_select_event_first, Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setMessage(R.string.confirm_delete_event)
                .setPositiveButton(R.string.btn_yes, (dialog, which) -> deleteEvent())
                .setNegativeButton(R.string.btn_no, null)
                .show();
    }

    private void deleteEvent() {
        Runnable deleteSqlite = () -> {
            if (databaseHelper.deleteEvent(selectedEvent.getId())) {
                Toast.makeText(this, R.string.success_event_deleted, Toast.LENGTH_SHORT).show();
                clearFields();
                loadEvents();
            } else {
                Toast.makeText(this, R.string.error_event_deleted, Toast.LENGTH_SHORT).show();
            }
        };

        String firestoreId = selectedEvent.getFirestoreId();
        if (firestoreId == null || firestoreId.isEmpty()) {
            deleteSqlite.run();
            return;
        }

        EventFirestoreSync.deleteEvent(firestoreId,
                aVoid -> deleteSqlite.run(),
                e -> Toast.makeText(this, getString(R.string.error_firestore, e.getMessage()), Toast.LENGTH_SHORT).show());
    }

    public void selectEventForEdit(Event event) {
        selectedEvent = event;
        edtEventName.setText(event.getName());
        edtOrganizer.setText(event.getOrganizer());
        edtEventDate.setText(event.getDate());
        edtDescription.setText(event.getDescription());

        for (int i = 0; i < spinnerManageCategory.getCount(); i++) {
            if (spinnerManageCategory.getItemAtPosition(i).toString().equals(event.getCategory())) {
                spinnerManageCategory.setSelection(i);
                break;
            }
        }

        Toast.makeText(this, R.string.event_selected_for_edit, Toast.LENGTH_SHORT).show();
    }

    private void clearFields() {
        edtEventName.setText("");
        edtOrganizer.setText("");
        edtEventDate.setText("");
        edtDescription.setText("");
        spinnerManageCategory.setSelection(0);
        selectedEvent = null;
    }
}
