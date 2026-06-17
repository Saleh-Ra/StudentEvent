package com.example.studentevent.ui;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.studentevent.R;

/**
 * Purpose: Shows a summary of a personal event with reminder and participation status.
 * Input: Event details passed as fragment arguments.
 * Output: Summary dialog with share option.
 */
public class EventSummaryFragment extends DialogFragment {

    public static EventSummaryFragment newInstance(String name, String organizer, String category,
                                                   String date, String reminderDate, boolean isRegistered) {
        EventSummaryFragment fragment = new EventSummaryFragment();
        Bundle args = new Bundle();
        args.putString("name", name);
        args.putString("organizer", organizer);
        args.putString("category", category);
        args.putString("date", date);
        args.putString("reminderDate", reminderDate);
        args.putBoolean("isRegistered", isRegistered);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = requireArguments();

        String participation = args.getBoolean("isRegistered")
                ? getString(R.string.summary_participation_registered)
                : getString(R.string.summary_participation_not_registered);

        String summary = getString(R.string.summary_event_name, args.getString("name")) + "\n"
                + getString(R.string.summary_organizer, args.getString("organizer")) + "\n"
                + getString(R.string.summary_category, args.getString("category")) + "\n"
                + getString(R.string.summary_event_date, args.getString("date")) + "\n"
                + getString(R.string.summary_reminder_date, args.getString("reminderDate")) + "\n"
                + participation;

        return new AlertDialog.Builder(requireContext())
                .setTitle(R.string.title_event_summary)
                .setMessage(summary)
                .setPositiveButton(R.string.btn_share_summary, (dialog, which) -> shareSummary(summary))
                .setNegativeButton(R.string.btn_close, null)
                .create();
    }

    private void shareSummary(String summary) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, summary);
        startActivity(Intent.createChooser(intent, getString(R.string.btn_share_summary)));
    }
}
