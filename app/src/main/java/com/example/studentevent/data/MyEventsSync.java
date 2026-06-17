package com.example.studentevent.data;

import android.content.Context;

import com.example.studentevent.model.Event;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * Purpose: Syncs personal events between SQLite and Firestore.
 * Input: Event data and user context.
 * Output: Firestore documents under users/{uid}/myEvents.
 */
public class MyEventsSync {

    private MyEventsSync() {
    }

    public static void saveToFirestore(Event event, String reminderDate) {
        String uid = getUid();
        if (uid == null) {
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("sqliteEventId", event.getId());
        data.put("firestoreEventId", event.getFirestoreId());
        data.put("reminderDate", reminderDate != null ? reminderDate : "");

        FirebaseFirestore.getInstance()
                .collection("users").document(uid)
                .collection("myEvents").document(getDocId(event))
                .set(data);
    }

    public static void removeFromFirestore(Event event) {
        String uid = getUid();
        if (uid == null) {
            return;
        }

        FirebaseFirestore.getInstance()
                .collection("users").document(uid)
                .collection("myEvents").document(getDocId(event))
                .delete();
    }

    public static void syncFromFirestore(Context context, Runnable onComplete) {
        String uid = getUid();
        if (uid == null) {
            onComplete.run();
            return;
        }

        DatabaseHelper dbHelper = new DatabaseHelper(context);
        FirebaseFirestore.getInstance()
                .collection("users").document(uid)
                .collection("myEvents")
                .get()
                .addOnSuccessListener(snapshot -> {
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        Long sqliteId = doc.getLong("sqliteEventId");
                        if (sqliteId == null) {
                            continue;
                        }
                        int eventId = sqliteId.intValue();
                        if (!dbHelper.isInMyEvents(eventId)) {
                            dbHelper.addToMyEvents(eventId);
                        }
                        String reminderDate = doc.getString("reminderDate");
                        if (reminderDate != null && !reminderDate.isEmpty()) {
                            dbHelper.updateReminderDate(eventId, reminderDate);
                        }
                    }
                    onComplete.run();
                })
                .addOnFailureListener(e -> onComplete.run());
    }

    private static String getUid() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            return null;
        }
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    private static String getDocId(Event event) {
        if (event.getFirestoreId() != null && !event.getFirestoreId().isEmpty()) {
            return event.getFirestoreId();
        }
        return "local_" + event.getId();
    }
}
