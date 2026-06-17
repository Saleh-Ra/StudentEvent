package com.example.studentevent.model;

public class Event {

    private int id;
    private String name;
    private String organizer;
    private String category;
    private String date;
    private int imageResource;

    /**
     * Purpose: Creates a new event object.
     * Input: Event id, name, organizer, category, date, and image resource.
     * Output: A ready Event object.
     */
    public Event(int id, String name, String organizer, String category, String date, int imageResource) {
        this.id = id;
        this.name = name;
        this.organizer = organizer;
        this.category = category;
        this.date = date;
        this.imageResource = imageResource;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getOrganizer() {
        return organizer;
    }

    public String getCategory() {
        return category;
    }

    public String getDate() {
        return date;
    }

    public int getImageResource() {
        return imageResource;
    }
}