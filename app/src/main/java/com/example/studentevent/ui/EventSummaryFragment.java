package com.example.studentevent.ui;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class EventSummaryFragment extends DialogFragment {

    /**
     * Purpose: Creates fragment with event details and reminder date.
     * Input: Event name, organizer, category, event date, and reminder date.
     * Output: EventSummaryFragment object with data.
     */
    public static EventSummaryFragment newInstance(String name, String organizer, String category,
                                                   String date, String reminderDate) {
        EventSummaryFragment fragment = new EventSummaryFragment();

        Bundle args = new Bundle();
        args.putString("name", name);
        args.putString("organizer", organizer);
        args.putString("category", category);
        args.putString("date", date);
        args.putString("reminderDate", reminderDate);

        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Purpose: Displays event summary dialog.
     * Input: savedInstanceState contains previous fragment state if it exists.
     * Output: Dialog with event summary and share button.
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();

        String summary = "שם האירוע: " + args.getString("name")
                + "\nשם המרצה / המארגן: " + args.getString("organizer")
                + "\nקטגוריה: " + args.getString("category")
                + "\nתאריך האירוע: " + args.getString("date")
                + "\nתאריך התזכורת שבחרת: " + args.getString("reminderDate");

        return new AlertDialog.Builder(requireContext())
                .setTitle("סיכום אירוע")
                .setMessage(summary)
                .setPositiveButton("שתף סיכום אירוע", (dialog, which) -> shareSummary(summary))
                .setNegativeButton("סגור", null)
                .create();
    }

    /**
     * Purpose: Shares event summary using implicit intent.
     * Input: summary is the event details text.
     * Output: Opens Android share menu.
     */
    private void shareSummary(String summary) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, summary);

        startActivity(Intent.createChooser(intent, "שתף סיכום אירוע"));
    }
}