package com.example.studentevent.data;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * Purpose: Syncs event CRUD operations with Firestore.
 * Input: Event field values.
 * Output: Firestore documents in the events collection.
 */
public class EventFirestoreSync {

    private EventFirestoreSync() {
    }

    public static void addEvent(String name, String organizer, String category, String date,
                                String description, int image,
                                OnSuccessListener<String> onSuccess, OnFailureListener onFailure) {
        FirebaseFirestore.getInstance().collection("events")
                .add(buildEventMap(name, organizer, category, date, description, image, 0))
                .addOnSuccessListener(documentReference -> onSuccess.onSuccess(documentReference.getId()))
                .addOnFailureListener(onFailure);
    }

    public static void updateEvent(String firestoreId, String name, String organizer, String category,
                                   String date, String description, int image, int registeredCount,
                                   OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        FirebaseFirestore.getInstance().collection("events").document(firestoreId)
                .update(buildEventMap(name, organizer, category, date, description, image, registeredCount))
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    public static void deleteEvent(String firestoreId,
                                   OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        FirebaseFirestore.getInstance().collection("events").document(firestoreId)
                .delete()
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    private static Map<String, Object> buildEventMap(String name, String organizer, String category,
                                                     String date, String description, int image,
                                                     int registeredCount) {
        Map<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("organizer", organizer);
        data.put("category", category);
        data.put("date", date);
        data.put("description", description);
        data.put("imageResource", image);
        data.put("registeredCount", registeredCount);
        return data;
    }
}
