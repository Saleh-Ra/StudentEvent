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
    public static final int DATABASE_VERSION = 2;

    public static final String TABLE_EVENTS = "events";

    public static final String COL_ID = "id";
    public static final String COL_NAME = "name";
    public static final String COL_ORGANIZER = "organizer";
    public static final String COL_CATEGORY = "category";
    public static final String COL_DATE = "date";
    public static final String COL_IMAGE = "image";
    public static final String TABLE_MY_EVENTS = "my_events";

    public static final String COL_MY_ID = "id";
    public static final String COL_MY_EVENT_ID = "event_id";

    /**
     * Purpose: Creates the SQLite database helper.
     * Input: context is the activity using the database.
     * Output: DatabaseHelper object ready to create/open database.
     */
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Purpose: Creates database tables.
     * Input: db is the SQLite database.
     * Output: Events table is created.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        String createEventsTable = "CREATE TABLE " + TABLE_EVENTS + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_NAME + " TEXT, "
                + COL_ORGANIZER + " TEXT, "
                + COL_CATEGORY + " TEXT, "
                + COL_DATE + " TEXT, "
                + COL_IMAGE + " INTEGER)";

        db.execSQL(createEventsTable);

        String createMyEventsTable = "CREATE TABLE " + TABLE_MY_EVENTS + " ("
                + COL_MY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_MY_EVENT_ID + " INTEGER)";

        db.execSQL(createMyEventsTable);
    }

    /**
     * Purpose: Recreates tables when database version changes.
     * Input: db is database, oldVersion is old version, newVersion is new version.
     * Output: Old table is deleted and new table is created.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EVENTS);
        onCreate(db);
    }
    /**
     * Purpose: Inserts a new event into SQLite.
     * Input: Event name, organizer, category, date, and image resource.
     * Output: Returns true if insert succeeded, false otherwise.
     */
    public boolean insertEvent(String name, String organizer, String category, String date, int image) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COL_NAME, name);
        values.put(COL_ORGANIZER, organizer);
        values.put(COL_CATEGORY, category);
        values.put(COL_DATE, date);
        values.put(COL_IMAGE, image);

        long result = db.insert(TABLE_EVENTS, null, values);
        return result != -1;
    }

    /**
     * Purpose: Reads all events from SQLite.
     * Input: None.
     * Output: ArrayList containing all events.
     */
    public ArrayList<Event> getAllEvents() {
        ArrayList<Event> events = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_EVENTS, null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME));
                String organizer = cursor.getString(cursor.getColumnIndexOrThrow(COL_ORGANIZER));
                String category = cursor.getString(cursor.getColumnIndexOrThrow(COL_CATEGORY));
                String date = cursor.getString(cursor.getColumnIndexOrThrow(COL_DATE));
                int image = cursor.getInt(cursor.getColumnIndexOrThrow(COL_IMAGE));

                events.add(new Event(id, name, organizer, category, date, image));
            } while (cursor.moveToNext());
        }

        cursor.close();
        return events;
    }

    /**
     * Purpose: Updates an existing event in SQLite.
     * Input: Event id, name, organizer, category, date, and image resource.
     * Output: Returns true if update succeeded, false otherwise.
     */
    public boolean updateEvent(int id, String name, String organizer, String category, String date, int image) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COL_NAME, name);
        values.put(COL_ORGANIZER, organizer);
        values.put(COL_CATEGORY, category);
        values.put(COL_DATE, date);
        values.put(COL_IMAGE, image);

        int result = db.update(TABLE_EVENTS, values, COL_ID + "=?", new String[]{String.valueOf(id)});
        return result > 0;
    }

    /**
     * Purpose: Deletes an event from SQLite.
     * Input: Event id.
     * Output: Returns true if delete succeeded, false otherwise.
     */
    public boolean deleteEvent(int id) {
        SQLiteDatabase db = this.getWritableDatabase();

        int result = db.delete(TABLE_EVENTS, COL_ID + "=?", new String[]{String.valueOf(id)});
        return result > 0;
    }

    /**
     * Purpose: Adds an event to the user's personal events list.
     * Input: eventId is the id of the selected event.
     * Output: Returns true if added successfully, false otherwise.
     */
    public boolean addToMyEvents(int eventId) {
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_MY_EVENTS + " WHERE " + COL_MY_EVENT_ID + "=?",
                new String[]{String.valueOf(eventId)}
        );

        if (cursor.getCount() > 0) {
            cursor.close();
            return false;
        }

        cursor.close();

        ContentValues values = new ContentValues();
        values.put(COL_MY_EVENT_ID, eventId);

        long result = db.insert(TABLE_MY_EVENTS, null, values);
        return result != -1;
    }

    /**
     * Purpose: Reads all events added to the user's personal list.
     * Input: None.
     * Output: ArrayList containing user's selected events.
     */
    public ArrayList<Event> getMyEvents() {
        ArrayList<Event> myEvents = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT e.* FROM " + TABLE_EVENTS + " e "
                + "INNER JOIN " + TABLE_MY_EVENTS + " m "
                + "ON e." + COL_ID + " = m." + COL_MY_EVENT_ID;

        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME));
                String organizer = cursor.getString(cursor.getColumnIndexOrThrow(COL_ORGANIZER));
                String category = cursor.getString(cursor.getColumnIndexOrThrow(COL_CATEGORY));
                String date = cursor.getString(cursor.getColumnIndexOrThrow(COL_DATE));
                int image = cursor.getInt(cursor.getColumnIndexOrThrow(COL_IMAGE));

                myEvents.add(new Event(id, name, organizer, category, date, image));
            } while (cursor.moveToNext());
        }

        cursor.close();
        return myEvents;
    }

    /**
     * Purpose: Removes an event from the user's personal events list.
     * Input: eventId is the id of the event to remove.
     * Output: Returns true if removed successfully, false otherwise.
     */
    public boolean removeFromMyEvents(int eventId) {
        SQLiteDatabase db = this.getWritableDatabase();

        int result = db.delete(
                TABLE_MY_EVENTS,
                COL_MY_EVENT_ID + "=?",
                new String[]{String.valueOf(eventId)}
        );

        return result > 0;
    }
}