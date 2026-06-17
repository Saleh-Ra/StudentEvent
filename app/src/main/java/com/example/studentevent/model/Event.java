package com.example.studentevent.model;

import com.google.firebase.firestore.DocumentId;

/**
 * Purpose: Represents an Event in the system.
 * Input: Data fields for event details.
 * Output: Event object containing details.
 */
public class Event {

    private int id; // SQLite ID
    @DocumentId
    private String firestoreId; // Firestore ID
    private String name;
    private String organizer;
    private String category;
    private String date;
    private String description;
    private int imageResource;
    private int registeredCount;
    private boolean userRegistered; // New field for Section 6

    public Event() {
        // Required for Firestore
    }

    /**
     * Purpose: Creates a new event object for SQLite.
     */
    public Event(int id, String firestoreId, String name, String organizer, String category, String date, String description, int imageResource, int registeredCount) {
        this.id = id;
        this.firestoreId = firestoreId;
        this.name = name;
        this.organizer = organizer;
        this.category = category;
        this.date = date;
        this.description = description;
        this.imageResource = imageResource;
        this.registeredCount = registeredCount;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getFirestoreId() { return firestoreId; }
    public void setFirestoreId(String firestoreId) { this.firestoreId = firestoreId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getOrganizer() { return organizer; }
    public void setOrganizer(String organizer) { this.organizer = organizer; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getImageResource() { return imageResource; }
    public void setImageResource(int imageResource) { this.imageResource = imageResource; }

    public int getRegisteredCount() { return registeredCount; }
    public void setRegisteredCount(int registeredCount) { this.registeredCount = registeredCount; }

    public boolean isUserRegistered() { return userRegistered; }
    public void setUserRegistered(boolean userRegistered) { this.userRegistered = userRegistered; }
}
