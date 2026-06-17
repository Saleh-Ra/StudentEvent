package com.example.studentevent.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.database.Cursor;

import com.example.studentevent.model.Event;

import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "StudentEvent.db";
    public static final int DATABASE_VERSION = 3;

    public static final String TABLE_EVENTS = "events";

    public static final String COL_ID = "id";
    public static final String COL_FIRESTORE_ID = "firestore_id";
    public static final String COL_NAME = "name";
    public static final String COL_ORGANIZER = "organizer";
    public static final String COL_CATEGORY = "category";
    public static final String COL_DATE = "date";
    public static final String COL_DESCRIPTION = "description";
    public static final String COL_IMAGE = "image";
    public static final String COL_REGISTERED_COUNT = "registered_count";
    public static final String COL_IS_REGISTERED = "is_registered"; // Local flag for user's participation

    public static final String TABLE_MY_EVENTS = "my_events";
    public static final String COL_MY_ID = "id";
    public static final String COL_MY_EVENT_ID = "event_id"; // Maps to SQLite ID or Firestore ID? 
    // Usually mapping to SQLite ID is easier locally, but for sync Firestore ID is better.
    // Let's keep event_id as the SQLite ID for now as per previous version, but we might need to change it.
    public static final String COL_REMINDER_DATE = "reminder_date";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createEventsTable = "CREATE TABLE " + TABLE_EVENTS + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_FIRESTORE_ID + " TEXT, "
                + COL_NAME + " TEXT, "
                + COL_ORGANIZER + " TEXT, "
                + COL_CATEGORY + " TEXT, "
                + COL_DATE + " TEXT, "
                + COL_DESCRIPTION + " TEXT, "
                + COL_IMAGE + " INTEGER, "
                + COL_REGISTERED_COUNT + " INTEGER DEFAULT 0, "
                + COL_IS_REGISTERED + " INTEGER DEFAULT 0)";

        db.execSQL(createEventsTable);

        String createMyEventsTable = "CREATE TABLE " + TABLE_MY_EVENTS + " ("
                + COL_MY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_MY_EVENT_ID + " INTEGER, "
                + COL_REMINDER_DATE + " TEXT)";

        db.execSQL(createMyEventsTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EVENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MY_EVENTS);
        onCreate(db);
    }

    /**
     * Purpose: Syncs an event from Firestore to SQLite.
     * Input: Event object from Firestore.
     * Output: Returns true if success.
     */
    public boolean syncEvent(Event event) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_FIRESTORE_ID, event.getFirestoreId());
        values.put(COL_NAME, event.getName());
        values.put(COL_ORGANIZER, event.getOrganizer());
        values.put(COL_CATEGORY, event.getCategory());
        values.put(COL_DATE, event.getDate());
        values.put(COL_DESCRIPTION, event.getDescription());
        values.put(COL_IMAGE, event.getImageResource());
        values.put(COL_REGISTERED_COUNT, event.getRegisteredCount());

        // Check if exists
        Cursor cursor = db.query(TABLE_EVENTS, new String[]{COL_ID}, COL_FIRESTORE_ID + "=?", 
                new String[]{event.getFirestoreId()}, null, null, null);
        
        long result;
        if (cursor.moveToFirst()) {
            result = db.update(TABLE_EVENTS, values, COL_FIRESTORE_ID + "=?", new String[]{event.getFirestoreId()});
        } else {
            result = db.insert(TABLE_EVENTS, null, values);
        }
        cursor.close();
        return result != -1;
    }

    public ArrayList<Event> getAllEvents() {
        ArrayList<Event> events = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_EVENTS, null);

        if (cursor.moveToFirst()) {
            do {
                events.add(cursorToEvent(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return events;
    }

    public ArrayList<Event> getEventsByCategoryAndParticipants(String category, int minParticipants) {
        ArrayList<Event> events = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        String selection = "";
        ArrayList<String> args = new ArrayList<>();

        if (!category.equals("All") && !category.isEmpty()) {
            selection += COL_CATEGORY + "=?";
            args.add(category);
        }

        if (minParticipants > 0) {
            if (!selection.isEmpty()) selection += " AND ";
            selection += COL_REGISTERED_COUNT + ">=?";
            args.add(String.valueOf(minParticipants));
        }

        Cursor cursor = db.query(TABLE_EVENTS, null, selection.isEmpty() ? null : selection, 
                args.isEmpty() ? null : args.toArray(new String[0]), null, null, null);

        if (cursor.moveToFirst()) {
            do {
                events.add(cursorToEvent(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return events;
    }

    private Event cursorToEvent(Cursor cursor) {
        int id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID));
        String fId = cursor.getString(cursor.getColumnIndexOrThrow(COL_FIRESTORE_ID));
        String name = cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME));
        String organizer = cursor.getString(cursor.getColumnIndexOrThrow(COL_ORGANIZER));
        String category = cursor.getString(cursor.getColumnIndexOrThrow(COL_CATEGORY));
        String date = cursor.getString(cursor.getColumnIndexOrThrow(COL_DATE));
        String desc = cursor.getString(cursor.getColumnIndexOrThrow(COL_DESCRIPTION));
        int image = cursor.getInt(cursor.getColumnIndexOrThrow(COL_IMAGE));
        int count = cursor.getInt(cursor.getColumnIndexOrThrow(COL_REGISTERED_COUNT));
        boolean isRegistered = cursor.getInt(cursor.getColumnIndexOrThrow(COL_IS_REGISTERED)) == 1;

        Event event = new Event(id, fId, name, organizer, category, date, desc, image, count);
        event.setUserRegistered(isRegistered);
        return event;
    }

    public boolean isUserRegistered(int eventId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_EVENTS, new String[]{COL_IS_REGISTERED}, COL_ID + "=?",
                new String[]{String.valueOf(eventId)}, null, null, null);
        boolean registered = false;
        if (cursor.moveToFirst()) {
            registered = cursor.getInt(0) == 1;
        }
        cursor.close();
        return registered;
    }

    public boolean setRegistrationStatus(int eventId, boolean registered) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_IS_REGISTERED, registered ? 1 : 0);
        return db.update(TABLE_EVENTS, values, COL_ID + "=?", new String[]{String.valueOf(eventId)}) > 0;
    }

    public boolean updateRegisteredCount(int eventId, int newCount) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_REGISTERED_COUNT, newCount);
        return db.update(TABLE_EVENTS, values, COL_ID + "=?", new String[]{String.valueOf(eventId)}) > 0;
    }

    // Previous methods updated for new schema
    public boolean insertEvent(String name, String organizer, String category, String date, String description, int image) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_NAME, name);
        values.put(COL_ORGANIZER, organizer);
        values.put(COL_CATEGORY, category);
        values.put(COL_DATE, date);
        values.put(COL_DESCRIPTION, description);
        values.put(COL_IMAGE, image);
        long result = db.insert(TABLE_EVENTS, null, values);
        return result != -1;
    }

    public boolean updateEvent(int id, String name, String organizer, String category, String date, String description, int image) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_NAME, name);
        values.put(COL_ORGANIZER, organizer);
        values.put(COL_CATEGORY, category);
        values.put(COL_DATE, date);
        values.put(COL_DESCRIPTION, description);
        values.put(COL_IMAGE, image);
        int result = db.update(TABLE_EVENTS, values, COL_ID + "=?", new String[]{String.valueOf(id)});
        return result > 0;
    }

    public boolean deleteEvent(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_EVENTS, COL_ID + "=?", new String[]{String.valueOf(id)}) > 0;
    }

    public boolean isInMyEvents(int eventId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_MY_EVENTS, new String[]{COL_MY_ID}, COL_MY_EVENT_ID + "=?",
                new String[]{String.valueOf(eventId)}, null, null, null);
        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }

    public boolean addToMyEvents(int eventId) {
        if (isInMyEvents(eventId)) {
            return false;
        }
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_MY_EVENT_ID, eventId);
        return db.insert(TABLE_MY_EVENTS, null, values) != -1;
    }

    public ArrayList<Event> getMyEvents() {
        ArrayList<Event> myEvents = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT e.*, m." + COL_REMINDER_DATE + " FROM " + TABLE_EVENTS + " e INNER JOIN "
                + TABLE_MY_EVENTS + " m ON e." + COL_ID + " = m." + COL_MY_EVENT_ID;
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                Event event = cursorToEvent(cursor);
                int reminderIndex = cursor.getColumnIndex(COL_REMINDER_DATE);
                if (reminderIndex >= 0) {
                    event.setReminderDate(cursor.getString(reminderIndex));
                }
                myEvents.add(event);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return myEvents;
    }

    public boolean updateReminderDate(int eventId, String reminderDate) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_REMINDER_DATE, reminderDate);
        return db.update(TABLE_MY_EVENTS, values, COL_MY_EVENT_ID + "=?", new String[]{String.valueOf(eventId)}) > 0;
    }

    public boolean hasExpiredReminders() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_MY_EVENTS, new String[]{COL_REMINDER_DATE},
                COL_REMINDER_DATE + " IS NOT NULL AND " + COL_REMINDER_DATE + " != ''",
                null, null, null, null);
        boolean hasExpired = false;
        if (cursor.moveToFirst()) {
            do {
                if (isDateBeforeToday(cursor.getString(0))) {
                    hasExpired = true;
                    break;
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        return hasExpired;
    }

    private boolean isDateBeforeToday(String dateText) {
        if (dateText == null || dateText.isEmpty()) {
            return false;
        }
        String[] parts = dateText.split("\\.");
        if (parts.length != 3) {
            return false;
        }
        try {
            int day = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]);
            int year = Integer.parseInt(parts[2]);

            java.util.Calendar reminder = java.util.Calendar.getInstance();
            reminder.set(year, month - 1, day, 0, 0, 0);
            reminder.set(java.util.Calendar.MILLISECOND, 0);

            java.util.Calendar today = java.util.Calendar.getInstance();
            today.set(java.util.Calendar.HOUR_OF_DAY, 0);
            today.set(java.util.Calendar.MINUTE, 0);
            today.set(java.util.Calendar.SECOND, 0);
            today.set(java.util.Calendar.MILLISECOND, 0);

            return reminder.before(today);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public boolean removeFromMyEvents(int eventId) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_MY_EVENTS, COL_MY_EVENT_ID + "=?", new String[]{String.valueOf(eventId)}) > 0;
    }
}