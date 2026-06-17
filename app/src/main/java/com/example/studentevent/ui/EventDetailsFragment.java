package com.example.studentevent.ui;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.studentevent.R;
import com.example.studentevent.data.DatabaseHelper;
import com.example.studentevent.model.Event;
import com.example.studentevent.model.Review;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.HashMap;
import java.util.Map;

/**
 * Purpose: Shows full event details and handles registration.
 * Input: SQLite event id.
 * Output: Event details dialog with register, add to my events, and share actions.
 */
public class EventDetailsFragment extends DialogFragment {

    private Event event;
    private DatabaseHelper dbHelper;
    private FirebaseFirestore firestore;

    private ImageView imgDetailsEvent;
    private TextView txtDetailsName, txtDetailsOrganizer, txtDetailsCategory, txtDetailsDate;
    private TextView txtDetailsDescription, txtDetailsRegisteredCount, txtDetailsAverageRating;
    private TextView txtDetailsReviewsLabel, txtDetailsReviews, txtDetailsStatus;
    private Button btnDetailsRegister, btnDetailsAddToMy, btnDetailsShare;

    public static EventDetailsFragment newInstance(int eventId) {
        EventDetailsFragment fragment = new EventDetailsFragment();
        Bundle args = new Bundle();
        args.putInt("event_id", eventId);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        dbHelper = new DatabaseHelper(requireContext());
        firestore = FirebaseFirestore.getInstance();

        int eventId = requireArguments().getInt("event_id");
        event = fetchEventFromSQLite(eventId);

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_event_details, null);
        connectViews(view);
        displayEventDetails();
        loadReviews();
        checkRegistrationFromFirestore();
        setListeners();

        return new AlertDialog.Builder(requireContext()).setView(view).create();
    }

    private Event fetchEventFromSQLite(int id) {
        for (Event e : dbHelper.getAllEvents()) {
            if (e.getId() == id) {
                return e;
            }
        }
        return null;
    }

    private void connectViews(View view) {
        imgDetailsEvent = view.findViewById(R.id.imgDetailsEvent);
        txtDetailsName = view.findViewById(R.id.txtDetailsName);
        txtDetailsOrganizer = view.findViewById(R.id.txtDetailsOrganizer);
        txtDetailsCategory = view.findViewById(R.id.txtDetailsCategory);
        txtDetailsDate = view.findViewById(R.id.txtDetailsDate);
        txtDetailsDescription = view.findViewById(R.id.txtDetailsDescription);
        txtDetailsRegisteredCount = view.findViewById(R.id.txtDetailsRegisteredCount);
        txtDetailsAverageRating = view.findViewById(R.id.txtDetailsAverageRating);
        txtDetailsReviewsLabel = view.findViewById(R.id.txtDetailsReviewsLabel);
        txtDetailsReviews = view.findViewById(R.id.txtDetailsReviews);
        txtDetailsStatus = view.findViewById(R.id.txtDetailsStatus);
        btnDetailsRegister = view.findViewById(R.id.btnDetailsRegister);
        btnDetailsAddToMy = view.findViewById(R.id.btnDetailsAddToMy);
        btnDetailsShare = view.findViewById(R.id.btnDetailsShare);
    }

    private void displayEventDetails() {
        if (event == null) {
            return;
        }

        imgDetailsEvent.setImageResource(event.getImageResource());
        txtDetailsName.setText(event.getName());
        txtDetailsOrganizer.setText(event.getOrganizer());
        txtDetailsCategory.setText(event.getCategory());
        txtDetailsDate.setText(event.getDate());
        txtDetailsDescription.setText(event.getDescription());
        txtDetailsRegisteredCount.setText(getString(R.string.registered_users_count, event.getRegisteredCount()));
        updateRegistrationUI();
    }

    private void updateRegistrationUI() {
        if (event == null) {
            return;
        }
        if (event.isUserRegistered()) {
            txtDetailsStatus.setText(R.string.status_registered);
            btnDetailsRegister.setEnabled(false);
            btnDetailsRegister.setText(R.string.btn_registered);
        } else {
            txtDetailsStatus.setText(R.string.status_not_registered);
            btnDetailsRegister.setEnabled(true);
            btnDetailsRegister.setText(R.string.btn_register_for_event);
        }
    }

    private void checkRegistrationFromFirestore() {
        if (event == null || event.getFirestoreId() == null || FirebaseAuth.getInstance().getCurrentUser() == null) {
            return;
        }

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        firestore.collection("events").document(event.getFirestoreId())
                .collection("registrations").document(uid)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        event.setUserRegistered(true);
                        dbHelper.setRegistrationStatus(event.getId(), true);
                        updateRegistrationUI();
                    }
                });
    }

    private void loadReviews() {
        if (event == null || event.getFirestoreId() == null) {
            txtDetailsAverageRating.setText(R.string.average_rating_none);
            txtDetailsReviews.setText(R.string.no_reviews);
            return;
        }

        firestore.collection("reviews")
                .whereEqualTo("eventId", event.getFirestoreId())
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.isEmpty()) {
                        txtDetailsAverageRating.setText(R.string.average_rating_none);
                        txtDetailsReviews.setText(R.string.no_reviews);
                        return;
                    }

                    StringBuilder reviewsText = new StringBuilder();
                    int totalRating = 0;
                    int count = 0;

                    for (QueryDocumentSnapshot doc : snapshot) {
                        Review review = doc.toObject(Review.class);
                        if (review == null) {
                            continue;
                        }
                        String name = review.getUserName() != null ? review.getUserName() : "";
                        String text = review.getText() != null ? review.getText() : "";
                        reviewsText.append(getString(R.string.review_item, name, review.getRating(), text))
                                .append("\n\n");
                        totalRating += review.getRating();
                        count++;
                    }

                    if (count == 0) {
                        txtDetailsAverageRating.setText(R.string.average_rating_none);
                        txtDetailsReviews.setText(R.string.no_reviews);
                    } else {
                        txtDetailsAverageRating.setText(getString(R.string.average_rating, (float) totalRating / count));
                        txtDetailsReviews.setText(reviewsText.toString().trim());
                    }
                })
                .addOnFailureListener(e -> {
                    txtDetailsAverageRating.setText(R.string.average_rating_none);
                    txtDetailsReviews.setText(R.string.no_reviews);
                });
    }

    private void setListeners() {
        btnDetailsRegister.setOnClickListener(v -> registerForEvent());

        btnDetailsAddToMy.setOnClickListener(v -> {
            if (dbHelper.addToMyEvents(event.getId())) {
                Toast.makeText(getContext(), R.string.success_added_to_my, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), R.string.error_already_in_my, Toast.LENGTH_SHORT).show();
            }
        });

        btnDetailsShare.setOnClickListener(v -> shareEvent());
    }

    private void registerForEvent() {
        if (event == null || event.isUserRegistered()) {
            return;
        }

        if (event.getFirestoreId() == null) {
            event.setUserRegistered(true);
            dbHelper.setRegistrationStatus(event.getId(), true);
            dbHelper.updateRegisteredCount(event.getId(), event.getRegisteredCount() + 1);
            event.setRegisteredCount(event.getRegisteredCount() + 1);
            updateRegistrationUI();
            txtDetailsRegisteredCount.setText(getString(R.string.registered_users_count, event.getRegisteredCount()));
            Toast.makeText(getContext(), R.string.success_event_registration, Toast.LENGTH_SHORT).show();
            return;
        }

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            return;
        }

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        firestore.collection("events").document(event.getFirestoreId())
                .collection("registrations").document(uid)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        event.setUserRegistered(true);
                        dbHelper.setRegistrationStatus(event.getId(), true);
                        updateRegistrationUI();
                        Toast.makeText(getContext(), R.string.error_already_registered_event, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Map<String, Object> registration = new HashMap<>();
                    registration.put("userId", uid);

                    WriteBatch batch = firestore.batch();
                    batch.set(firestore.collection("events").document(event.getFirestoreId())
                            .collection("registrations").document(uid), registration);
                    batch.update(firestore.collection("events").document(event.getFirestoreId()),
                            "registeredCount", FieldValue.increment(1));

                    batch.commit()
                            .addOnSuccessListener(aVoid -> {
                                event.setUserRegistered(true);
                                event.setRegisteredCount(event.getRegisteredCount() + 1);
                                dbHelper.setRegistrationStatus(event.getId(), true);
                                dbHelper.updateRegisteredCount(event.getId(), event.getRegisteredCount());
                                updateRegistrationUI();
                                txtDetailsRegisteredCount.setText(getString(R.string.registered_users_count, event.getRegisteredCount()));
                                Toast.makeText(getContext(), R.string.success_event_registration, Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> Toast.makeText(getContext(),
                                    getString(R.string.error_event_registration, e.getMessage()), Toast.LENGTH_SHORT).show());
                });
    }

    private void shareEvent() {
        String shareText = getString(R.string.share_event_text,
                event.getName(), event.getOrganizer(), event.getCategory(), event.getDate())
                + "\n" + event.getDescription();

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, shareText);
        startActivity(Intent.createChooser(intent, getString(R.string.share_event_chooser)));
    }
}
