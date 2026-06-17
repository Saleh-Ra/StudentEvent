package com.example.studentevent.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.studentevent.R;

public class MainActivity extends AppCompatActivity {

    private Button btnViewEvents;
    private Button btnMyEvents;
    private Button btnManageEvents;

    /**
     * Purpose: Starts the main screen and connects buttons to their screens.
     * Input: savedInstanceState contains previous activity state if it exists.
     * Output: Displays the main menu screen.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        connectViews();
        setButtonListeners();
    }

    /**
     * Purpose: Connects XML components to Java variables.
     * Input: None.
     * Output: All main screen buttons are ready to use.
     */
    private void connectViews() {
        btnViewEvents = findViewById(R.id.btnViewEvents);
        btnMyEvents = findViewById(R.id.btnMyEvents);
        btnManageEvents = findViewById(R.id.btnManageEvents);
    }

    /**
     * Purpose: Adds click actions to the main menu buttons.
     * Input: None.
     * Output: Each button opens the correct activity using Intent.
     */
    private void setButtonListeners() {
        btnViewEvents.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, BrowseEventsActivity.class);
            startActivity(intent);
        });

        btnMyEvents.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MyEventsActivity.class);
            startActivity(intent);
        });

        btnManageEvents.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ManageEventsActivity.class);
            startActivity(intent);
        });
    }
}