package com.example.studentevent.ui;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.studentevent.R;
import com.example.studentevent.model.Review;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Purpose: Dialog for writing an event review.
 * Input: Event id, name, and whether the user is registered.
 * Output: Saves review to Firestore if allowed.
 */
public class WriteReviewFragment extends DialogFragment {

    public interface ReviewSavedListener {
        void onReviewSaved();
    }

    public static WriteReviewFragment newInstance(String eventFirestoreId, String eventName, boolean isRegistered) {
        WriteReviewFragment fragment = new WriteReviewFragment();
        Bundle args = new Bundle();
        args.putString("eventId", eventFirestoreId);
        args.putString("eventName", eventName);
        args.putBoolean("isRegistered", isRegistered);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (!requireArguments().getBoolean("isRegistered")) {
            Toast.makeText(getContext(), R.string.error_review_not_registered, Toast.LENGTH_SHORT).show();
            return new AlertDialog.Builder(requireContext()).create();
        }

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_write_review, null);
        Spinner spinnerRating = view.findViewById(R.id.spinnerRating);
        EditText edtReviewText = view.findViewById(R.id.edtReviewText);
        Button btnSaveReview = view.findViewById(R.id.btnSaveReview);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(), R.array.rating_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRating.setAdapter(adapter);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle(R.string.title_write_review)
                .setView(view)
                .create();

        btnSaveReview.setOnClickListener(v -> saveReview(dialog, spinnerRating, edtReviewText));

        return dialog;
    }

    private void saveReview(AlertDialog dialog, Spinner spinnerRating, EditText edtReviewText) {
        String text = edtReviewText.getText().toString().trim();
        if (text.isEmpty()) {
            Toast.makeText(getContext(), R.string.error_review_empty, Toast.LENGTH_SHORT).show();
            return;
        }

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            return;
        }

        String eventId = requireArguments().getString("eventId");
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        SharedPreferences prefs = requireContext().getSharedPreferences("UserPrefs", 0);
        String userName = prefs.getString("user_name", "");
        int rating = spinnerRating.getSelectedItemPosition() + 1;

        String reviewDocId = eventId + "_" + uid;
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        firestore.collection("reviews").document(reviewDocId).get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        Toast.makeText(getContext(), R.string.error_review_already_exists, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Review review = new Review();
                    review.setEventId(eventId);
                    review.setUserId(uid);
                    review.setUserName(userName);
                    review.setRating(rating);
                    review.setText(text);

                    firestore.collection("reviews").document(reviewDocId).set(review)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(getContext(), R.string.success_review_saved, Toast.LENGTH_SHORT).show();
                                if (getTargetFragment() instanceof ReviewSavedListener) {
                                    ((ReviewSavedListener) getTargetFragment()).onReviewSaved();
                                }
                                dialog.dismiss();
                            })
                            .addOnFailureListener(e -> Toast.makeText(getContext(),
                                    getString(R.string.error_review_save, e.getMessage()), Toast.LENGTH_SHORT).show());
                });
    }
}
