package com.example.studentevent.model;

/**
 * Purpose: Represents a user review for an event.
 * Input: Review fields from Firestore.
 * Output: Review object for display.
 */
public class Review {

    private String eventId;
    private String userId;
    private String userName;
    private int rating;
    private String text;

    public Review() {
        // Required for Firestore
    }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
}
